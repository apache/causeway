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
package org.apache.isis.persistence.jdo.datanucleus.test;

import java.sql.SQLIntegrityConstraintViolationException;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.persistence.jdo.datanucleus.exceptions.recognizers.ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException;

import lombok.val;

public class ExceptionRecognizerForSQLIntegrityConstraintViolationExceptionTest {

    private ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException exceptionRecognizer;

    @Before
    public void setUp() throws Exception {
        exceptionRecognizer = new ExceptionRecognizerForSQLIntegrityConstraintViolationUniqueOrIndexException();
    }

    @Test
    public void uniqueConstraintOrIndexViolation() throws Exception {
        final String msg = "initial gumph: unique constraint or index violation; further details";
        final SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);
        val recognized = exceptionRecognizer.recognize(ex).orElse(null);
        assertThat(recognized, is(not(nullValue())));
        assertThat(recognized.getReason(), is("Data already exists: " + msg));
    }

    @Test
    public void notNullCheckConstraintViolation() throws Exception {
        final String msg = "initial gumph: NOT NULL check constraint; further details";
        final SQLIntegrityConstraintViolationException ex = new SQLIntegrityConstraintViolationException(msg);
        val recognized = exceptionRecognizer.recognize(ex).orElse(null);
        assertThat(recognized, is(nullValue()));
    }

}
