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
import java.util.concurrent.TimeUnit;
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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@BenchmarkMode({ Mode.Throughput })
@OutputTimeUnit(TimeUnit.SECONDS)
public class CompressTest {

    private byte[] src;
    private byte[] data_jdk_gz;
    private byte[] data_commons_gz;
    private byte[] data_commons_bz2;
    private byte[] data_commons_deflate;

    // public static final int BEST_COMPRESSION 9
    // public static final int BEST_SPEED 1
    // public static final int DEFAULT_COMPRESSION -1
    // public static final int DEFAULT_STRATEGY 0
    // public static final int DEFLATED 8
    // public static final int FILTERED 1
    // public static final int HUFFMAN_ONLY 2
    // public static final int NO_COMPRESSION 0

    @Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9" })
    protected int level;

    @Setup
    public void prepare() {
        try {
            src = Files
                    .readAllBytes(Paths.get("./THIRDPARTYLICENSEREADME.txt"));
            data_jdk_gz = getJdkGzipCompressContent();
            data_commons_gz = getCommonsGzipCompressContent();
            data_commons_bz2 = getCommonsBZip2CompressContent();
            data_commons_deflate = getCommonsDeflateCompressContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface StreamFactory {

        public InputStream getInputStream(final InputStream in)
                throws IOException;

        public OutputStream getOutputStream(final OutputStream out)
                throws IOException;
    }

    private byte[] getCompressContent(StreamFactory factory) {
        ByteArrayOutputStream bout = null;
        OutputStream out = null;
        try {
            bout = new ByteArrayOutputStream(src.length);
            out = factory.getOutputStream(bout);
            out.write(src);
        } catch (Exception e) {
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
        return bout.toByteArray();
    }

    private byte[] getDecompressContent(StreamFactory factory, byte[] data) {
        byte[] dest = null;
        ByteArrayInputStream bin = null;
        InputStream in = null;
        ByteArrayOutputStream os = null;
        try {
            bin = new ByteArrayInputStream(data);
            in = factory.getInputStream(bin);
            dest = new byte[src.length];
            // in.read(dest);
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

        assert src.length == dest.length;
        assert Arrays.equals(src, dest);
        assert new String(src, Charset.forName("UTF-8")).equals(new String(
                dest, Charset.forName("UTF-8")));
        return dest;
    }

    private int compress(StreamFactory factory) {
        return getCompressContent(factory).length;
    }

    private int decompress(StreamFactory factory, byte[] data) {
        return getDecompressContent(factory, data).length;
    }

    class JdkGzipCompress implements StreamFactory {
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

    private byte[] getJdkGzipCompressContent() {
        return getCompressContent(new JdkGzipCompress());
    }

    @Benchmark
    public int jdkGzipCompress() {
        return compress(new JdkGzipCompress());
    }

    @Benchmark
    public int jdkGzipDecompress() {
        return decompress(new JdkGzipCompress(), data_jdk_gz);
    }

    class CommonsGzipCompress implements StreamFactory {
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

    private byte[] getCommonsGzipCompressContent() {
        return getCompressContent(new CommonsGzipCompress());
    }

    @Benchmark
    public int commonsGzipCompress() {
        return compress(new CommonsGzipCompress());
    }

    @Benchmark
    public int commonsGzipDecompress() {
        return decompress(new CommonsGzipCompress(), data_commons_gz);
    }

    class CommonsBZip2Compress implements StreamFactory {
        @Override
        public InputStream getInputStream(InputStream in) throws IOException {
            return new BZip2CompressorInputStream(in);
        }

        @Override
        public OutputStream getOutputStream(OutputStream out)
                throws IOException {
            return new BZip2CompressorOutputStream(out, level);
        }
    }

    private byte[] getCommonsBZip2CompressContent() {
        return getCompressContent(new CommonsBZip2Compress());
    }

    @Benchmark
    public int commonsBZip2Compress() {
        return compress(new CommonsBZip2Compress());
    }

    @Benchmark
    public int commonsBZip2Decompress() {
        return decompress(new CommonsBZip2Compress(), data_commons_bz2);
    }

    class CommonsDeflateCompress implements StreamFactory {
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

    private byte[] getCommonsDeflateCompressContent() {
        return getCompressContent(new CommonsDeflateCompress());
    }

    @Benchmark
    public int commonsDeflateCompress() {
        return compress(new CommonsDeflateCompress());
    }

    @Benchmark
    public int commonsDeflateDecompress() {
        return decompress(new CommonsDeflateCompress(), data_commons_deflate);
    }

    /**
     * 
     * java -jar target/benchmarks.jar CompressTest -wi 5 -i 5 -f 1
     * 
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CompressTest.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
