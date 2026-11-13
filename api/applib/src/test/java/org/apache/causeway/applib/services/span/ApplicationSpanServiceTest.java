/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.applib.services.span;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationSpanServiceTest {
    @Test void standaloneNoopPreservesResultsAndInvocationCountsWithoutValidation() {
        var calls = new AtomicInteger();
        var result = new Object();
        assertSame(result, ApplicationSpanService.NOOP.call(null, () -> { calls.incrementAndGet(); return result; }));
        ApplicationSpanService.NOOP.run("", calls::incrementAndGet);
        assertNull(ApplicationSpanService.NOOP.call("load", () -> null));
        assertEquals(2, calls.get());
    }

    @Test void standaloneNoopRethrowsOriginalCheckedRuntimeAndErrorFailures() {
        for (Throwable failure : new Throwable[] { new IOException("checked"), new IllegalStateException("runtime"), new AssertionError("error") }) {
            assertSame(failure, assertThrows(Throwable.class, () -> ApplicationSpanService.NOOP.call("load", () -> { throwFailure(failure); return null; })));
            assertSame(failure, assertThrows(Throwable.class, () -> ApplicationSpanService.NOOP.run("load", () -> throwFailure(failure))));
        }
    }

    @lombok.SneakyThrows
    private static void throwFailure(final Throwable failure) { throw failure; }
}
