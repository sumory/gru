#!/bin/bash

cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf

echo "curr dir is $DEPLOY_DIR"


SERVER_NAME=gru_spear
STDOUT_FILE=$DEPLOY_DIR/stdout.log

LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dio.netty.leakDetectionLevel=advanced"


RMI_SERVER_HOSTNAME=10.60.0.67
JMX_REMOTE_PORT=10900
JAVA_JMX_OPTS=" -Djava.rmi.server.hostname="$RMI_SERVER_HOSTNAME" -Dcom.sun.management.jmxremote.port="$JMX_REMOTE_PORT" -Dcom.sun.management.jmxremote=true  -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
#JAVA_JMX_OPTS=""


JAVA_MEM_OPTS=" -server -Xmx8g -Xms8g -Xmn3560m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "


echo -e "Starting the $SERVER_NAME ...\c"
nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_JMX_OPTS -classpath $CONF_DIR:$LIB_JARS com.sumory.gru.spear.main.SpearMain > $STDOUT_FILE 2>&1 &


echo "OK!"
PIDS=`ps  --no-heading -C java -f --width 1000 | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
echo $PIDS>pid
echo "STDOUT: $STDOUT_FILE"
