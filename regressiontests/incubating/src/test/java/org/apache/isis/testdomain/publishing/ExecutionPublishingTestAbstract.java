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
package org.apache.isis.testdomain.publishing;

import java.util.Objects;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.iactn.ActionInvocation;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.PropertyEdit;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.publishing.subscriber.ExecutionSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class ExecutionPublishingTestAbstract
implements HasPersistenceStandard {

    @Inject private KVStoreForTesting kvStore;

    protected void given() {
        ExecutionSubscriberForTesting.clearPublishedEntries(kvStore);
    }

    protected void verify(
            final ChangeScenario changeScenario,
            final VerificationStage verificationStage) {
        switch(verificationStage) {

        case FAILURE_CASE:
            assertHasExecutionEntries(Can.empty());
            break;
        case PRE_COMMIT:
        case POST_INTERACTION:
            break;
        case POST_COMMIT:
            val bookClass = bookClass();
            Interaction interaction = null;
            Identifier propertyId = Identifier.propertyOrCollectionIdentifier(
                    LogicalType.fqcn(bookClass), "name");
            Object target = null;
            Object argValue = "Book #2";
            String targetMemberName = "name???";
            String targetClass = bookClass.getName();

            assertHasExecutionEntries(Can.of(
                    new PropertyEdit(interaction, propertyId, target, argValue, targetMemberName, targetClass)
                    ));
            break;
        default:
            // if hitting this, the caller is requesting a verification stage, we are providing no case for
            fail(String.format("internal error, stage not verified: %s", verificationStage));
        }
    }

    // -- HELPER

    private Class<?> bookClass() {
        switch(getPersistenceStandard()) {
        case JDO:
            return JdoBook.class;
        case JPA:
            return JpaBook.class;
        default:
            throw _Exceptions.unmatchedCase(getPersistenceStandard());
        }
    }

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
