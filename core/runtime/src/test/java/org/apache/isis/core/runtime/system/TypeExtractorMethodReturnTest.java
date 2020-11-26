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

package org.apache.isis.core.runtime.system;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.metamodel.specloader.typeextract.TypeExtractor;

import lombok.val;

public class TypeExtractorMethodReturnTest {

    @Test
    public void shouldFindGenericTypes() throws Exception {

        class Customer {
        }
        class CustomerRepository {
            @SuppressWarnings("unused")
            public List<Customer> findCustomers() {
                return null;
            }
        }

        final Class<?> clazz = CustomerRepository.class;
        final Method method = clazz.getMethod("findCustomers");
        
        val classes = TypeExtractor.streamMethodReturn(method).collect(Collectors.toSet());
        
        assertEquals(2, classes.size());
        assertTrue(classes.contains(java.util.List.class));
        assertTrue(classes.contains(Customer.class));
        
    }

    @Test
    public void ignoresVoidType() throws Exception {

        class CustomerRepository {
            @SuppressWarnings("unused")
            public void findCustomers() {
            }
        }

        final Class<?> clazz = CustomerRepository.class;
        final Method method = clazz.getMethod("findCustomers");

        val classes = TypeExtractor.streamMethodReturn(method).collect(Collectors.toSet());
        assertEquals(0, classes.size());
    }

}
