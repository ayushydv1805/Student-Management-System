# Build the Java 21 WAR, then run it on Tomcat 10.1 (Jakarta Servlet 6).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package

FROM tomcat:10.1-jdk21-temurin
ENV CATALINA_HOME=/usr/local/tomcat
RUN rm -rf "$CATALINA_HOME/webapps/"*
COPY --from=build /build/target/student-management-system.war "$CATALINA_HOME/webapps/ROOT.war"
COPY deploy/start-tomcat.sh /usr/local/bin/start-tomcat
RUN chmod +x /usr/local/bin/start-tomcat
EXPOSE 10000
ENTRYPOINT ["/usr/local/bin/start-tomcat"]
