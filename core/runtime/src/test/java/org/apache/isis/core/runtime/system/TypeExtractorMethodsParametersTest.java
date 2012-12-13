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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.specloader.traverser.TypeExtractorMethodParameters;

public class TypeExtractorMethodsParametersTest {

    @Test
    public void shouldFindGenericTypes() throws Exception {

        class Customer {
        }
        class CustomerRepository {
            @SuppressWarnings("unused")
            public void filterCustomers(final List<Customer> customerList) {
                ;
            }
        }

        final Class<?> clazz = CustomerRepository.class;
        final Method method = clazz.getMethod("filterCustomers", List.class);

        final TypeExtractorMethodParameters extractor = new TypeExtractorMethodParameters(method);

        assertThat(extractor.getClasses().size(), is(2));
        assertThat(extractor.getClasses(), IsisMatchers.containsElementThat(equalTo(java.util.List.class)));
        assertThat(extractor.getClasses(), IsisMatchers.containsElementThat(equalTo(Customer.class)));
    }

}
