/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specic language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.discovery.domain.engine;

import app.metatron.discovery.common.fileloader.FileLoaderProperties;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "polaris.engine")
public class EngineProperties {

  public final static String SEARCH_QUERY = "query";
  public final static String SQL_QUERY = "sql";
  public final static String BULK_LOAD = "load";
  public final static String GET_PROGRESS = "getProgress";
  public final static String GET_LOAD_DATASOURCE = "getLoadDatasourceInfo";
  public final static String GET_LOAD_DATASOURCE_LIST = "getLoadDatasources";
  public final static String DELETE_LOAD_DATASOURCE = "deleteLoadDatasources";
  public final static String CANCEL_QUERY = "cancelQuery";
  public final static String INGESTION_DATASOUCE = "ingestion";
  public final static String GET_INGESTION_STATUS = "ingestionStatus";
  public final static String GET_INGESTION_LOG = "ingestionLog";
  public final static String SHUTDOWN_INGESTION = "ingestionShutdown";
  public final static String SUPERVISOR_INGESTION = "supervisor";
  public final static String GET_SUPERVISOR_STATUS = "supervisorStatus";
  public final static String SHUTDOWN_SUPERVISOR = "supervisorShutdown";
  public final static String RESET_SUPERVISOR = "supervisorReset";
  public final static String GET_WORKER_STATUS = "workerStatus";
  public final static String GET_DATASOURCE_LIST = "getDatasources";
  public final static String GET_DATASOURCE_STATUS = "datasourceStatus";
  public final static String ENABLE_DATASOURCE = "datasourceEnable";
  public final static String DISABLE_DATASOURCE = "datasourceDisable";
  public final static String PURGE_DATASOURCE = "datasourcePurge";
  public final static String GET_HISTORICAL_NODE = "getHistoricalNode";
  public final static String GET_MIDDLEMGMT_NODE = "getMiddleMgmtNode";
  public final static String GET_CONFIGS = "getConfigs";
  public final static String GET_PENDING_TASKS = "getPendingTasks";
  public final static String GET_RUNNING_TASKS = "getRunningTasks";
  public final static String GET_WAITING_TASKS = "getWaitingTasks";
  public final static String GET_COMPLETE_TASKS = "getCompleteTasks";
  public final static String GET_SUPERVISOR_LIST = "getSupervisorList";
  public final static String GET_DATASOURCE_META = "getMetaDatasource";
  public final static String GET_DATASOURCE_RULES = "datasourceRules";
  public final static String GET_DATASOURCE_RULE = "datasourceRule";
  public final static String SET_DATASOURCE_RULE = "setDatasourceRule";
  public final static String GET_DATASOURCE_INTERVAL_LIST = "datasourceIntervals";
  public final static String GET_DATASOURCE_INTERVALS_STATUS = "datasourceIntervalStatus";
  public final static String GET_DATASOURCE_LOAD_STATUS = "datasourceLoadStatus";
  public final static String GET_RUNNING_IDS = "getRunningIds";
  public final static String SQL = "sql";

  public final static String TEMP_CSV_PREFIX = "temp_ds_";

  Map<String, String> hostname;

  Map<String, EngineApi> api;

  IngestionInfo ingestion;

  QueryInfo query;

  @PostConstruct
  public void init() {
    api.forEach((s, engineApi) -> engineApi.makeTargetUrl(hostname.get(engineApi.getTarget())));

    // Processed for backward compatibility of settings
    if (ingestion.getLoader() == null) {
      ingestion.setLoader(new FileLoaderProperties(FileLoaderProperties.RemoteType.SSH,
                                                   ingestion.getBaseDir(),
                                                   ingestion.getBaseDir(),
                                                   ingestion.getHosts()));
    }

    if (query.getLoader() == null) {
      query.setLoader(new FileLoaderProperties(FileLoaderProperties.RemoteType.SSH,
                                               query.getLocalResultDir(),
                                               query.getDefaultForwardUrl(),
                                               query.getHosts()));
    }

  }

  public EngineApi getApiInfoByType(String type) {
    return api.get(type);
  }

  public EngineApi getIngestionStatusApi() {
    return api.get(GET_INGESTION_STATUS);
  }

  public EngineApi getDataSourceStatusApi() {
    return api.get(GET_DATASOURCE_STATUS);
  }

  public EngineApi getDataSourceDisableApi() {
    return api.get(DISABLE_DATASOURCE);
  }

  public EngineApi getCancelQueryApi() {
    return api.get(CANCEL_QUERY);
  }

  public String getHostnameByType(String type, boolean exceptSchema) {
    if(exceptSchema) {
      URI uri = URI.create(hostname.get(type));
      return uri.getHost() + ":" + uri.getPort();
    }

    return hostname.get(type);
  }

  public Map<String, String> getHostname() {
    return hostname;
  }

  public void setHostname(Map<String, String> hostname) {
    this.hostname = hostname;
  }

  public Map<String, EngineApi> getApi() {
    return api;
  }

  public void setApi(Map<String, EngineApi> api) {
    this.api = api;
  }

  public IngestionInfo getIngestion() {
    return ingestion;
  }

  public void setIngestion(IngestionInfo ingestion) {
    this.ingestion = ingestion;
  }

  public QueryInfo getQuery() {
    return query;
  }

  public void setQuery(QueryInfo query) {
    this.query = query;
  }

  public static class EngineApi {
    @NotNull
    private String target;
    @NotNull
    private HttpMethod method;
    @NotNull
    private String uri;

    private String targetUrl;

    public void makeTargetUrl(String target) {
      targetUrl = target + uri;
    }

    public String getTarget() {
      return target;
    }

    public void setTarget(String target) {
      this.target = target;
    }

    public HttpMethod getMethod() {
      return method;
    }

    public void setMethod(HttpMethod method) {
      this.method = method;
    }

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public String getTargetUrl() {
      return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
      this.targetUrl = targetUrl;
    }
  }


  /**
   * Information for loading the engine (Druid)
   */
  public static class IngestionInfo {

    @Deprecated
    String baseDir;

    @Deprecated
    Map<String, Host> hosts = Maps.newHashMap();

    /**
     * File processing information for loading
     */
    FileLoaderProperties loader;

    public IngestionInfo() {
    }

    public String getLocalBaseDir() {
      return getLoader().getLocalBaseDir();
    }

    /**
     * Processed for backward compatibility
     */
    public String getBaseDir() {
      if (StringUtils.isEmpty(baseDir) && loader != null) {
        return loader.getLocalBaseDir();
      }
      return baseDir;
    }

    public void setBaseDir(String baseDir) {
      this.baseDir = baseDir;
    }

    /**
     * Processed for backward compatibility
     */
    public Map<String, Host> getHosts() {
      if (MapUtils.isEmpty(hosts) && loader != null) {
        return loader.getHosts();
      }
      return hosts;
    }

    public void setHosts(Map<String, Host> hosts) {
      this.hosts = hosts;
    }

    public FileLoaderProperties getLoader() {
      return loader;
    }

    public void setLoader(FileLoaderProperties loader) {
      this.loader = loader;
    }

    @Override
    public String toString() {
      return "IngestionInfo{" +
          ", loader=" + getLoader() +
          '}';
    }
  }

  /**
   * Information for querying engines
   */
  public static class QueryInfo {

    @Deprecated
    String localResultDir;

    @Deprecated
    String defaultForwardUrl;

    @Deprecated
    Map<String, Host> hosts = Maps.newHashMap();

    /**
     * Default time zone used by the engine, default 'UTC'
     */
    String defaultTimezone;

    /**
     * Default Locale value used by engine, default 'en'
     */
    String defaultLocale;

    /**
     * Information related to file processing with broker nodes
     */
    FileLoaderProperties loader;

    public QueryInfo() {
    }

    public String getLocalBaseDir() {
      return getLoader().getLocalBaseDir();
    }

    /**
     * Processed for backward compatibility
     */
    public String getLocalResultDir() {
      if (StringUtils.isEmpty(localResultDir) && loader != null) {
        return loader.getLocalBaseDir();
      }
      return localResultDir;
    }

    public void setLocalResultDir(String localResultDir) {
      this.localResultDir = localResultDir;
    }

    /**
     * Processed for backward compatibility
     */
    public String getDefaultForwardUrl() {
      if (StringUtils.isEmpty(defaultForwardUrl) && loader != null) {
        return loader.getRemoteDir();
      }
      return defaultForwardUrl;
    }

    public void setDefaultForwardUrl(String defaultForwardUrl) {
      this.defaultForwardUrl = defaultForwardUrl;
    }

    public String getDefaultTimezone() {
      return defaultTimezone;
    }

    public void setDefaultTimezone(String defaultTimezone) {
      this.defaultTimezone = defaultTimezone;
    }

    public String getDefaultLocale() {
      return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
      this.defaultLocale = defaultLocale;
    }

    /**
     * Processed for backward compatibility
     */
    public Map<String, Host> getHosts() {
      if (MapUtils.isEmpty(hosts) && loader != null) {
        return loader.getHosts();
      }
      return hosts;
    }

    public void setHosts(Map<String, Host> hosts) {
      this.hosts = hosts;
    }

    public FileLoaderProperties getLoader() {
      return loader;
    }

    public void setLoader(FileLoaderProperties loader) {
      this.loader = loader;
    }

    @Override
    public String toString() {
      return "QueryInfo{" +
          ", defaultTimezone='" + defaultTimezone + '\'' +
          ", defaultLocale='" + defaultLocale + '\'' +
          ", loader=" + getLoader() +
          '}';
    }
  }

  public static class Host {
    // String hostname;
    Integer port;
    String username;
    String password;

    public Host() {
    }

    public Host(Integer port, String username, String password) {
      this.port = port;
      this.username = username;
      this.password = password;
    }

    public Integer getPort() {
      return port;
    }

    public void setPort(Integer port) {
      this.port = port;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public String toString() {
      return "Host{" +
          "port=" + port +
          ", username='" + username + '\'' +
          '}';
    }
  }

}
