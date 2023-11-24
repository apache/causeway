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
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.exceprecog.Category;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.val;

class ExceptionRecognizerForDataAccessExceptionTest {

    private ExceptionRecognizerForDataAccessException recognizerForDae;  

    @BeforeEach
    public void setUp() {
        val conf = new IsisConfiguration(null); 
        recognizerForDae = new ExceptionRecognizerForDataAccessException(conf);  
    }

    @Test
    void migrationWarning() {
        System.err.printf(
                "%s - TODO: No tests yet for whether we can recognize Spring's various org.springframework.dao.DataAccessException(s)", 
                this.getClass().getName());
    }

    @Test
    void daeToCategoryMapping() {
        // testing just a subset here ...
        assertCategory(Category.SERVER_ERROR, new NonTransientDataAccessResourceException("msg"));
        assertCategory(Category.RETRYABLE, new TransientDataAccessResourceException("msg"));
        assertCategory(Category.RETRYABLE, new RecoverableDataAccessException("msg"));
        assertCategory(Category.NOT_FOUND, new EmptyResultDataAccessException("msg", 99));
        assertCategory(Category.CONSTRAINT_VIOLATION, new DuplicateKeyException("msg"));
        assertCategory(Category.CONCURRENCY, new CannotAcquireLockException("msg"));
    }
    
    // -- HELPER
    
    void assertCategory(
            Category category,
            DataAccessException dae) {
        
        val recognized = recognizerForDae.recognize(dae).orElse(null);
        assertNotNull(recognized);
        assertNotNull(recognized.getReason());
        assertTrue(recognized.getReason().length()>10);
        assertEquals(category, recognized.getCategory());
    }

}
