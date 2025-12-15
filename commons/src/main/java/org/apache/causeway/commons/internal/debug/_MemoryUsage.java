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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.causeway.commons.functional.ThrowingRunnable;

import lombok.SneakyThrows;

/**
 * <h1>- internal use only -</h1>
 *
 * <p>Memory Usage Utility
 *
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @implNote heap-space measurement relies on GC actually running before taking the measurements;
 *  {@link System#gc()} is the only hint we can provide to the GC, but have no control over what it
 *  is doing; However, I was comparing measurements of a large 200MB+ object graph using
 *  {@code GraphLayout.parseInstance(obj).totalSize()} from <pre>{@code
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.17</version>
</dependency>
 *  }</pre>
 * and the error with {@link _MemoryUsage} was surprisingly low {@code <1%}.
 *
 * @since 3.4.0
 */
public record _MemoryUsage(
		long metaspaceUsed,
		long heapUsed
		) {

    // -- UTILITIES

	@SneakyThrows
	public static <T> T measure(final Callable<T> callable, final Consumer<_MemoryUsage> callback) {
		System.gc();
        var before = metaspace();
        T result = callable.call();
    	System.gc();
        var after = metaspace();
        callback.accept(after.minus(before));
        return result;
	}

    public static _MemoryUsage measure(final ThrowingRunnable runnable) {
    	System.gc();
        var before = metaspace();
        var after = (_MemoryUsage) null;
        try {
            runnable.run();
        } catch (Throwable e) {
        } finally {
        	System.gc();
            after = metaspace();
        }
        return after.minus(before);
    }

    // -- FACTORY

    private static _MemoryUsage metaspace() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().contains("Metaspace"))
				return new _MemoryUsage(pool.getUsage());
        }
        throw new RuntimeException("Metaspace Usage not found");
    }

    // -- NON CANONICAL CONSTRUCTOR

    private _MemoryUsage(final java.lang.management.MemoryUsage usage) {
        this(usage.getUsed(),
        		Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    @Override
    public String toString() {
        return "metaspace: %.3f MB, heap: %.3f MB".formatted(
        		0.000_001 * metaspaceUsed,
        		0.000_001 * heapUsed
        		);
    }

    // -- HELPER

    private _MemoryUsage minus(final _MemoryUsage other) {
        return new _MemoryUsage(this.metaspaceUsed - other.metaspaceUsed, this.heapUsed - other.heapUsed);
    }

}
