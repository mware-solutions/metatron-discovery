cd ..
mvn clean install -DskipTests

cp ./discovery-extensions/athena-connection/target/athena-connection-1.0.0-2.0.2-3.4.0-rc4.zip ./docker/lib/discovery/extensions/
cp ./discovery-extensions/mssql-connection/target/mssql-connection-7.3.0-3.4.0-rc4.zip ./docker/lib/discovery/extensions/
cp ./discovery-extensions/oracle-connection/target/oracle-connection-18.3.0.0-3.4.0-rc4.zip ./docker/lib/discovery/extensions/
cp ./discovery-server/target/discovery-server-3.4.0-rc4.jar ./docker/lib/discovery/

VERSION=1.0.0
docker login registry.cloud.bigconnect.io
docker build -t registry.cloud.bigconnect.io/discovery:$VERSION -f ./docker/Dockerfile ./docker
docker push registry.cloud.bigconnect.io/discovery:$VERSION
