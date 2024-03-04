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
package org.apache.causeway.testdomain.publishing.stubs;

import java.util.Collections;
import java.util.Objects;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.causeway.testdomain.publishing.subscriber.ExecutionSubscriberForTesting;
import org.apache.causeway.testdomain.util.CollectionAssertions;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class ExecutionPublishingTestAbstract
extends PublishingTestAbstract {

    @Inject private KVStoreForTesting kvStore;

    @Override
    protected final boolean supportsProgrammaticTesting(final ChangeScenario changeScenario) {
        return false;
    }

    @Override
    protected void given() {
        ExecutionSubscriberForTesting.clearPublishedEntries(kvStore);
    }

    @Override
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
            final Interaction interaction = null;
            final Object target = null;

            switch(changeScenario) {
            case PROPERTY_UPDATE: {
                Identifier propertyId = Identifier.propertyIdentifier(
                        LogicalType.fqcn(bookClass), "name");
                Object argValue = "Book #2";

                assertHasExecutionEntries(Can.of(
                        new PropertyEdit(interaction, propertyId, target, argValue)
                        ));
            }
                break;
            case ACTION_INVOCATION: {
                Identifier actionId = Identifier.actionIdentifier(
                        LogicalType.fqcn(bookClass), "doubleThePrice");
                val args = Collections.<Object>emptyList();

                assertHasExecutionEntries(Can.of(
                        new ActionInvocation(interaction, actionId, target, args)
                        ));
            }

                break;
            default:
                throw _Exceptions.unmatchedCase(changeScenario);
            }

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
        if(!Objects.equals(a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier())) {
            return String.format("differing member identifier %s != %s",
                    a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier());
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
