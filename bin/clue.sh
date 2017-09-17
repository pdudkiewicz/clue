#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

lib=$bin/../target/lib
dist=$bin/../target

HEAP_OPTS="-Xmx1g -Xms1g -XX:+UseG1GC"
JAVA_OPTS="-server"
#JAVA_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y"

MAIN_CLASS="com.senseidb.clue.ClueApplication"
CLASSPATH=$CLASSPATH:$resources/:$lib/*:$dist/*

(cd $bin/..; java $JAVA_OPTS $JAVA_DEBUG $HEAP_OPTS -classpath $CLASSPATH $MAIN_CLASS $@)

