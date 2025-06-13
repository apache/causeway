/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.commons.internal.debug;

import lombok.SneakyThrows;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.concurrent.Callable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Memory Usage Utility
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 3.4.0
 */
public record _MemoryUsage(long usedInKibiBytes) {
    
    // -- UTILITIES
    
    static int indent = 0;

    @SneakyThrows
    public static <T> T measureMetaspace(final String desc, final Callable<T> runnable) {
        var before = metaspace();
        try {
            indent++;
            return runnable.call();
        } finally {
            var after = metaspace();
            System.out.printf("%s%s : %s%n", spaces(indent), after.minus(before), desc);
            indent--;
        }
    }
    
    public static void measureMetaspace(String desc, final Runnable runnable) {
        var before = metaspace();
        try {
            runnable.run();
        } finally {
            var after = metaspace();
            System.out.printf("%s : %s%n", after.minus(before), desc);
        }
    }
    
    // -- FACTORY
    
    private static _MemoryUsage metaspace() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().contains("Metaspace")) {
                return new _MemoryUsage(pool.getUsage());
            }
        }
        throw new RuntimeException("Metaspace Usage not found");
    }
    
    // -- NON CANONICAL CONSTRUCTOR

    private _MemoryUsage(java.lang.management.MemoryUsage usage) {
        this(usage.getUsed() / 1024);
    }

    @Override
    public String toString() {
        return String.format("%,d KiB", usedInKibiBytes);
    }

    // -- HELPER

    private static String spaces(int indent) {
        return " ".repeat(indent * 2);
    }

    private _MemoryUsage minus(_MemoryUsage before) {
        return new _MemoryUsage(this.usedInKibiBytes - before.usedInKibiBytes);
    }

}
