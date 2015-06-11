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
package org.apache.isis.applib.services.exceprecog;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * An extension of the {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer} interface that
 * allows recognized exceptions to be {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2.Category categorize}d.
 */
public interface ExceptionRecognizer2 extends ExceptionRecognizer {


    public enum Category {
        /**
         * A violation of some declarative constraint (eg uniqueness or referential integrity) was detected.
         */
        CONSTRAINT_VIOLATION,
        /**
         * The object to be acted upon cannot be found (404)
         */
        NOT_FOUND,
        /**
         * A concurrency exception, in other words some other user has changed this object.
         */
        CONCURRENCY,
        /**
         * Recognized, but for some other reason... 40x error
         */
        CLIENT_ERROR,
        /**
         * 50x error
         */
        SERVER_ERROR,
        /**
         * Recognized, but uncategorized (typically: a recognizer of the original ExceptionRecognizer API).
         */
        OTHER
    }

    public static class Recognition {

        /**
         * Returns a recognition of the specified type (assuming a non-null reason); else null.
         */
        public static Recognition of(final Category category, final String reason) {
            return reason != null? new Recognition(category, reason): null;
        }

        private final Category category;
        private final String reason;

        public Recognition(final Category category, final String reason) {
            this.category = category;
            this.reason = reason;
        }

        public Category getCategory() {
            return category;
        }

        public String getReason() {
            return reason;
        }
    }

    @Programmatic
    public Recognition recognize2(final Throwable ex);

}
