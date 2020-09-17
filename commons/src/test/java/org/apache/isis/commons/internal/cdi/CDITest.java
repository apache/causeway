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
package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.ioc.cdi._CDI;

class CDITest {

    private Field stringField1;
    private Field stringField2;
    private Field stringField3;

    @BeforeEach
    void setUp() throws Exception {
        stringField1 = Customer.class.getDeclaredField("stringField1");
        stringField2 = Customer.class.getDeclaredField("stringField2");
        stringField3 = Customer.class.getDeclaredField("stringField3");
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    // -- STAGED TEST CLASS

    static class Customer {

        @Inject  
        String stringField1;

        @Inject @CheckForNull //arbitrary non qualifier
        String stringField2;

        @Inject @ValidQualifierForTesting
        String stringField3;
    }

    // ---

    @Test
    void qualifierDetection() {

        //when
        ValidQualifierForTesting[] annotations = stringField3.getAnnotationsByType(ValidQualifierForTesting.class);

        //then
        assertNotNull(annotations);
        assertEquals(1, annotations.length);

        //when 
        ValidQualifierForTesting annotation = annotations[0];

        //then
        assertNotNull(annotation);
        Assertions.assertTrue(_CDI.isQualifier(annotation));

    }


    @Test
    void noQualifier() {

        //when
        List<Annotation> qualifiers = _CDI.filterQualifiers(stringField1.getAnnotations());

        //then
        assertNotNull(qualifiers);
        assertEquals(0, qualifiers.size());

    }

    @Test
    void noQualifier_arbitraryAnnotation() {

        //when
        List<Annotation> qualifiers = _CDI.filterQualifiers(stringField2.getAnnotations());

        //then
        assertNotNull(qualifiers);
        assertEquals(0, qualifiers.size());

    }

    @Test
    void singleQualifier() {

        //when
        List<Annotation> qualifiers = _CDI.filterQualifiers(stringField3.getAnnotations());

        //then
        assertNotNull(qualifiers);
        assertEquals(1, qualifiers.size());

    }

}
