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
package org.apache.isis.persistence.jdo.integration.changetracking;

import java.awt.Color;

import javax.inject.Provider;

import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.commons.internal.debug._XrayEvent;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.security.util.XrayUtil;

import lombok.val;

final class _Xray {

    public static void publish(
            final EntityChangeTrackerJdo entityChangeTrackerDefault,
            final Provider<InteractionProvider> interactionProviderProvider) {

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        final long propertyChangeRecordCount = entityChangeTrackerDefault.countPotentialPropertyChangeRecords();

        val enteringLabel = String.format("consider %d entity change records for publishing",
                propertyChangeRecordCount);

        XrayUtil.createSequenceHandle(interactionProviderProvider.get(), "ec-tracker")
        .ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("ec-tracker", "EntityChange-\nTracker-\n(Default)");

                if(propertyChangeRecordCount==0) {
                    sequenceData.setConnectionArrowColor(Color.GRAY);
                    sequenceData.setConnectionLabelColor(Color.GRAY);
                }

                val callee = handle.getCallees().getFirstOrFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                //sequenceData.activate(callee);
            });

        });
    }

    public static void enlistCreated(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("enlistCreated", entity, interactionProviderProvider);
    }

    public static void enlistDeleting(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("enlistDeleting", entity, interactionProviderProvider);
    }

    public static void enlistUpdating(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("enlistUpdating", entity, interactionProviderProvider);
    }

    public static void recognizeLoaded(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("recognizeLoaded", entity, interactionProviderProvider);
    }

    public static void recognizePersisting(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("recognizePersisting", entity, interactionProviderProvider);
    }

    public static void recognizeUpdating(
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {
        addSequence("recognizeUpdating", entity, interactionProviderProvider);
    }

    // -- HELPER

    private static void addSequence(
            final String what,
            final ManagedObject entity,
            final Provider<InteractionProvider> interactionProviderProvider) {

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        val enteringLabel = String.format("%s %s",
                what,
                ManagedObjects.isNullOrUnspecifiedOrEmpty(entity)
                    ? "<empty>"
                    : String.format("%s:\n%s",
                            entity.getSpecification().getLogicalTypeName(),
                            "" + entity.getPojo()));

        _XrayEvent.event(enteringLabel);

        XrayUtil.createSequenceHandle(interactionProviderProvider.get(), "ec-tracker")
        .ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("ec-tracker", "EntityChange-\nTracker-\n(Default)");

                val callee = handle.getCallees().getFirstOrFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                //sequenceData.activate(callee);
            });

        });

    }




}
