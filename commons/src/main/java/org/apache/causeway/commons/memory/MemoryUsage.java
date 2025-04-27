package org.apache.causeway.commons.memory;

import lombok.SneakyThrows;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.concurrent.Callable;

public class MemoryUsage {

    private MemoryUsage(java.lang.management.MemoryUsage usage) {
        this(usage.getUsed() / 1024);
    }

    private MemoryUsage(final long usedInKb) {
        this.usedInKb = usedInKb;
    }

    final long usedInKb;

    @Override
    public String toString() {
        return String.format("%,d KB", usedInKb);
    }

    public static void measureMetaspace(String desc, final Runnable runnable) {
        MemoryUsage before = metaspace();
        try {
            runnable.run();
        } finally {
            MemoryUsage after = metaspace();
            System.out.printf("%s : %s%n", after.minus(before), desc);
        }
    }

    static int indent = 0;

    @SneakyThrows
    public static <T> T measureMetaspace(final String desc, final Callable<T> runnable) {
        MemoryUsage before = metaspace();
        try {
            indent++;
            return runnable.call();
        } finally {
            MemoryUsage after = metaspace();
            System.out.printf("%s%s : %s%n", spaces(indent), after.minus(before), desc);
            indent--;
        }
    }

    private static String spaces(int indent) {
        return " ".repeat(indent * 2);
    }

    private MemoryUsage minus(MemoryUsage before) {
        return new MemoryUsage(this.usedInKb - before.usedInKb);
    }

    public static MemoryUsage metaspace() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().contains("Metaspace")) {
                return new MemoryUsage(pool.getUsage());
            }
        }
        throw new RuntimeException("Metaspace Usage not found");
    }
}
