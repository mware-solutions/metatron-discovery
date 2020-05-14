package app.metatron.discovery.domain.dataprep.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

public class HdfsUtil {
    public static FileSystem get(Configuration conf, String user) throws IOException {
        try {
            return FileSystem.get(FileSystem.getDefaultUri(conf), conf, user);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
