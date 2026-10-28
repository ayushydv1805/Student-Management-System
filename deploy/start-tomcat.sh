#!/bin/sh
set -eu
PORT="${PORT:-10000}"
# Render provides PORT at runtime; keep Tomcat's HTTP connector aligned with it.
sed -i "s/port=\"8080\"/port=\"$PORT\"/" "$CATALINA_HOME/conf/server.xml"
exec "$CATALINA_HOME/bin/catalina.sh" run
