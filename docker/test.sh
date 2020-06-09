docker run -it -v /tmp/discovery/discovery:/opt/bdl/data -v /tmp/discovery/ingest:/opt/bdl/ingest -p 8180:8180 \
	registry.cloud.bigconnect.io/discovery:1.0.0 