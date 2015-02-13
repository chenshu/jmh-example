package com.chenshu.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;

public class CompressSizeTest {

    interface StreamFactory {

        public InputStream getInputStream(final InputStream in)
                throws IOException;

        public OutputStream getOutputStream(final OutputStream out)
                throws IOException;
    }

    static class JdkGzipCompress implements StreamFactory {
        int level;

        public JdkGzipCompress(int level) {
            this.level = level;
        }

        @Override
        public InputStream getInputStream(InputStream in) throws IOException {
            return new GZIPInputStream(in);
        }

        @Override
        public OutputStream getOutputStream(OutputStream out)
                throws IOException {
            return new GZIPOutputStream(out) {
                {
                    def.setLevel(level);
                }
            };
        }
    }

    static class CommonsGzipCompress implements StreamFactory {
        int level;

        public CommonsGzipCompress(int level) {
            this.level = level;
        }

        @Override
        public InputStream getInputStream(InputStream in) throws IOException {
            return new GzipCompressorInputStream(in);
        }

        @Override
        public OutputStream getOutputStream(OutputStream out)
                throws IOException {
            GzipParameters p = new GzipParameters();
            p.setCompressionLevel(level);
            return new GzipCompressorOutputStream(out, p);
        }
    }

    static class CommonsBZip2Compress implements StreamFactory {
        int blockSize;

        public CommonsBZip2Compress(int blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public InputStream getInputStream(InputStream in) throws IOException {
            return new BZip2CompressorInputStream(in);
        }

        @Override
        public OutputStream getOutputStream(OutputStream out)
                throws IOException {
            return new BZip2CompressorOutputStream(out, blockSize);
        }
    }

    static class CommonsDeflateCompress implements StreamFactory {
        int level;

        public CommonsDeflateCompress(int level) {
            this.level = level;
        }

        @Override
        public InputStream getInputStream(InputStream in) throws IOException {
            return new DeflateCompressorInputStream(in);
        }

        @Override
        public OutputStream getOutputStream(OutputStream out)
                throws IOException {
            DeflateParameters p = new DeflateParameters();
            p.setCompressionLevel(level);
            return new DeflateCompressorOutputStream(out, p);
        }
    }

    public static int compress(StreamFactory factory, byte[] src) {
        byte[] dest = null;

        ByteArrayOutputStream bout = null;
        OutputStream out = null;
        try {
            bout = new ByteArrayOutputStream(src.length);
            out = factory.getOutputStream(bout);
            out.write(src);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bout != null) {
                try {
                    bout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] bs = bout.toByteArray();
        ByteArrayInputStream bin = null;
        InputStream in = null;
        ByteArrayOutputStream os = null;
        try {
            bin = new ByteArrayInputStream(bs);
            in = factory.getInputStream(bin);
            dest = new byte[src.length];
            os = new ByteArrayOutputStream(src.length);
            int count = 0;
            while ((count = in.read(dest)) != -1) {
                os.write(dest, 0, count);
            }
            dest = os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bin != null) {
                try {
                    bin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(src.length == dest.length);
        System.out.println(Arrays.equals(src, dest));
        System.out.println(new String(src, Charset.forName("UTF-8"))
                .equals(new String(dest, Charset.forName("UTF-8"))));
        return bs.length;
    }

    public static void main(String[] args) throws Exception {
        byte[] data = Files.readAllBytes(Paths
                .get("./THIRDPARTYLICENSEREADME.txt"));
        for (int level = 1; level < 10; level++) {
            System.out.println("JDK Gzip\t" + level + " "
                    + compress(new JdkGzipCompress(level), data));
            System.out.println("COMMONS Gzip\t" + level + " "
                    + compress(new CommonsGzipCompress(level), data));
            System.out.println("COMMONS BZip2\t" + level + " "
                    + compress(new CommonsBZip2Compress(level), data));
            System.out.println("COMMONS Deflate\t" + level + " "
                    + compress(new CommonsDeflateCompress(level), data));
        }
    }

}
