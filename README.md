BigConnect Discovery
------------------

**BigConnect Discovery** is an end-to-end big data self discovery solution.
To learn more about it, visit our [web site](https://bigconnect.io). 
Check [our blog](https://bigconnect.io/blog/) for upcoming events and development news. 
Also if you got stuck when using BigConnect Discovery, please leave your problem on our [user forum](https://community.bigconnect.io/c/bigconnect-discovery).


Strengths
------------------

- Able to process HUGE data sets super FAST, powered by (optimized) [Apache Druid](http://druid.io/).
- Single solution for data analytics from easy data preparation to fast visualization.
- Easy data analytics for everyone.


Key Features
------------------

Metatron provides:
- Interactive dashboards with numerous preloaded charts.
- Query(SQL) based data exploration and GUI based data wrangling.
- Various data source connections (e.g. DBs, HIVE, or Kafka streams).
- Geo data analysis with geospatial operations.
- Job and data usage monitoring.
- Metadata management.
- 3rd party data analytics tool integration(e.g. [Apache Zeppelin](https://zeppelin.apache.org/)).
- Fine grain access control of users and workspaces.
- Overcomes Druid weaknesses(e.g. no data join function) without performance degradation using.
- Full API support, enabling easy integration into your environment/software.
- Multiple languages according to browser setting(Currently supporting English, Korean, Chinese).
- Available on AWS, Azure('19.3Q).
- Docker support for distributed version deploy('19.3Q).
- Monitoring Metatron engine performance('19.3Q).

Installation
----------------------------

### Requirements
- MacOS / Linux (Redhat, CentOS)
- JDK 1.8
- **Druid customized version for Metatron**
  - [Here is the link for downloading the archive.](https://sktmetatronkrsouthshared.blob.core.windows.net/metatron-public/discovery-dist/latest/druid-0.9.1-latest-hadoop-2.7.3-bin.tar.gz)
  - To install the Metatron distributed Druid, simply untar the downloaded archive. And start | stop the druid with the following commands.
  <pre><code> $ start-single.sh | stop-single.sh </code></pre>
- (Optional) Apache Maven 3.3+ for building the project

### Install Metatron Discovery
There is two way to get the running binary file.

- Directly download the final binary file from [this link](https://sktmetatronkrsouthshared.blob.core.windows.net/metatron-public/discovery-dist/latest/metatron-discovery-latest-bin.tar.gz)
- Or, build source code from this repository as shown below:
  - Clone this project.
  <pre><code>$ git clone https://github.com/mware-solutions/bigconnect-discovery.git</code></pre>
  - Build through Maven 3.3+.
  <pre><code>$ mvn clean install -DskipTests</code></pre>
  Building the whole project takes some time especially for the "discovery-frontend". Please wait a few minutes.
  
  If the build succeeds, you can find an archive file under "discovery-distribution/target"

### Start up BigConnect Discovery
Untar the archive binary file of BigConnect Discovery.
<pre><code>$ tar zxf bigconnect-discovery-{VERSION}-{TIMESTAMP}-bin.tar.gz</code></pre>

#### Configuration (optional)
BigConnect Discovery loads its configuration from the files under “/conf” directory by default. We already wrote some frequent configurations in the template files. For your own configuration of BigConnect Discovery application, you should create a new configuration file with reference to the pre-distributed template file as belows. In the generated setting file, refer to [the configuration guide](https://github.com/metatron-app/metatron-discovery/blob/master/discovery-server/src/main/asciidoc/application-config-guide.adoc) and specify detailed setting information.

<pre><code>$ cp ./conf/application-config.templete.yaml ./conf/application-config.yaml</code></pre>

To configure the environment in which the server is running, you need to configure server memory or classpath settings by editing the “discovery-env.sh” file.

<pre><code>$ cp ./conf/discovery-env.sh.templete ./conf/discovery-env.sh</code></pre>

For example, if you want to use MySQL and increase the memory, you should set it as below. See the comments in the file “discovery-env.sh.templete” for more information.
```
export METATRON_JAVA_OPTS=-Xms4g -Xmx4g
export METATRON_DB_TYPE=mysql
```

#### Run Bigconnect Discovery
Run with the following command.
<pre><code>$ bin/discovery.sh start</code></pre>

Running options are provided as well.
<pre><code>$ bin/discovery.sh [--config=directory] [--management] [--debug=port] {start|stop|restart|status}</code></pre>
To access BigConnect Discovery, go to [http://localhost:8180](http://localhost:8180). (The default admin user account is provided as Username: admin, PW: admin.)

### Using REST API
BigConnect Discovery supports RESTful APIs. Please refer to the following details [how to using the REST API](.github/USE_REST_API.md)

Problems & Suggestions
----------------------------
This project welcomes contributions and suggestions. If you encounter any bugs or want to request new features, feel free to open an [GitHub Issue](https://github.com/metatron-app/metatron-discovery/issues) in the repo so that the community can find resolutions for it. Or reports bug to our [discussion forum](https://metatron.app/discussion/). Although, please check before you raise an issue. That is please make sure someone else hasn’t already created an issue for the same topic.

License
----------------------------
BigConnect Discovery is available under the Apache License V2.
