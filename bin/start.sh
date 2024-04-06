#!/bin/bash
CLASSPATH=.:resources:resources/static:resources/templates:resources/mybatis:$CLASSPATH
DEBUG='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044'
export CLASSPATH=$CLASSPATH
echo $CLASSPATH
java -jar golden-monitor.jar --spring.config.location=./application.properties