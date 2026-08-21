#!/usr/bin/env bash

#
# NOTICE
# 
# This software (or technical data) was produced for the U. S. Government
# and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV 
# (May 2014) – Alternative IV (Dec 2007)
# 
# (c) 2024 The MITRE Corporation. All Rights Reserved.
# 

CONFIG_PATH=/app/config/application.properties
LOG_FILE="log/biqt-component.$HOSTNAME.log"
JAVA_OPTS=""
SPRING_OPTS="--logging.file=$LOG_FILE"

. /etc/profile.d/biqt.sh

cd /app

if [ -f "$CONFIG_PATH" ]; then
  echo "External configuration found @ $CONFIG_PATH."
else
  echo "External configuration not found. You can add one by providing it at $CONFIG_PATH."
fi

RUN_JAR=$(find -type f -name '*.jar' | head -n 1)
java $JAVA_OPTS -jar $RUN_JAR $SPRING_OPTS