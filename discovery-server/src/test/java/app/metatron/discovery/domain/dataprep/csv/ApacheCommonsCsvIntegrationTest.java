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

package app.metatron.discovery.domain.dataprep.csv;

import io.prestosql.jdbc.$internal.jackson.core.JsonProcessingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import app.metatron.discovery.AbstractRestIntegrationTest;
import app.metatron.discovery.core.oauth.OAuthTestExecutionListener;
import app.metatron.discovery.domain.dataprep.PrepProperties;
import app.metatron.discovery.domain.dataprep.exceptions.PrepErrorCodes;
import app.metatron.discovery.domain.dataprep.exceptions.PrepException;
import app.metatron.discovery.domain.dataprep.exceptions.PrepMessageKey;
import app.metatron.discovery.domain.dataprep.file.PrepCsvUtil;
import app.metatron.discovery.domain.dataprep.file.PrepFileUtil;
import app.metatron.discovery.domain.dataprep.file.PrepParseResult;
import app.metatron.discovery.domain.dataprep.teddy.DataFrame;

import static org.junit.Assert.assertNull;

@TestExecutionListeners(value = OAuthTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ApacheCommonsCsvIntegrationTest extends AbstractRestIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApacheCommonsCsvIntegrationTest.class);

  @Autowired(required = false)
  PrepProperties prepProperties;

  String strHdfsUriCrime;

  @Before
  public void setUp() {
    strHdfsUriCrime = prepareCsvOnHdfs("csv/crime.csv");
  }

  private Configuration getHadoopConf() {
    Configuration hadoopConf = new Configuration();
    String hadoopConfDir = prepProperties.getHadoopConfDir(true);

    hadoopConf.addResource(new Path(hadoopConfDir + File.separator + "core-site.xml"));
    hadoopConf.addResource(new Path(hadoopConfDir + File.separator + "hdfs-site.xml"));

    return hadoopConf;
  }

  private String prepareCsvOnHdfs(String localRelPath) {
    String strLocalUri = ApacheCommonsCsvInputTest.buildStrUrlFromResourceDir("csv/crime.csv");
    String strHdfsUri = String.format("%s/test_output/%s", prepProperties.getStagingBaseDir(true), localRelPath);
    FSDataOutputStream hos;
    FileSystem hdfsFs;
    Reader reader;

    PrepParseResult result = new PrepParseResult();
    reader = PrepFileUtil.getReader(strLocalUri, null, "bdl", false, result);

    try {
      URI uri = new URI(strHdfsUri);
      Path path = new Path(uri);
      hdfsFs = FileSystem.get(getHadoopConf());
      hos = hdfsFs.create(path);

      org.apache.commons.io.IOUtils.copy(reader, hos);

      hos.close();
      reader.close();
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw PrepException
              .create(PrepErrorCodes.PREP_DATASET_ERROR_CODE, PrepMessageKey.MSG_DP_ALERT_MALFORMED_URI_SYNTAX,
                      strHdfsUri);
    } catch (IOException e) {
      e.printStackTrace();
      throw PrepException
              .create(PrepErrorCodes.PREP_DATASET_ERROR_CODE, PrepMessageKey.MSG_DP_ALERT_CANNOT_WRITE_TO_HDFS_PATH,
                      strHdfsUri);
    }

    return strHdfsUri;
  }

  @Test
  public void test_hdfs() throws JsonProcessingException {
    PrepParseResult result = PrepCsvUtil.DEFAULT.withHadoopConf(getHadoopConf()).parse(strHdfsUriCrime);

    assertNull(result.colNames);

    DataFrame df = new DataFrame();
    df.setByGrid(result.grid, result.colNames);
    df.show();
  }

  @Test
  public void test_hdfs_header() throws JsonProcessingException {
    PrepParseResult result = PrepCsvUtil.DEFAULT
            .withHeader(true)
            .withHadoopConf(getHadoopConf())
            .parse(strHdfsUriCrime);
    LOGGER.debug("colNames={}", result.colNames);
    LOGGER.debug("colCnt={}", result.colNames.size());

    DataFrame df = new DataFrame();
    df.setByGrid(result);
    df.show();
  }
}
