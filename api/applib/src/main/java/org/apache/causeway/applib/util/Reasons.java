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
package org.apache.causeway.applib.util;

/**
 * Very simple alternative to {@link ReasonBuffer} and {@link ReasonBuffer2}.
 *
 * @since 1.x {@index}
 */
public class Reasons {
    private Reasons() {
    }

    public static String coalesce(final String... reasons) {
        final StringBuilder buf = new StringBuilder();
        for (final String reason : reasons) {
            appendIfNotNull(buf, reason);
        }
        return asStringElseNull(buf);
    }

    private static void appendIfNotNull(final StringBuilder buf, final String reason) {
        if (reason == null) {
            return;
        }
        if (buf.length() > 0) {
            buf.append("; ");
        }
        buf.append(reason);
    }

    private static String asStringElseNull(final StringBuilder buf) {
        return buf.length() == 0 ? null : buf.toString();
    }

}
