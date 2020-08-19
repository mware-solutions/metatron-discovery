FROM registry.cloud.bigconnect.io/service-container-base:latest

ENV KC_CONFIG /opt/bdl/etc/
ENV HADOOP_CONF_DIR /opt/bdl/etc/hadoop

RUN apt install -y unzip \
    && mkdir /opt/bdl \
    && mkdir -p /opt/bdl/data \
    && mkdir -p /opt/bdl/ingest \
    && mkdir -p /opt/bdl/log

COPY ./bin /opt/bdl/bin
COPY ./etc /opt/bdl/etc
COPY ./lib /opt/bdl/lib
COPY ./sbin /opt/bdl/sbin

RUN unzip -d /opt/bdl/lib/discovery/extensions/athena-connection-1.0.0-2.0.2-3.4.0-rc4 /opt/bdl/lib/discovery/extensions/athena*.zip \
    && unzip -d /opt/bdl/lib/discovery/extensions/mssql-connection-7.3.0-3.4.0-rc4 /opt/bdl/lib/discovery/extensions/mssql*.zip \
    && rm -f /opt/bdl/lib/discovery/extensions/*.zip

VOLUME /opt/bdl/data
VOLUME /opt/bdl/ingest

EXPOSE 8180
EXPOSE 45005

COPY entrypoint.sh /opt/entrypoint.sh
RUN chmod a+x /opt/entrypoint.sh

ENTRYPOINT ["/opt/entrypoint.sh"]
