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

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;

import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utility for adding temporary debug code,
 * that needs to be removed later. Also integrates with {@link XrayUi}, if enabled.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 *
 * @deprecated not deprecated, but marked a such,
 * to indicate that any call to this class is temporary for debugging purposes
 * and should be removed ultimately
 */
@Deprecated(forRemoval = false) // do not remove, see java-doc
@UtilityClass
public class _Debug {

    @Deprecated(forRemoval = false) // do not remove, see java-doc
	public void onCondition(
            final boolean condition,
            final Runnable runnable) {

        if(condition) {
            runnable.run();
        }
    }

    @Deprecated(forRemoval = false) // do not remove, see java-doc
	public void onClassSimpleNameMatch(
            final Class<?> correspondingClass,
            final String classSimpleName,
            final Runnable runnable) {
        onCondition(correspondingClass.getSimpleName().equals(classSimpleName), runnable);
    }

    @Deprecated(forRemoval = false) // do not remove, see java-doc
	public void dump(final Object x) {
        dump(x, 0);
    }

    /**
     * General purpose log entry.
     */
    @Deprecated(forRemoval = false) // do not remove, see java-doc
	public void log(final String format, final Object...args) {
        _XrayEvent.record(1, _IconResource.LOG, format, args);
    }

    public record Profiler(
    		Map<String, Measurement> measurements) {

    	public record Measurement(
    			String name,
    			LongSummaryStatistics stats) {
    		Measurement(final String name) {
    			this(name, new LongSummaryStatistics());
    		}
    		void collect(final Runnable runnable) {
    			var t0 = System.nanoTime();
    			runnable.run();
    			stats.accept(System.nanoTime() - t0);
    		}
    		<T> T collect(final Supplier<T> callable) {
    			var t0 = System.nanoTime();
    			var t = callable.get();
    			stats.accept(System.nanoTime() - t0);
    			return t;
    		}
    		@Override
    		public final String toString() {
    			return "Profiling %s: %d ms, avg %.2f ms (count=%d)"
    					.formatted(name,
    							stats.getSum()/1000_000L,
    							stats.getAverage()/1000_000.,
    							stats.getCount());
    		}
    	}

    	public Profiler() {
    		this(new HashMap<>());
    	}

    	public void measure(final String name, final Runnable runnable) {
    		measurements.computeIfAbsent(name, Measurement::new)
    			.collect(runnable);
    	}

    	public <T> T measure(final String name, final Supplier<T> callable) {
    		return measurements.computeIfAbsent(name, Measurement::new)
    			.collect(callable);
    	}

    	@Override
    	public final String toString() {
    		return new TreeMap<>(measurements).values().stream()
    			.map(Measurement::toString)
    			.collect(Collectors.joining("\n"));
    	}
    }

    // -- HELPER

    private void dump(Object x, final int indent) {
        if(x instanceof Iterable) {
            _NullSafe.streamAutodetect(x)
            .forEach(element->dump(element, indent+1));
            return;
        }
        if(x!=null
                && x.getClass().isArray()) {

            var array = _NullSafe.streamAutodetect(x)
            .map(e->""+e)
            .collect(Collectors.joining(", "));

            x = String.format("[%s]", array);
        }

        if(indent==0) {
            System.err.printf("%s%n", x);
        } else {
            var suffix = _Strings.padEnd("", indent, '-');
            System.err.printf("%s %s%n", suffix, x);
        }

    }

}
