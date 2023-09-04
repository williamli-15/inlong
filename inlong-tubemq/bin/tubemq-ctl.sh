#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# project directory
if [ -z "$BASE_DIR" ] ; then
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done
  BASE_DIR=`dirname "$PRG"`/..

  # make it fully qualified
  BASE_DIR=`cd "$BASE_DIR" && pwd`
fi

# load environmental variables
source $BASE_DIR/bin/env.sh


# display usage
function help() {
  echo "Usage: tubemq-ctl {topic|producer|consumer}" >&2
  echo "       topic:      manage the topic, subcommands: {query|create|delete|modify}"
  echo "       producer:   send message."
  echo "       consumer:   consume message."
}


if [ $# -lt 2 ]; then
  help;
  exit 1;
fi

SERVICE=$1

SUBCOMMAND=""
case $SERVICE in
  topic)
    SERVICE_CLASS="org.apache.inlong.tubemq.server.tools.cli.CliTopicAdmin"
    shift 1
    case $1 in
      query)
        SUBCOMMAND="query"
        ;;
      create)
        SUBCOMMAND="create"
        ;;
      delete)
        SUBCOMMAND="delete"
        ;;
      modify)
        SUBCOMMAND="modify"
        ;;
    esac
    ;;
  producer)
    SERVICE_CLASS="org.apache.inlong.tubemq.server.tools.cli.CliProducer"
    ;;
  consumer)
    SERVICE_CLASS="org.apache.inlong.tubemq.server.tools.cli.CliConsumer"
    ;;
  *)
    help;
    exit 1;
    ;;
esac

shift 1
OTHERS="$*"

#echo "$JAVA $TOOLS_ARGS $SERVICE_CLASS $SUBCOMMAND $OTHERS"

$JAVA $TOOLS_ARGS $SERVICE_CLASS $SUBCOMMAND $OTHERS