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
package org.apache.isis.core.runtimeservices.recognizer.dae;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Getter;
import lombok.val;

/**
 * Translates {@link DataAccessException}(s) to {@link Recognition}(s),
 * unless disabled via {@link IsisConfiguration}.
 */
@Service
@Named("isis.runtime.ExceptionRecognizerForDataAccessException")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExceptionRecognizerForDataAccessException
implements ExceptionRecognizer {

    @Getter
    private final boolean disabled;
    
    @Inject
    public ExceptionRecognizerForDataAccessException(IsisConfiguration conf) {
        this.disabled = conf.getCore().getRuntimeServices()
              .getExceptionRecognizer().getDae().isDisable();
    }
    
    @Override
    public Optional<Recognition> recognize(Throwable ex) {
        if(ex instanceof DataAccessException
                && !isDisabled()) {
            return recognizeDae((DataAccessException)ex);
        }
        return Optional.empty();
    }

    // -- HELPER
    
    private Optional<Recognition> recognizeDae(DataAccessException ex) {
        if(ex instanceof ConcurrencyFailureException) {
            return recognitionOf(Category.CONCURRENCY, ex);
             
        }
        if(ex instanceof TransientDataAccessException
                || ex instanceof RecoverableDataAccessException) {
            return recognitionOf(Category.RETRYABLE, ex); 
        }
        if(ex instanceof DataIntegrityViolationException) {
            // eg. Data or related data already exists
            return recognitionOf(Category.CONSTRAINT_VIOLATION, ex); 
        }
        if(ex instanceof DataRetrievalFailureException) {
            // Unable to load object. eg. Has it been deleted by someone else?
            return recognitionOf(Category.NOT_FOUND, ex); 
        }
        if(ex instanceof NonTransientDataAccessException) {
            // eg. Unable to save changes. Does similar data already exist, 
            // or has referenced data been deleted?"
            return recognitionOf(Category.SERVER_ERROR, ex); 
        }
        return recognitionOf(Category.OTHER, ex);
    }

    private Optional<Recognition> recognitionOf(Category category, DataAccessException ex) {
        val causeMessage = _Strings.nullToEmpty(ex.getMostSpecificCause().getMessage()).trim();
        
        val exceptionFriendlyName = _Strings.asNaturalName2
                .apply(ex.getClass().getSimpleName())
                .toLowerCase();
        
        val friendlyMessage = String.format("%s (%s): %s", 
                category.getFriendlyName(),
                exceptionFriendlyName,
                _Strings.isEmpty(causeMessage)
                    ? "Cannot find any details for what is causing the issue."
                    : causeMessage);
        
        return Recognition.of(category, friendlyMessage);
    }


}
