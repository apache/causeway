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
package org.apache.causeway.testing.integtestsupport.applib;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import org.apache.causeway.applib.exceptions.RecoverableException;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
public class ExceptionRecognizerTranslate implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(
            final ExtensionContext extensionContext,
            final Throwable throwable) throws Throwable {

        val translatedException = _Helper.getExceptionRecognizerService(extensionContext)
        .flatMap(recService->recService.recognize(throwable))
        .<Throwable>map(recognition->new RecoverableException(
                String.format("%s: %s",
                        recognition.getCategory().getFriendlyName(),
                        recognition.getReason()
                ),
                throwable))
        .orElse(throwable);

        throw translatedException;
    }

}
