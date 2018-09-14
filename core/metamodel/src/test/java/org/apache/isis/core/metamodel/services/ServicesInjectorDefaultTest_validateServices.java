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

package org.apache.isis.core.metamodel.services;

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;

public class ServicesInjectorDefaultTest_validateServices {

    ServicesInjector servicesInjector;

    public static class DomainServiceWithSomeId {
        public String getId() { return "someId"; }
    }

    public static class DomainServiceWithDuplicateId {
        public String getId() { return "someId"; }
    }

    public static class DomainServiceWithDifferentId {
        public String getId() { return "otherId"; }
    }

    public static class ValidateServicesTestValidateServices extends ServicesInjectorDefaultTest_validateServices {

        List<Object> serviceList;
        IsisConfigurationDefault stubConfiguration;

        @Before
        public void setUp() throws Exception {
            serviceList = _Lists.newArrayList();
            stubConfiguration = new IsisConfigurationDefault();
        }

        @Test(expected=IllegalStateException.class)
        public void validate_DomainServicesWithDuplicateIds() {

            // given
            serviceList.add(new DomainServiceWithSomeId());
            serviceList.add(new DomainServiceWithDuplicateId());

            servicesInjector = ServicesInjector.forTesting(serviceList, stubConfiguration, null);

            // when
            servicesInjector.validateServices();
        }

        public void validate_DomainServicesWithDifferentIds() {

            // given
            serviceList.add(new DomainServiceWithSomeId());
            serviceList.add(new DomainServiceWithDifferentId());

            servicesInjector = ServicesInjector.forTesting(serviceList, stubConfiguration, null);

            // when
            servicesInjector.validateServices();
        }

    }
}
