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
package org.apache.causeway.core.runtimeservices.recognizer;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.exceprecog.Recognition;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ExceptionRecognizerServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExceptionRecognizerServiceDefault
implements ExceptionRecognizerService {

    @Inject private ServiceRegistry serviceRegistry;

    @Override
    public Can<ExceptionRecognizer> getExceptionRecognizers() {
        return exceptionRecognizers.get();
    }

    @Override
    public Optional<Recognition> recognizeFromSelected(
            final @NonNull Can<ExceptionRecognizer> recognizers,
            final @NonNull Throwable ex) {

        return _Exceptions.streamCausalChain(ex)
        .map(nextEx->recognize(recognizers, nextEx))
        .filter(Optional::isPresent)
        .findFirst()
        .orElse(Optional.empty());
    }

    // -- HELPER

    private final _Lazy<Can<ExceptionRecognizer>> exceptionRecognizers =
            _Lazy.threadSafe(()->serviceRegistry.select(ExceptionRecognizer.class));

    private static Optional<Recognition> recognize(
            final Can<ExceptionRecognizer> recognizers,
            final Throwable ex) {

        return recognizers.stream()
        .map(recognizer->recognize(recognizer, ex))
        .filter(Optional::isPresent)
        .findFirst()
        .orElse(Optional.empty());
    }

    /*
     * handle recognizers in a null-safe manner (might be third party contributed)
     */
    private static Optional<Recognition> recognize(
            final ExceptionRecognizer recognizer,
            final Throwable ex) {

        val recognized = recognizer.recognize(ex);
        return recognized==null
                ? Optional.empty()
                : recognized;
    }


}
