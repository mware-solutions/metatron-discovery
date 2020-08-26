package app.metatron.discovery.domain.dataprep.file;

import app.metatron.discovery.domain.dataprep.util.HdfsUtil;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static app.metatron.discovery.domain.dataprep.PrepProperties.HADOOP_CONF_DIR;
import static app.metatron.discovery.domain.dataprep.exceptions.PrepMessageKey.*;
import static app.metatron.discovery.domain.dataprep.util.PrepUtil.*;

public class PrepFileUtil {

  private static Logger LOGGER = LoggerFactory.getLogger(PrepFileUtil.class);

  private static InputStreamReader getReaderWithCharset(InputStream is, String strUri, String charset) {
    InputStreamReader reader;

    try {
      reader = new InputStreamReader(new BOMInputStream(is), charset);
    } catch (IOException e) {
      e.printStackTrace();
      throw datasetError(MSG_DP_ALERT_FAILED_TO_READ_CSV, String.format("%s (charset: %s)", strUri, charset));
    }

    return reader;
  }

  // Almost same to CommonsCsvProcessor.detectingCharset()
  // We have to unify both codes.
  private static String detectingCharset(InputStream is, String strUri) {

    CharsetDetector detector;
    CharsetMatch match;

    try {
      byte[] byteData = new byte[1000];
      is.read(byteData);
      is.close();

      detector = new CharsetDetector();

      detector.setText(byteData);
      match = detector.detect();
      String charset = match.getName();

      if (!Charset.isSupported(charset)) {
        throw datasetError(MSG_DP_ALERT_UNSUPPORTED_CHARSET, charset);
      }

      return charset;
    } catch (IOException e) {
      throw datasetError(MSG_DP_ALERT_FAILED_TO_READ_CSV, strUri);
    }

  }

  public static Reader getReader(String strUri, Configuration conf, String hdfsUser, boolean onlyCount, PrepParseResult result) {
    Reader reader;
    URI uri;
    String charset;

    try {
      uri = new URI(strUri);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw datasetError(MSG_DP_ALERT_MALFORMED_URI_SYNTAX, strUri);
    }

    switch (uri.getScheme()) {
      case "hdfs":
      case "s3a":
        if (conf == null) {
          throw configError(MSG_DP_ALERT_REQUIRED_PROPERTY_MISSING, HADOOP_CONF_DIR);
        }
        Path path = new Path(uri);

        FileSystem hdfsFs;
        try {
          hdfsFs = HdfsUtil.get(conf, hdfsUser);
        } catch (IOException e) {
          e.printStackTrace();
          throw datasetError(MSG_DP_ALERT_CANNOT_GET_HDFS_FILE_SYSTEM, strUri);
        }

        InputStream his;
        InputStream dhis;
        try {
          if (onlyCount) {
            ContentSummary cSummary = hdfsFs.getContentSummary(path);
            result.totalBytes = cSummary.getLength();
          }

          his = HdfsUtil.openFile(hdfsFs, path);
          dhis = HdfsUtil.openFile(hdfsFs, path);

        } catch (IOException e) {
          e.printStackTrace();
          throw datasetError(MSG_DP_ALERT_CANNOT_READ_FROM_HDFS_PATH, strUri);
        }

        charset = detectingCharset(dhis, strUri);
        reader = getReaderWithCharset(his, strUri, charset);
        break;

      case "file":
        File file = new File(uri);
        if (onlyCount) {
          result.totalBytes = file.length();
        }

        FileInputStream fis;
        FileInputStream dfis;
        try {
          fis = new FileInputStream(file);
          dfis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw datasetError(MSG_DP_ALERT_CANNOT_READ_FROM_LOCAL_PATH, strUri);
        }

        charset = detectingCharset(dfis, strUri);
        reader = getReaderWithCharset(fis, strUri, charset);
        break;

      default:
        throw datasetError(MSG_DP_ALERT_UNSUPPORTED_URI_SCHEME, strUri);
    }

    return reader;
  }

  // public for tests
  public static OutputStreamWriter getWriterUtf8(OutputStream os) {
    OutputStreamWriter writer;
    String charset = "UTF-8";

    try {
      writer = new OutputStreamWriter(os, charset);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw datasetError(MSG_DP_ALERT_UNSUPPORTED_CHARSET, charset);
    }

    return writer;
  }

  public static Writer getWriter(String strUri, Configuration conf, String hdfsUser) {
    Writer writer;
    URI uri;

    try {
      uri = new URI(strUri);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw snapshotError(MSG_DP_ALERT_MALFORMED_URI_SYNTAX, strUri);
    }

    switch (uri.getScheme()) {
      case "hdfs":
        if (conf == null) {
          throw configError(MSG_DP_ALERT_REQUIRED_PROPERTY_MISSING, HADOOP_CONF_DIR);
        }
        Path path = new Path(uri);

        FileSystem hdfsFs;
        try {
          hdfsFs = HdfsUtil.get(conf, hdfsUser);
        } catch (IOException e) {
          e.printStackTrace();
          throw snapshotError(MSG_DP_ALERT_CANNOT_GET_HDFS_FILE_SYSTEM, strUri);
        }

        FSDataOutputStream hos;
        try {
          hos = hdfsFs.create(path);
        } catch (IOException e) {
          e.printStackTrace();
          throw snapshotError(MSG_DP_ALERT_CANNOT_WRITE_TO_HDFS_PATH, strUri);
        }

        writer = getWriterUtf8(hos);
        break;

      case "file":
        File file = new File(uri);
        File dirParent = file.getParentFile();
        if (dirParent == null) {
          throw snapshotError(MSG_DP_ALERT_CANNOT_WRITE_TO_LOCAL_PATH, strUri);
        }
        if (!dirParent.exists()) {
          if (!dirParent.mkdirs()) {
            throw snapshotError(MSG_DP_ALERT_CANNOT_WRITE_TO_LOCAL_PATH, strUri);
          }
        }

        FileOutputStream fos;
        try {
          fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          throw snapshotError(MSG_DP_ALERT_CANNOT_READ_FROM_LOCAL_PATH, strUri);
        }

        writer = getWriterUtf8(fos);
        break;

      default:
        throw snapshotError(MSG_DP_ALERT_UNSUPPORTED_URI_SCHEME, strUri);
    }

    return writer;
  } // end of getWriter()
}
