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
package org.apache.isis.core.runtimeservices.exceprecog;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer.Recognition;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._NullSafe;

import lombok.NonNull;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@Service
@Named("isisRuntimeServices.ExceptionRecognizerServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class ExceptionRecognizerServiceDefault implements ExceptionRecognizerService {

    @Inject private ServiceRegistry serviceRegistry;
    
    @Override
    public Can<ExceptionRecognizer> getExceptionRecognizers() {
        return exceptionRecognizers.get();
    }
    
    @Override
    public Optional<Recognition> recognize(@NonNull final Exception ex) {
        return handleMultiple(getExceptionRecognizers(), ex);
    }

    @Override
    public Optional<Recognition> recognize(
            @NonNull final Exception ex, 
            @NonNull final Can<ExceptionRecognizer> additionalRecognizers) {
        
        val recognized = recognize(ex);
        if(recognized.isPresent()) {
            return recognized; 
        }
        return handleMultiple(additionalRecognizers, ex);
    }
    
    // -- HELPER
    
    private final _Lazy<Can<ExceptionRecognizer>> exceptionRecognizers = 
            _Lazy.threadSafe(()->serviceRegistry.select(ExceptionRecognizer.class));

    /*
     * handle recognizers in a null-safe manner (might be third party contributed)
     */
    private static Recognition handleSingle(ExceptionRecognizer recognizer, Exception ex) {
        val recognized = recognizer.recognize(ex);
        if(recognized==null || !recognized.isPresent()) {
            return null;    
        }
        return recognized.get();
        
    }
    
    private static Optional<Recognition> handleMultiple(
            @NonNull final Can<ExceptionRecognizer> recognizers, 
            @NonNull final Exception ex) {
        
        return recognizers.stream()
            .map($->handleSingle($, ex))
            .filter(_NullSafe::isPresent)
            .findFirst();
    }

    
}
