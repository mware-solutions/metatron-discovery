package app.metatron.discovery.domain.dataprep.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

import java.io.IOException;
import java.io.InputStream;

public class HdfsUtil {
    public static FileSystem get(Configuration conf, String user) throws IOException {
        try {
            return FileSystem.get(FileSystem.getDefaultUri(conf), conf, user);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static InputStream openFile(FileSystem fs, Path path) throws IOException {
        CompressionCodecFactory factory = new CompressionCodecFactory(fs.getConf());
        CompressionCodec codec = factory.getCodec(path);
        if (codec != null) {
            return codec.createInputStream(fs.open(path));
        } else {
            return fs.open(path);
        }
    }
}
