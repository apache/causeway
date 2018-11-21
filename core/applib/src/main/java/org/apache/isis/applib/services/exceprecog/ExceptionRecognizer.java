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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.Programmatic;


/**
 * Domain service to (attempt) to recognize certain
 * exceptions, and return user-friendly messages instead.
 *
 * <p>
 * Rather than redirecting to a general-purpose error page,
 * the message (corresponding to the recognized exception) is rendered
 * as a regular validation message.
 *
 * <p>
 * More than one implementation of {@link ExceptionRecognizer} can
 * be registered; they will all be consulted (in the order specified in
 * <tt>isis.properties</tt>) to determine if they recognize the exception.
 * The message returned by the first service recognizing the exception is
 * used.
 *
 * <p>
 * The Isis framework also provides a default implementation of this
 * service that recognizes any {@link org.apache.isis.applib.RecoverableException}, simply returning
 * the exception's {@link org.apache.isis.applib.RecoverableException#getMessage() message}.  This
 * allows any component or domain object to throw this exception with
 * the knowledge that it will be handled appropriately.
 *
 * <p>
 * Initially introduced for the Wicket viewer; check the documentation
 * of other viewers to determine whether they also support this service.
 */
public interface ExceptionRecognizer {

    /**
     * (Attempt to) recognize the exception and return a user-friendly
     * message to render instead.
     *
     * @return user-friendly message to render, or <tt>null</tt> otherwise.
     */
    @Programmatic
    public String recognize(Throwable ex);

    /**
     * An extension to {@link #recognize(Throwable)} that allows recognized exceptions
     * to be {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer.Category categorize}d.
     */
    @Programmatic
    Recognition recognize2(Throwable ex);

    @Programmatic
    @PostConstruct
    public void init(Map<String, String> properties);

    @Programmatic
    @PreDestroy
    public void shutdown();

    enum Category {
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

    class Recognition {

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
}
