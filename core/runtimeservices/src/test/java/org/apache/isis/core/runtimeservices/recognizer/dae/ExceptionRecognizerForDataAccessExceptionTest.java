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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void recognizerDataAlreadyExists() {

        val msg = "JDO operation: "
                + "Insert of object \"domainapp.modules.hello.dom.hwo.HelloWorldObject@6cad4834\" "
                + "using statement \"INSERT INTO \"hello\".\"HelloWorldObject\" "
                + "(\"name\",\"notes\",\"version\") VALUES (?,?,?)\" failed : "
                + "Unique index or primary key violation: "
                + "\"hello.HelloWorldObject_name_UNQ_INDEX_B ON hello.HelloWorldObject(name) "
                + "VALUES 1\"; SQL statement: ...";

        val ex = new org.springframework.dao.DataIntegrityViolationException(msg);

        val recognized = recognizerDataAlreadyExists.recognize(ex).orElse(null);
        assertNotNull(recognized);
        assertNotNull(recognized.getReason());
        assertEquals("Data already exists: " + msg, recognized.getReason());
    }

    //TODO @Test
    void recognizerObjectNotFound() {

    }

    //TODO @Test
    void recognizerRelatedDataExists() {

    }

    //TODO @Test
    void recognizerUnableToSaveData() {

    }



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
