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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtimeservices.recognizer.dae.impl.ExceptionRecognizerForDataAlreadyExists;
import org.apache.isis.core.runtimeservices.recognizer.dae.impl.ExceptionRecognizerForObjectNotFound;
import org.apache.isis.core.runtimeservices.recognizer.dae.impl.ExceptionRecognizerForRelatedDataExists;
import org.apache.isis.core.runtimeservices.recognizer.dae.impl.ExceptionRecognizerForUnableToSaveData;

import lombok.val;

class ExceptionRecognizerForDataAccessExceptionTest {

    private ExceptionRecognizerForDataAlreadyExists recognizerDataAlreadyExists;  
    private ExceptionRecognizerForObjectNotFound recognizerObjectNotFound;
    private ExceptionRecognizerForRelatedDataExists recognizerRelatedDataExists;
    private ExceptionRecognizerForUnableToSaveData recognizerUnableToSaveData;

    @BeforeEach
    public void setUp() {
        val conf = new IsisConfiguration(null); 
        recognizerDataAlreadyExists = new ExceptionRecognizerForDataAlreadyExists(conf);  
        recognizerObjectNotFound = new ExceptionRecognizerForObjectNotFound(conf);
        recognizerRelatedDataExists = new ExceptionRecognizerForRelatedDataExists(conf);
        recognizerUnableToSaveData = new ExceptionRecognizerForUnableToSaveData(conf);
    }

    @Test
    void migrationWarning() {
        System.err.printf(
                "%s - TODO: No tests yet for whether we can recognize Spring's various org.springframework.dao.DataAccessException(s)", 
                this.getClass().getName());
    }
    
    //TODO implement tests
    
//    @Test
//    void uniqueConstraintOrIndexViolation() {
//        final String msg = "initial gumph: unique constraint or index violation; further details";
//        final SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);
//        val recognized = exceptionRecognizer.recognize(ex).orElse(null);
//        assertThat(recognized, is(not(nullValue())));
//        assertThat(recognized.getReason(), is("Data already exists: " + msg));
//    }
//
//    @Test
//    void notNullCheckConstraintViolation() {
//        final String msg = "initial gumph: NOT NULL check constraint; further details";
//        final SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);
//        val recognized = exceptionRecognizer.recognize(ex).orElse(null);
//        assertThat(recognized, is(nullValue()));
//    }

}
