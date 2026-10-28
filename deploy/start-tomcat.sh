#!/bin/sh
set -eu
PORT="${PORT:-10000}"
# Render provides PORT at runtime; keep Tomcat's HTTP connector aligned with it.
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"
# Containers are stopped with SIGTERM, so disable Tomcat's remote shutdown socket.
# This prevents non-HTTP probes from reaching the shutdown port.
sed -i 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="-1" shutdown="SHUTDOWN">/' "$CATALINA_HOME/conf/server.xml"
exec "$CATALINA_HOME/bin/catalina.sh" run
