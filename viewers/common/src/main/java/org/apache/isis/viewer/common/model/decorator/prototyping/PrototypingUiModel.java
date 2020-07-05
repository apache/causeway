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
package org.apache.isis.viewer.common.model.decorator.prototyping;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PrototypingUiModel {

    private final Class<?> featureType;
    private final String featureShortLabel;
    private final String featureFullLabel;
    private final Supplier<Stream<Facet>> facetStreamProvider;
    
    public static PrototypingUiModel of(ActionUiMetaModel actionMeta) {
        return null; // used by wicket, not supported yet
    }
    
    public static PrototypingUiModel of(ManagedAction managedAction) {
        Class<?> featureType = managedAction.getAction().getReturnType().getCorrespondingClass();
        String featureShortLabel = managedAction.getName();
        String featureFullLabel = String.format("%s: %s", 
                managedAction.getMemberType(), 
                managedAction.getName());
        
        return new PrototypingUiModel(featureType, featureShortLabel, featureFullLabel,
                managedAction.getAction()::streamFacets);
    }
    
    public static PrototypingUiModel of(ManagedMember managedMember) {
        Class<?> featureType = managedMember.getSpecification().getCorrespondingClass();
        String featureShortLabel = managedMember.getName();
        String featureFullLabel = String.format("%s: %s", 
                managedMember.getMemberType(), 
                managedMember.getName());
        
        return new PrototypingUiModel(featureType, featureShortLabel, featureFullLabel,
                managedMember.getSpecification()::streamFacets);
    }
    
    public Stream<Facet> streamFeatureFacets() {
        return facetStreamProvider.get();
    }
    

    
}
