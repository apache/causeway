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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PrototypingUiModel {

    private final Class<?> featureType;
    private final String featureFriendlyName;
    private final String featureFriendlyIdentifier;
    private final Supplier<Stream<Facet>> facetStreamProvider;

    public static PrototypingUiModel of(final ManagedAction managedAction) {
        Class<?> featureType = managedAction.getAction().getReturnType().getCorrespondingClass();
        String featureShortLabel = managedAction.getFriendlyName();
        String featureFullLabel = String.format("%s: %s",
                managedAction.getMemberType(),
                managedAction.getId());

        return new PrototypingUiModel(featureType, featureShortLabel, featureFullLabel,
                managedAction.getAction()::streamFacets);
    }

    public static PrototypingUiModel of(final ManagedMember managedMember) {
        Class<?> featureType = managedMember.getElementClass();
        String featureShortLabel = managedMember.getFriendlyName();
        String featureFullLabel = String.format("%s: %s",
                managedMember.getMemberType(),
                managedMember.getId());

        return new PrototypingUiModel(featureType, featureShortLabel, featureFullLabel,
                managedMember.getElementType()::streamFacets);
    }

    public Stream<Facet> streamFeatureFacets() {
        return facetStreamProvider.get();
    }



}
