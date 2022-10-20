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
package org.apache.causeway.applib.services.exceprecog;

import java.util.Optional;

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
 * For example, a set of recognizers are provided for the JPA and JDO
 * persistence mechanisms in order to recognize and handle SQL constraint
 * exceptions such as uniqueness violations. These can then be rendered back to
 * the user as expected errors, rather than fatal stacktraces.
 *
 * <p>
 * More than one implementation of {@link ExceptionRecognizer} can
 * be registered; they will all be consulted (in the order as specified by
 * Spring's {@link org.springframework.core.annotation.Order} annotation)
 * to determine if they recognize the exception.
 * The message returned by the first service recognizing the exception is
 * used.
 *
 * <p>
 * The framework also provides a default implementation of this
 * service that recognizes any {@link org.apache.causeway.applib.exceptions.RecoverableException}, simply returning
 * the exception's {@link org.apache.causeway.applib.exceptions.RecoverableException#getMessage() message}.  This
 * allows any component or domain object to throw this exception with
 * the knowledge that it will be handled appropriately.
 *
 * <p>
 * Initially introduced for the Wicket viewer; check the documentation
 * of other viewers to determine whether they also support this service.
 *
 * @since 1.x {@index}
 */
public interface ExceptionRecognizer {

    /**
     * (Attempt to) recognize the exception and return a user-friendly
     * message to render instead.
     *
     * @return optionally a
     * {@link Recognition recognition} object,
     * that describes both the
     * {@link Category category}
     * and reason that will be included with the user-friendly message.
     */
    Optional<Recognition> recognize(Throwable ex);

}
