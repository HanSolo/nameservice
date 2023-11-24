#!/bin/bash

START_TIME=$(($(date +'%s * 1000 + %-N / 1000000')))
echo $START_TIME

java -DSTART_TIME=$START_TIME -Dspring.context.checkpoint=onRefresh -XX:CRaCCheckpointTo=./tmp_checkpoint -jar build/libs/nameservice-21.0.0.jar