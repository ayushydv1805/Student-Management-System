package edu.college.sms.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;
import java.util.*;

 /**
  * Minimal JDBC-compatible bridge for hosted deployments where the runtime cannot
  * open a direct PostgreSQL socket. SQL still executes on Supabase PostgreSQL,
  * inside a restricted database role, over an authenticated HTTPS Edge Function.
  */
public final class HttpSqlConnection {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();
    private static final TypeReference<Map<String,Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Map<String,Object>>> ROWS_TYPE = new TypeReference<>() {};

    private HttpSqlConnection() {}

    public static Connection open(String endpoint,String token) {
        InvocationHandler handler=(proxy,method,args)->{
            String name=method.getName();
            if ("prepareStatement".equals(name)) return preparedStatement((String)args[0],proxy,endpoint,token);
            if ("getMetaData".equals(name)) return metadata();
            if ("close".equals(name)) return null;
            if ("isClosed".equals(name)) return false;
            if ("isValid".equals(name)) return true;
            if ("getAutoCommit".equals(name)) return true;
            if ("toString".equals(name)) return "CampusDesk HTTPS SQL connection";
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("equals".equals(name)) return proxy==args[0];
            if ("unwrap".equals(name)) throw new SQLFeatureNotSupportedException("unwrap is not supported by the HTTPS SQL bridge.");
            if ("isWrapperFor".equals(name)) return false;
            throw new SQLFeatureNotSupportedException("Connection method is not supported by the HTTPS SQL bridge: "+name);
        };
        return (Connection)Proxy.newProxyInstance(HttpSqlConnection.class.getClassLoader(),
            new Class<?>[]{Connection.class},handler);
    }

    private static DatabaseMetaData metadata() {
        InvocationHandler handler=(proxy,method,args)->switch(method.getName()) {
            case "getDatabaseProductName" -> "PostgreSQL";
            case "getDatabaseProductVersion" -> "Supabase PostgreSQL";
            case "getDriverName" -> "CampusDesk HTTPS SQL bridge";
            case "getURL" -> "https://supabase.com/functions/v1/campusdesk-sql-gateway";
            case "supportsTransactions" -> true;
            case "supportsBatchUpdates" -> false;
            case "toString" -> "CampusDesk PostgreSQL metadata";
            case "isWrapperFor" -> false;
            case "unwrap" -> throw new SQLFeatureNotSupportedException("unwrap is not supported.");
            default -> throw new SQLFeatureNotSupportedException("DatabaseMetaData method is not supported: "+method.getName());
        };
        return (DatabaseMetaData)Proxy.newProxyInstance(HttpSqlConnection.class.getClassLoader(),
            new Class<?>[]{DatabaseMetaData.class},handler);
    }

    private static PreparedStatement preparedStatement(String rawSql,Object connection,String endpoint,String token) {
        String sql=postgresSql(rawSql);
        int parameterCount=(int)sql.chars().filter(ch->ch=='$').count();
        // The DAO SQL has no dollar-quoted strings; counting the highest $n is more reliable than counting '$'.
        java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("\\$(\\d+)").matcher(sql);
        while(matcher.find()) parameterCount=Math.max(parameterCount,Integer.parseInt(matcher.group(1)));
        List<Object> values=new ArrayList<>(Collections.nCopies(parameterCount,null));
        InvocationHandler handler=(proxy,method,args)->{
            String name=method.getName();
            if (name.startsWith("set") && args!=null && args.length>=2 && args[0] instanceof Integer index) {
                while(values.size()<index)values.add(null);
                values.set(index-1,"setNull".equals(name)?null:normalize(args[1]));
                return null;
            }
            if ("clearParameters".equals(name)) { Collections.fill(values,null); return null; }
            if ("executeQuery".equals(name)) {
                Map<String,Object> result=execute(endpoint,token,sql,values,"query");
                Object rawRows=result.get("rows");
                @SuppressWarnings("unchecked") List<Map<String,Object>> rows=rawRows instanceof List<?> ? (List<Map<String,Object>>)rawRows : List.of();
                return resultSet(rows,proxy);
            }
            if ("executeUpdate".equals(name)) {
                Map<String,Object> result=execute(endpoint,token,sql,values,"update");
                Object count=result.get("count");
                return count instanceof Number n?n.intValue():0;
            }
            if ("execute".equals(name)) {
                execute(endpoint,token,sql,values,"update");
                return false;
            }
            if ("getConnection".equals(name)) return connection;
            if ("close".equals(name)) return null;
            if ("isClosed".equals(name)) return false;
            if ("toString".equals(name)) return "HTTPS PreparedStatement: "+sql;
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("equals".equals(name)) return proxy==args[0];
            if ("isWrapperFor".equals(name)) return false;
            if ("unwrap".equals(name)) throw new SQLFeatureNotSupportedException("unwrap is not supported.");
            throw new SQLFeatureNotSupportedException("PreparedStatement method is not supported: "+name);
        };
        return (PreparedStatement)Proxy.newProxyInstance(HttpSqlConnection.class.getClassLoader(),
            new Class<?>[]{PreparedStatement.class},handler);
    }

    private static String postgresSql(String sql) {
        // PostgreSQL needs a type for an untyped NULL in patterns such as (? IS NULL OR ...).
        String typed=sql.replaceAll("\\?\\s+IS\\s+NULL","CAST(? AS VARCHAR) IS NULL");
        StringBuilder out=new StringBuilder(typed.length()+16);int index=1;
        for(int i=0;i<typed.length();i++){
            char ch=typed.charAt(i);
            if(ch=='?')out.append('$').append(index++);else out.append(ch);
        }
        return out.toString();
    }

    private static Object normalize(Object value) {
        if(value==null)return null;
        if(value instanceof java.sql.Date d)return d.toString();
        if(value instanceof Timestamp t)return t.toInstant().toString();
        if(value instanceof Time t)return t.toString();
        return value;
    }

    private static Map<String,Object> execute(String endpoint,String token,String sql,List<Object> values,String mode)throws SQLException {
        try {
            Map<String,Object> payload=new LinkedHashMap<>();
            payload.put("sql",sql);payload.put("params",values.stream().map(HttpSqlConnection::normalize).toList());payload.put("mode",mode);
            String body=JSON.writeValueAsString(payload);
            HttpRequest request=HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type","application/json")
                .header("X-CampusDesk-Token",token)
                .POST(HttpRequest.BodyPublishers.ofString(body,StandardCharsets.UTF_8)).build();
            HttpResponse<String> response=HTTP.send(request,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if(response.statusCode()<200||response.statusCode()>=300) {
                throw new SQLException("Supabase SQL gateway returned HTTP "+response.statusCode()+": "+truncate(response.body(),400));
            }
            return JSON.readValue(response.body(),MAP_TYPE);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();throw new SQLException("Interrupted while calling the Supabase SQL gateway.",e);
        } catch(IOException|IllegalArgumentException e) {
            throw new SQLException("Unable to call the configured Supabase SQL gateway.",e);
        }
    }

    private static String truncate(String value,int maximum) {
        if(value==null)return "";
        return value.length()<=maximum?value:value.substring(0,maximum);
    }

    private static ResultSet resultSet(List<Map<String,Object>> rows,PreparedStatement statement) {
        int[] cursor={-1};boolean[] closed={false};boolean[] lastNull={false};
        InvocationHandler handler=(proxy,method,args)->{
            String name=method.getName();
            if("next".equals(name)){if(closed[0])throw new SQLException("ResultSet is closed.");cursor[0]++;return cursor[0]<rows.size();}
            if("close".equals(name)){closed[0]=true;return null;}
            if("isClosed".equals(name))return closed[0];
            if("getStatement".equals(name))return statement;
            if("getRow".equals(name))return cursor[0]>=0&&cursor[0]<rows.size()?cursor[0]+1:0;
            if("wasNull".equals(name))return lastNull[0];
            if("findColumn".equals(name))return findColumn(rows,cursor[0],String.valueOf(args[0]));
            if(name.startsWith("get") && args!=null && args.length>=1) {
                Object value=columnValue(rows,cursor[0],args[0]);lastNull[0]=value==null;
                if("getObject".equals(name)){
                    if(args.length==2 && args[1] instanceof Class<?> target)return convert(value,target);
                    return value;
                }
                if("getString".equals(name))return value==null?null:String.valueOf(value);
                if("getInt".equals(name))return value==null?0:asNumber(value).intValue();
                if("getLong".equals(name))return value==null?0L:asNumber(value).longValue();
                if("getDouble".equals(name))return value==null?0d:asNumber(value).doubleValue();
                if("getFloat".equals(name))return value==null?0f:asNumber(value).floatValue();
                if("getBigDecimal".equals(name))return value==null?null:(value instanceof BigDecimal b?b:new BigDecimal(String.valueOf(value)));
                if("getDate".equals(name)){
                    if(value==null)return null;
                    String text=String.valueOf(value);
                    if(text.length()>=10)return java.sql.Date.valueOf(text.substring(0,10));
                    return java.sql.Date.valueOf(text);
                }
                if("getBoolean".equals(name))return value!=null&&(value.equals(Boolean.TRUE)||"true".equalsIgnoreCase(String.valueOf(value))||"1".equals(String.valueOf(value)));
            }
            if("toString".equals(name))return "HTTPS ResultSet row "+cursor[0];
            if("hashCode".equals(name))return System.identityHashCode(proxy);
            if("equals".equals(name))return proxy==args[0];
            if("isWrapperFor".equals(name))return false;
            if("unwrap".equals(name))throw new SQLFeatureNotSupportedException("unwrap is not supported.");
            throw new SQLFeatureNotSupportedException("ResultSet method is not supported: "+name);
        };
        return (ResultSet)Proxy.newProxyInstance(HttpSqlConnection.class.getClassLoader(),
            new Class<?>[]{ResultSet.class},handler);
    }

    private static Object columnValue(List<Map<String,Object>> rows,int cursor,Object label)throws SQLException{
        if(cursor<0||cursor>=rows.size())throw new SQLException("Cursor is not positioned on a row.");
        Map<String,Object> row=rows.get(cursor);
        if(label instanceof Number n){
            int index=n.intValue();if(index<1||index>row.size())throw new SQLException("Invalid column index: "+index);
            return row.values().stream().skip(index-1L).findFirst().orElse(null);
        }
        String key=String.valueOf(label);
        if(row.containsKey(key))return row.get(key);
        for(Map.Entry<String,Object> e:row.entrySet())if(e.getKey().equalsIgnoreCase(key))return e.getValue();
        throw new SQLException("Unknown result column: "+key);
    }

    private static int findColumn(List<Map<String,Object>> rows,int cursor,String label)throws SQLException{
        if(cursor<0||cursor>=rows.size())throw new SQLException("Cursor is not positioned on a row.");
        int index=1;for(String key:rows.get(cursor).keySet()){if(key.equalsIgnoreCase(label))return index;index++;}
        throw new SQLException("Unknown result column: "+label);
    }

    private static Number asNumber(Object value)throws SQLException{
        if(value instanceof Number n)return n;
        try{return new BigDecimal(String.valueOf(value));}
        catch(NumberFormatException e){throw new SQLException("Result column is not numeric.",e);}
    }

    private static Object convert(Object value,Class<?> target)throws SQLException{
        if(value==null)return null;
        if(target==String.class)return String.valueOf(value);
        if(target==Integer.class||target==int.class)return asNumber(value).intValue();
        if(target==Long.class||target==long.class)return asNumber(value).longValue();
        if(target==BigDecimal.class)return value instanceof BigDecimal?value:new BigDecimal(String.valueOf(value));
        if(target==java.sql.Date.class){String s=String.valueOf(value);return java.sql.Date.valueOf(s.substring(0,10));}
        if(target.isInstance(value))return value;
        throw new SQLFeatureNotSupportedException("Result column conversion is not supported for "+target.getName());
    }
}
