#!/bin/bash

echo "Bootstrapping BigConnect Discovery...."

sed -i 's@HDFS_ROOT@'"$HDFS_ROOT"'@g' /opt/bdl/etc/hadoop/core-site.xml

/opt/bdl/sbin/start-discovery.sh
