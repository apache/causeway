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
package org.apache.isis.core.transaction.changetracking;

import javax.inject.Provider;

import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.security.authentication.AuthenticationContext;
import org.apache.isis.core.security.util.XrayUtil;

import lombok.val;

final class _Xray {

    public static void publish(
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }
        
        val enteringLabel = String.format("do publish");
        
        XrayUtil.createSequenceHandle(iaContextProvider.get(), authContextProvider.get(), "ec-tracker")
        .ifPresent(handle->{
            
            handle.submit(sequenceData->{
                
                sequenceData.alias("ec-tracker", "EntityChange-\nTracker-\n(Default)");
                
                val callee = handle.getCallees().getFirstOrFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                //sequenceData.activate(callee);
            });
            
        });
    }
    
    public static void enlistCreated(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("enlistCreated", entity, iaContextProvider, authContextProvider);
    }

    public static void enlistDeleting(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("enlistDeleting", entity, iaContextProvider, authContextProvider);
    }

    public static void enlistUpdating(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("enlistUpdating", entity, iaContextProvider, authContextProvider);
    }

    public static void recognizeLoaded(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("recognizeLoaded", entity, iaContextProvider, authContextProvider);
    }

    public static void recognizePersisting(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("recognizePersisting", entity, iaContextProvider, authContextProvider);
    }

    public static void recognizeUpdating(
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        addSequence("recognizeUpdating", entity, iaContextProvider, authContextProvider);
    }

    // -- HELPER
    
    private static void addSequence(
            final String what,
            final ManagedObject entity, 
            final Provider<InteractionContext> iaContextProvider,
            final Provider<AuthenticationContext> authContextProvider) {
        
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
        
        XrayUtil.createSequenceHandle(iaContextProvider.get(), authContextProvider.get(), "ec-tracker")
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
