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
        ACTION("causeway.wicket.action.render", "causeway.action.id");

        private final String observationName;
        private final String memberTag;
    }

    public static WicketRenderObservationDescriptor page(final String objectType) {
        return new WicketRenderObservationDescriptor(Region.PAGE, objectType, null);
    }

    public static WicketRenderObservationDescriptor fieldset(
            final String objectType,
            final String fieldsetId) {
        return new WicketRenderObservationDescriptor(
                Region.FIELDSET,
                objectType,
                fieldsetId != null && !fieldsetId.isEmpty() ? fieldsetId : "<default>");
    }

    public static WicketRenderObservationDescriptor property(
            final String objectType,
            final String propertyId) {
        return new WicketRenderObservationDescriptor(Region.PROPERTY, objectType, propertyId);
    }

    public static WicketRenderObservationDescriptor collection(
            final String objectType,
            final String collectionId) {
        return new WicketRenderObservationDescriptor(Region.COLLECTION, objectType, collectionId);
    }

    public static WicketRenderObservationDescriptor action(
            final String objectType,
            final String actionId) {
        return new WicketRenderObservationDescriptor(Region.ACTION, objectType, actionId);
    }

    private final Region region;
    private final String objectType;
    private final String memberId;

    private WicketRenderObservationDescriptor(
            final Region region,
            final String objectType,
            final String memberId) {
        this.region = Objects.requireNonNull(region, "region");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
        if(region.getMemberTag() != null) {
            this.memberId = Objects.requireNonNull(memberId, "memberId");
        } else {
            this.memberId = null;
        }
    }

    public Observation customize(final Observation observation) {
        Observation customized = observation
                .contextualName(region.getObservationName())
                .lowCardinalityKeyValue(OBJECT_TYPE_TAG, objectType);
        if(region.getMemberTag() != null) {
            customized = customized.lowCardinalityKeyValue(region.getMemberTag(), memberId);
        }
        return customized;
    }
}
