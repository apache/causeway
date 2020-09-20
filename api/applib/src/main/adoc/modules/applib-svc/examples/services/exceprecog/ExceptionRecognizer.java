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

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.i18n.TranslationService;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;


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
 * be registered; they will all be consulted (in the order as specified by the @Order annotation) 
 * to determine if they recognize the exception.
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
// tag::refguide[]
public interface ExceptionRecognizer {

    // end::refguide[]
    /**
     * (Attempt to) recognize the exception and return a user-friendly
     * message to render instead.
     *
     * @return optionally a
     * {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer.Recognition recognition} object,
     * that describes both the
     * {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer.Category category}
     * and reason that will be included with the user-friendly message.
     */
    // tag::refguide[]
    Optional<Recognition> recognize(Throwable ex);  // <.>

    // end::refguide[]
    // tag::refguide-1[]
    @RequiredArgsConstructor
    enum Category {
        // end::refguide-1[]
        /**
         * A violation of some declarative constraint (eg uniqueness or referential integrity) was detected.
         */
        // tag::refguide-1[]
        CONSTRAINT_VIOLATION(                                           // <.>
                "violation of some declarative constraint"),
        // end::refguide-1[]
        /**
         * The object to be acted upon cannot be found (404)
         */
        // tag::refguide-1[]
        NOT_FOUND(                                                      // <.>
                "object not found"),
        // end::refguide-1[]
        /**
         * A concurrency exception, in other words some other user has changed this object.
         */
        // tag::refguide-1[]
        CONCURRENCY(                                                    // <.>
                "concurrent modification"),
        // end::refguide-1[]
        /**
         * Recognized, but for some other reason... 40x error
         */
        // tag::refguide-1[]
        CLIENT_ERROR(                                                   // <.>
                "client side error"),
        // end::refguide-1[]
        /**
         * 50x error
         */
        // tag::refguide-1[]
        SERVER_ERROR(                                                   // <.>
                "server side error"),
        // end::refguide-1[]
        /**
         * Recognized, but uncategorized (typically: a recognizer of the original ExceptionRecognizer API).
         */
        // tag::refguide-1[]
        OTHER(                                                          // <.>
                "other")
        ;

        @Getter
        private final String friendlyName;
    }
    // end::refguide-1[]

    // tag::refguide-2[]
    @Value
    class Recognition {

        // end::refguide-2[]
        /**
         * @return optionally a recognition of the specified type, based on a whether given reason is non-null
         */
        // tag::refguide-2[]
        public static Optional<Recognition> of(
                @Nullable final Category category,
                @Nullable final String reason) {
            // end::refguide-2[]

            if(reason==null) {
                return Optional.empty();
            }

            val nonNullCategory = category!=null? category: Category.OTHER;
            return Optional.of(new Recognition(nonNullCategory, reason));
            // tag::refguide-2[]
            // ...
        }

        @NonNull private final Category category;
        @NonNull private final String reason;

        public String toMessage(@Nullable TranslationService translationService) {
            // end::refguide-2[]

            val categoryLiteral = translate(getCategory().getFriendlyName(), translationService);
            val reasonLiteral = translate(getReason(), translationService);

            return String.format("[%s]: %s", categoryLiteral, reasonLiteral);
            // tag::refguide-2[]
            // ...
        }
        
        public String toMessageNoCategory(@Nullable TranslationService translationService) {
            // end::refguide-2[]

            val reasonLiteral = translate(getReason(), translationService);
            return String.format("%s", reasonLiteral);
            // tag::refguide-2[]
            // ...
        }
        
        private static String translate(
                @Nullable String x, 
                @Nullable TranslationService translationService) {
            if(x==null || translationService==null) {
                return x;
            }
            return translationService.translate(
                    ExceptionRecognizer.Recognition.class.getName(), x);
        }
        
    }
    // end::refguide-2[]

    // tag::refguide[]
}
// end::refguide[]
