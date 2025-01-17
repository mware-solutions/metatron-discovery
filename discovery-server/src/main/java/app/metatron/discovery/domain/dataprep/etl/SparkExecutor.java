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

package app.metatron.discovery.domain.dataprep.etl;

import static app.metatron.discovery.domain.dataprep.PrepProperties.ETL_SPARK_PORT;
import static app.metatron.discovery.domain.dataprep.PrepProperties.HADOOP_CONF_DIR;

import app.metatron.discovery.common.GlobalObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import app.metatron.discovery.domain.dataprep.util.PrepUtil;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class SparkExecutor {

  private static Logger LOGGER = LoggerFactory.getLogger(SparkExecutor.class);

  @Value("${spark.driver-host}")
  private String driverHost;

  @Async("prepThreadPoolTaskExecutor")
  public Future<String> run(String[] argv) throws IOException {
    LOGGER.debug("runSpark(): start");

    Map<String, Object> prepPropertiesInfo = GlobalObjectMapper.readValue(argv[0], HashMap.class);
    Map<String, Object> datasetInfo = GlobalObjectMapper.readValue(argv[1], HashMap.class);
    Map<String, Object> snapshotInfo = GlobalObjectMapper.readValue(argv[2], HashMap.class);
    Map<String, Object> callbackInfo = GlobalObjectMapper.readValue(argv[3], HashMap.class);

    String storedUri = (String) datasetInfo.get("storedUri");
    if (storedUri.startsWith("s3a://")) {
      String hadoopConfDir = (String) prepPropertiesInfo.get(HADOOP_CONF_DIR);
      Configuration conf = PrepUtil.getHadoopConf(hadoopConfDir);

      datasetInfo.put("fs.s3a.endpoint", conf.get("fs.s3a.endpoint"));
      datasetInfo.put("fs.s3a.access.key", conf.get("fs.s3a.access.key"));
      datasetInfo.put("fs.s3a.secret.key", conf.get("fs.s3a.secret.key"));
      datasetInfo.put("spark.driverHost", driverHost);

      if (datasetInfo.containsKey("upstreamDatasetInfos")) {
        List<Map<String, Object>> upstreamDatasetInfos = (List<Map<String, Object>>) datasetInfo.get("upstreamDatasetInfos");
        upstreamDatasetInfos.forEach((map) -> {
          String uStoredUri = (String) map.get("storedUri");
          if (uStoredUri.startsWith("s3a://")) {
            map.put("fs.s3a.endpoint", conf.get("fs.s3a.endpoint"));
            map.put("fs.s3a.access.key", conf.get("fs.s3a.access.key"));
            map.put("fs.s3a.secret.key", conf.get("fs.s3a.secret.key"));
            map.put("spark.driverHost", driverHost);
          }
        });
      }
    }

    String ssName = (String) snapshotInfo.get("ssName");
    String ssType = (String) snapshotInfo.get("ssType");
    LOGGER.debug("runSpark(): ssName={} ssType={}" + ssName, ssType);

    // Send spark request
    Map<String, Object> args = new HashMap();

    args.put("prepProperties", prepPropertiesInfo);
    args.put("datasetInfo", datasetInfo);
    args.put("snapshotInfo", snapshotInfo);
    args.put("callbackInfo", callbackInfo);

    URL url = new URL("http://localhost:" + prepPropertiesInfo.get(ETL_SPARK_PORT) + "/run");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();

    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json; utf-8");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);

    String jsonArgs = GlobalObjectMapper.getDefaultMapper().writeValueAsString(args);

    try (OutputStream os = con.getOutputStream()) {
      byte[] input = jsonArgs.getBytes("utf-8");
      os.write(input, 0, input.length);
    }

    StringBuilder response = new StringBuilder();
    InputStreamReader reader = new InputStreamReader(con.getInputStream(), "utf-8");

    try (BufferedReader br = new BufferedReader(reader)) {
      String responseLine;

      while (true) {
        responseLine = br.readLine();
        if (responseLine == null) {
          break;
        }
        response.append(responseLine.trim());
      }
      LOGGER.debug("runSpark(): response: " + response.toString());
      br.readLine();
    }

    LOGGER.debug("runSpark(): done with statusCode " + con.getResponseCode());
    return new AsyncResult<>(response.toString());
  }

}
