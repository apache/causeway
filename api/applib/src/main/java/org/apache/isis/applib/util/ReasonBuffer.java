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
package org.apache.isis.applib.util;

/**
 * Helper class to create properly concatenated reason string for use in method
 * that return {@link String}s with reasons.
 *
 * <p>
 * If no reasons are specified {@link #getReason()} will return
 * <code>null</code> , otherwise it will return a {@link String} with all the
 * valid reasons concatenated with a semi-colon separating each one.
 * </p>
 *
 * <p>
 * An alternative is to use the (very simple) {@link Reasons} class or the
 * (much more sophisticated) {@link ReasonBuffer2}.
 * </p>
 *
 * @see Reasons
 * @see ReasonBuffer2
 *
 * @since 1.x {@index}
 */
public class ReasonBuffer {
    StringBuffer reasonBuffer = new StringBuffer();

    /**
     * Append a reason to the list of existing reasons.
     */
    public void append(final String reason) {
        if (reason != null) {
            if (reasonBuffer.length() > 0) {
                reasonBuffer.append("; ");
            }
            reasonBuffer.append(reason);
        }
    }

    /**
     * Append a reason to the list of existing reasons if the condition flag is
     * true.
     */
    public void appendOnCondition(final boolean condition, final String reason) {
        if (condition) {
            append(reason);
        }
    }

    /**
     * Return the combined set of reasons, or <code>null</code> if there are
     * none.
     */
    public String getReason() {
        return reasonBuffer.length() == 0 ? null : reasonBuffer.toString();
    }

}
