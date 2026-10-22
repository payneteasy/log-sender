package com.payneteasy.logsender.application;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.posix.PosixAPI;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

/**
 * ASM is pinned in the parent pom (CVE fixes). jnr-ffi (chronicle-queue -> net.openhft:posix -> jnr-ffi)
 * uses ASM at runtime to generate native call stubs, so check that this still works with the pinned ASM.
 * The native checks run on Linux (GitHub Actions CI).
 */
public class NativeAsmCompatibilityTest {

    public interface LibC {
        int getpid();

        long strlen(String s);
    }

    @Test
    public void asmFamilyIsPatched() {
        // CVE-2026-110257..110263, CVE-2026-110270, CVE-2026-110591 are fixed in 9.11, CVE-2026-112046 in 9.11.1
        Class<?>[] asmClasses = {
                ClassReader.class,                                   // asm
                org.objectweb.asm.commons.Remapper.class,            // asm-commons
                org.objectweb.asm.tree.ClassNode.class,              // asm-tree
                org.objectweb.asm.tree.analysis.Analyzer.class,      // asm-analysis
                org.objectweb.asm.util.Textifier.class               // asm-util
        };
        for (Class<?> asmClass : asmClasses) {
            String version = asmClass.getPackage().getImplementationVersion();
            Assert.assertNotNull("no implementation version for " + asmClass.getName(), version);
            Assert.assertTrue(asmClass.getName() + " comes from ASM " + version + ", expected 9.11.1+",
                    compareVersions(version, "9.11.1") >= 0);
        }
    }

    @Test
    public void jnrFfiGeneratesAsmStubs() {
        assumeLinux();
        Assert.assertNotEquals("jnr-ffi native runtime is not available",
                "jnr.ffi.provider.InvalidRuntime", jnr.ffi.Runtime.getSystemRuntime().getClass().getName());

        LibC libc = LibraryLoader.create(LibC.class)
                .library(Platform.getNativePlatform().getStandardCLibraryName())
                .load();

        // jnr-ffi names ASM generated classes "<iface>$jnr$ffi$N"; the reflection fallback uses java.lang.reflect.Proxy
        Assert.assertTrue("jnr-ffi did not use the ASM stub generator: " + libc.getClass().getName(),
                libc.getClass().getName().contains("$jnr$ffi$"));
        Assert.assertEquals(currentPid(), libc.getpid());
        Assert.assertEquals(5L, libc.strlen("hello"));
    }

    @Test
    public void openhftPosixUsesJnr() {
        assumeLinux();

        PosixAPI posix = PosixAPI.posix();
        // PosixAPIHolder silently falls back to another implementation if jnr-ffi fails to load
        Assert.assertNotNull("PosixAPI is not available", posix);
        Assert.assertEquals("net.openhft.posix.internal.jnr.JNRPosixAPI", posix.getClass().getName());
        Assert.assertEquals(currentPid(), posix.getpid());

        long address = posix.malloc(64);
        Assert.assertNotEquals(0L, address);
        posix.free(address);
    }

    @Test
    public void chronicleQueueRoundTrip() throws Exception {
        File dir = Files.createTempDirectory("log-sender-queue").toFile();
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.single(dir).build()) {
            ExcerptAppender appender = queue.acquireAppender();
            appender.writeText("hello");

            ExcerptTailer tailer = queue.createTailer();
            Assert.assertEquals("hello", tailer.readText());
        } finally {
            deleteRecursively(dir);
        }
    }

    /**
     * jffi 1.3.5 (used by jnr-ffi 2.2.6) has no working native stub for every platform (e.g. macOS on Apple Silicon
     * gets jnr-ffi's InvalidRuntime regardless of the ASM version), so the native checks run on Linux, i.e. in CI.
     */
    private static void assumeLinux() {
        Assume.assumeTrue("native jnr-ffi checks run on Linux only",
                Platform.getNativePlatform().getOS() == Platform.OS.LINUX);
    }

    private static int compareVersions(String left, String right) {
        String[] l = left.split("\\.");
        String[] r = right.split("\\.");
        for (int i = 0; i < Math.max(l.length, r.length); i++) {
            int a = i < l.length ? Integer.parseInt(l[i]) : 0;
            int b = i < r.length ? Integer.parseInt(r[i]) : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }

    private static int currentPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.substring(0, name.indexOf('@')));
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
