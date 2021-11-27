#!/bin/bash

echo "Bootstrapping BigConnect Discovery...."

sed -i 's@TENANT_ID@'"$TENANT_ID"'@g' /opt/bdl/etc/hadoop/core-site.xml
sed -i 's@AWS_ACCESS_KEY@'"$AWS_ACCESS_KEY"'@g' /opt/bdl/etc/hadoop/core-site.xml
sed -i 's@AWS_SECRET_KEY@'"$AWS_SECRET_KEY"'@g' /opt/bdl/etc/hadoop/core-site.xml

nohup $JDK8_HOME/bin/java -jar -Xmx4g -Djava.security.egd=file:/dev/./urandom /opt/bdl/lib/spark/discovery-prep-spark-engine-4.1.0.jar > /opt/bdl/log/sparkengine.log &

/opt/bdl/sbin/start-discovery.sh
