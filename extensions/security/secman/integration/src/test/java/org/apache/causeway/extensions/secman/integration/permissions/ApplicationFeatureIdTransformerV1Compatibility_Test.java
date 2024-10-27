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
package org.apache.causeway.extensions.secman.integration.permissions;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.secman.integration.permissions.exampledomain.Customer;

@ExtendWith(MockitoExtension.class)
class ApplicationFeatureIdTransformerV1Compatibility_Test {

    @Mock SpecificationLoader mockSpecificationLoader;
    @Mock ObjectSpecification mockSpecificationForCustomerClass;

    ApplicationFeatureIdTransformer transformer;

    @BeforeEach
    void setup() {
        transformer = new ApplicationFeatureIdTransformerV1Compatibility(mockSpecificationLoader);

        lenient().when(mockSpecificationLoader.specForLogicalTypeName("customer.Customer")).thenReturn(Optional.of(mockSpecificationForCustomerClass));
        lenient().when(mockSpecificationForCustomerClass.getCorrespondingClass()).then(__ -> Customer.class);
    }

    @Test
    void member_when_its_owning_type_exists() {
        ApplicationFeatureId input = ApplicationFeatureId.newMember("customer.Customer", "lastName");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.MEMBER);
        assertThat(transformed.getLogicalTypeName()).isEqualTo(Customer.class.getName());
        assertThat(transformed.getLogicalMemberName()).isEqualTo("lastName");
    }

    @Test
    void member_when_its_owning_type_does_not_exist() {
        ApplicationFeatureId input = ApplicationFeatureId.newMember("order.Order", "placedOn");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.MEMBER);
        assertThat(transformed.getLogicalTypeName()).isEqualTo("order.Order");
        assertThat(transformed.getLogicalMemberName()).isEqualTo("placedOn");
    }

    @Test
    void type_when_exists() {
        ApplicationFeatureId input = ApplicationFeatureId.newType("customer.Customer");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.TYPE);
        assertThat(transformed.getLogicalTypeName()).isEqualTo(Customer.class.getName());
        assertThat(transformed.getLogicalMemberName()).isNull();
    }

    @Test
    void type_when_does_not_exist() {
        ApplicationFeatureId input = ApplicationFeatureId.newType("order.Order");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.TYPE);
        assertThat(transformed.getLogicalTypeName()).isEqualTo("order.Order");
        assertThat(transformed.getLogicalMemberName()).isNull();
    }

}
