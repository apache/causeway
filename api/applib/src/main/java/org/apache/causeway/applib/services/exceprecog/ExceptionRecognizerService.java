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

import org.apache.causeway.commons.collections.Can;

/**
 * Used by the framework to iterate over all registered
 * {@link ExceptionRecognizer}s in the correct order.
 *
 * @since 1.x {@index}
 */
public interface ExceptionRecognizerService {

    /**
     * All ExceptionRecognizer implementations as discovered by the IoC
     * container,  honoring order of precedence.
     * @return all ExceptionRecognizer implementations as discovered by the IoC container,
     * honoring order of precedence.
     */
    Can<ExceptionRecognizer> getExceptionRecognizers();

    /**
     * Takes into consideration ExceptionRecognizers as given by {@link #getExceptionRecognizers()}.
     *
     * @param ex - exception to be recognized
     * @return optionally a recognition object, that describes both the category and reason,
     * that will be included with the user-friendly message.
     */
    default Optional<Recognition> recognize(Throwable ex) {
        return recognizeFromSelected(getExceptionRecognizers(), ex);
    }

    /**
     * Takes into consideration ExceptionRecognizers as given by {@code recognizers}.
     *
     * @param recognizers - one or more recognizers to attempt to recognize the exception
     * @param ex - the exception to be recognized
     * @return optionally a recognition object, that describes both the category and reason,
     * that will be included with the user-friendly message.
     */
    Optional<Recognition> recognizeFromSelected(Can<ExceptionRecognizer> recognizers, Throwable ex);

}
