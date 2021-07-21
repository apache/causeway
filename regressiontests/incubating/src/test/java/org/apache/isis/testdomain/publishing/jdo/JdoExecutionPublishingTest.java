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
package org.apache.isis.testdomain.publishing.jdo;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.iactn.ActionInvocation;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.PropertyEdit;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayEnable;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.publishing.ApplicationLayerTestFactoryJdo;
import org.apache.isis.testdomain.publishing.ApplicationLayerTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.publishing.conf.Configuration_usingExecutionPublishing;
import org.apache.isis.testdomain.publishing.subscriber.ExecutionSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingExecutionPublishing.class,
                ApplicationLayerTestFactoryJdo.class,
                XrayEnable.class
        },
        properties = {
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.persistence.IsisTransactionJdo=DEBUG",
                "logging.level.org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault=DEBUG",
                "logging.level.org.apache.isis.persistence.jdo.datanucleus5.datanucleus.service.JdoPersistenceLifecycleService=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
class JdoExecutionPublishingTest {

    @Inject private ApplicationLayerTestFactoryJdo testFactory;
    @Inject private KVStoreForTesting kvStore;

    @TestFactory @DisplayName("Application Layer")
    List<DynamicTest> generateTests() {
        return testFactory.generateTests(this::given, this::verify);
    }

    private void given() {
        ExecutionSubscriberForTesting.clearPublishedEntries(kvStore);
    }

    private void verify(final VerificationStage verificationStage) {
        switch(verificationStage) {

        case FAILURE_CASE:
            assertHasExecutionEntries(Can.empty());
            break;
        case POST_COMMIT:
            Interaction interaction = null;
            Identifier propertyId = Identifier.propertyOrCollectionIdentifier(
                    LogicalType.fqcn(JdoBook.class), "name");
            Object target = null;
            Object argValue = "Book #2";
            String targetMemberName = "name???";
            String targetClass = "org.apache.isis.testdomain.jdo.entities.JdoBook";
            assertHasExecutionEntries(Can.of(
                    new PropertyEdit(interaction, propertyId, target, argValue, targetMemberName, targetClass)
                    ));
            break;
        default:
            // ignore ... no checks
        }
    }

    // -- HELPER

    private void assertHasExecutionEntries(final Can<Execution<?, ?>> expectedExecutions) {
        val actualExecutions = ExecutionSubscriberForTesting.getPublishedExecutions(kvStore);
        CollectionAssertions.assertComponentWiseEquals(
                expectedExecutions, actualExecutions, this::executionDifference);
    }

    private String executionDifference(final Execution<?, ?> a, final Execution<?, ?> b) {
        if(!Objects.equals(a.getMemberIdentifier(), b.getMemberIdentifier())) {
            return String.format("differing member identifier %s != %s",
                    a.getMemberIdentifier(), b.getMemberIdentifier());
        }
        if(!Objects.equals(a.getInteractionType(), b.getInteractionType())) {
            return String.format("differing interaction type %s != %s",
                    a.getInteractionType(), b.getInteractionType());
        }

        switch(a.getInteractionType()) {
        case ACTION_INVOCATION:
            return actionInvocationDifference(
                    (ActionInvocation)a, (ActionInvocation)b);
        case PROPERTY_EDIT:
            return porpertyEditDifference(
                    (PropertyEdit)a, (PropertyEdit)b);
        default:
            throw _Exceptions.unexpectedCodeReach();
        }
    }

    private String actionInvocationDifference(final ActionInvocation a, final ActionInvocation b) {
        return null; // no difference
    }


    private String porpertyEditDifference(final PropertyEdit a, final PropertyEdit b) {
        if(!Objects.equals(a.getNewValue(), b.getNewValue())) {
            return String.format("differing new value %s != %s",
                    a.getNewValue(), b.getNewValue());
        }

        return null; // no difference
    }

}
