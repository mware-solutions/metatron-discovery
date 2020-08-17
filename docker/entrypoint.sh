#!/bin/bash

echo "Bootstrapping BigConnect Discovery...."

sed -i 's@TENANT_ID@'"$TENANT_ID"'@g' /opt/bdl/etc/hadoop/core-site.xml

nohup $JDK8_HOME/bin/java -jar /opt/bdl/lib/spark/discovery-prep-spark-engine-4.1.0.jar > /opt/bdl/log/sparkengine.log &

/opt/bdl/sbin/start-discovery.sh
