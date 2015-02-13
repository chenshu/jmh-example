package com.chenshu.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
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
public class CompressOldTest {

    private byte[] data;

    // public static final int BEST_COMPRESSION 9
    // public static final int BEST_SPEED 1
    // public static final int DEFAULT_COMPRESSION -1
    // public static final int DEFAULT_STRATEGY 0
    // public static final int DEFLATED 8
    // public static final int FILTERED 1
    // public static final int HUFFMAN_ONLY 2
    // public static final int NO_COMPRESSION 0

    @Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9" })
    private int level;

    @Setup
    public void prepare() {
        try {
            data = Files.readAllBytes(Paths
                    .get("./THIRDPARTYLICENSEREADME.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public int jdkGzipCompress() {
        ByteArrayOutputStream bout = null;
        GZIPOutputStream gzout = null;
        try {
            bout = new ByteArrayOutputStream(data.length);
            gzout = new GZIPOutputStream(bout) {
                {
                    def.setLevel(level);
                }
            };
            gzout.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (gzout != null) {
                try {
                    gzout.close();
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
        return bs.length;
    }

    @Benchmark
    public int jdkGzip2Compress() {
        ByteArrayInputStream bin = null;
        ByteArrayOutputStream bout = null;
        GZIPOutputStream gzout = null;
        try {
            bin = new ByteArrayInputStream(data);
            bout = new ByteArrayOutputStream(data.length);
            gzout = new GZIPOutputStream(bout) {
                {
                    def.setLevel(level);
                }
            };
            int count;
            byte ret[] = new byte[1024];
            while ((count = bin.read(ret, 0, 1024)) != -1) {
                gzout.write(ret, 0, count);
            }
            gzout.finish();
            gzout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (gzout != null) {
                try {
                    gzout.close();
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
        return bs.length;
    }

    @Benchmark
    public int commonsGzipCompress() {
        ByteArrayOutputStream bout = null;
        GzipCompressorOutputStream gzout = null;
        try {
            GzipParameters p = new GzipParameters();
            p.setCompressionLevel(level);
            bout = new ByteArrayOutputStream(data.length);
            gzout = new GzipCompressorOutputStream(bout, p);
            gzout.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzout != null) {
                try {
                    gzout.close();
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
        return bs.length;
    }

    @Benchmark
    public int commonsBZip2Compress() {
        ByteArrayOutputStream bout = null;
        BZip2CompressorOutputStream bzip2out = null;
        try {
            bout = new ByteArrayOutputStream(data.length);
            bzip2out = new BZip2CompressorOutputStream(bout, level);
            bzip2out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bzip2out != null) {
                try {
                    bzip2out.close();
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
        return bs.length;
    }

    @Benchmark
    public int commonsDeflateCompress() {
        ByteArrayOutputStream bout = null;
        DeflateCompressorOutputStream dout = null;
        try {
            DeflateParameters p = new DeflateParameters();
            p.setCompressionLevel(level);
            bout = new ByteArrayOutputStream(data.length);
            dout = new DeflateCompressorOutputStream(bout, p);
            dout.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dout != null) {
                try {
                    dout.close();
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
        return bs.length;
    }

    /**
     * 
     * java -jar target/benchmarks.jar CompressTest -wi 5 -i 5 -f 1
     * 
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CompressOldTest.class.getSimpleName())
                .warmupIterations(5).measurementIterations(5).forks(1).build();

        new Runner(opt).run();
    }

}
