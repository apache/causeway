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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.io.Serializable;
import java.util.Objects;

import io.micrometer.observation.Observation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.causeway.core.config.observation.CausewayObservationNaming;

/**
 * Serializable, instance-data-free description of a semantic Wicket render region.
 *
 * @since 2.2
 */
@Getter
public final class WicketRenderObservationDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE_TAG = "causeway.object.type";

    @RequiredArgsConstructor
    @Getter
    public enum Region {
        PAGE("causeway.wicket.page.render", null),
        FIELDSET("causeway.wicket.fieldset.render", "causeway.fieldset.id"),
        PROPERTY("causeway.wicket.property.render", "causeway.property.id"),
        COLLECTION("causeway.wicket.collection.render", "causeway.collection.id"),
        ACTION("causeway.wicket.action.render", "causeway.action.id"),
        ACTION_PROMPT("causeway.wicket.action.prompt.render", "causeway.action.id");

        private final String observationName;
        private final String memberTag;
    }

    public static WicketRenderObservationDescriptor page(final String objectType) {
        return new WicketRenderObservationDescriptor(
                Region.PAGE,
                objectType,
                null,
                CausewayObservationNaming.forType("render", objectType));
    }

    public static WicketRenderObservationDescriptor fieldset(
            final String objectType,
            final String fieldsetId) {
        return regionDescriptor(
                Region.FIELDSET,
                objectType,
                fieldsetId != null && !fieldsetId.isEmpty() ? fieldsetId : "<default>",
                "fieldset");
    }

    public static WicketRenderObservationDescriptor property(
            final String objectType,
            final String propertyId) {
        return regionDescriptor(
                Region.PROPERTY, objectType, propertyId, "property");
    }

    public static WicketRenderObservationDescriptor collection(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.COLLECTION, objectType, collectionId, "collection");
    }

    public static WicketRenderObservationDescriptor action(
            final String objectType,
            final String actionId) {
        return regionDescriptor(
                Region.ACTION, objectType, actionId, "action");
    }

    public static WicketRenderObservationDescriptor actionPrompt(
            final String objectType,
            final String actionId,
            final String actionMemberName) {
        return new WicketRenderObservationDescriptor(
                Region.ACTION_PROMPT,
                objectType,
                actionId,
                CausewayObservationNaming.forMember(
                        "prompt", objectType, actionMemberName));
    }

    private static WicketRenderObservationDescriptor regionDescriptor(
            final Region region,
            final String objectType,
            final String memberId,
            final String regionName) {
        return new WicketRenderObservationDescriptor(
                region,
                objectType,
                memberId,
                CausewayObservationNaming.forRenderRegion(
                        regionName, memberId));
    }

    private final Region region;
    private final String objectType;
    private final String memberId;
    private final String contextualName;

    private WicketRenderObservationDescriptor(
            final Region region,
            final String objectType,
            final String memberId,
            final String contextualName) {
        this.region = Objects.requireNonNull(region, "region");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
        this.contextualName = Objects.requireNonNull(contextualName, "contextualName");
        if(region.getMemberTag() != null) {
            this.memberId = Objects.requireNonNull(memberId, "memberId");
        } else {
            this.memberId = null;
        }
    }

    public Observation customize(final Observation observation) {
        Observation customized = observation
                .contextualName(contextualName)
                .lowCardinalityKeyValue(OBJECT_TYPE_TAG, objectType);
        if(region.getMemberTag() != null) {
            customized = customized.lowCardinalityKeyValue(region.getMemberTag(), memberId);
        }
        return customized;
    }
}
