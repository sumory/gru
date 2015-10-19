#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf

SERVER_NAME=gru_ticket


PIDS=`ps  aux | grep java | grep "$CONF_DIR" | grep -v grep  |awk '{print $2}'`
if [ -z "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME does not started!"
    exit 1
fi

echo "Stopping $SERVER_NAME..."
for PID in $PIDS ; do
	echo "kill PID: "$PID
	kill -15 $PID > /dev/null 2>&1
done

sleep 4s

echo "OK!"
echo "PID: $PIDS"