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

import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation.Detail;
import org.apache.causeway.core.config.observation.CausewayObservationNaming;
import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;

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
        PAGE_PREPARATION("causeway.wicket.page.prepare", null, true),
        COLLECTION_PREPARATION(
                "causeway.wicket.collection.prepare", "causeway.collection.id", true),
        ROW_PREPARATION(
                "causeway.wicket.collection.row.prepare", "causeway.collection.id", true),
        PAGE("causeway.wicket.page.render", null, false),
        FIELDSET("causeway.wicket.fieldset.render", "causeway.fieldset.id", false),
        PROPERTY("causeway.wicket.property.render", "causeway.property.id", false),
        COLLECTION("causeway.wicket.collection.render", "causeway.collection.id", false),
        TABLE("causeway.wicket.collection.table.render", "causeway.collection.id", false),
        TABLE_HEADER(
                "causeway.wicket.collection.table.header.render", "causeway.collection.id", false),
        TABLE_BODY(
                "causeway.wicket.collection.table.body.render", "causeway.collection.id", false),
        TABLE_FOOTER(
                "causeway.wicket.collection.table.footer.render", "causeway.collection.id", false),
        ROW("causeway.wicket.collection.row.render", "causeway.collection.id", false),
        ACTION("causeway.wicket.action.render", "causeway.action.id", false),
        ACTION_PROMPT("causeway.wicket.action.prompt.render", "causeway.action.id", false);

        private final String observationName;
        private final String memberTag;
        private final boolean preparation;
    }

    public static WicketRenderObservationDescriptor pagePreparation(final String objectType) {
        return new WicketRenderObservationDescriptor(
                Region.PAGE_PREPARATION,
                objectType,
                null,
                CausewayObservationNaming.forType("prepare", objectType));
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

    public static WicketRenderObservationDescriptor collectionPreparation(
            final String objectType,
            final String collectionId) {
        return new WicketRenderObservationDescriptor(
                Region.COLLECTION_PREPARATION,
                objectType,
                collectionId,
                CausewayObservationNaming.forRegion(
                        "prepare", "collection", collectionId));
    }

    public static WicketRenderObservationDescriptor rowPreparation(
            final String elementObjectType,
            final String collectionId) {
        return new WicketRenderObservationDescriptor(
                Region.ROW_PREPARATION,
                elementObjectType,
                collectionId,
                CausewayObservationNaming.forType(
                        "prepare row", elementObjectType));
    }

    public static WicketRenderObservationDescriptor collection(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.COLLECTION, objectType, collectionId, "collection");
    }

    public static WicketRenderObservationDescriptor table(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.TABLE, objectType, collectionId, "table");
    }

    public static WicketRenderObservationDescriptor tableHeader(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.TABLE_HEADER, objectType, collectionId, "table header");
    }

    public static WicketRenderObservationDescriptor tableBody(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.TABLE_BODY, objectType, collectionId, "table body");
    }

    public static WicketRenderObservationDescriptor tableFooter(
            final String objectType,
            final String collectionId) {
        return regionDescriptor(
                Region.TABLE_FOOTER, objectType, collectionId, "table footer");
    }

    public static WicketRenderObservationDescriptor row(
            final String elementObjectType,
            final String collectionId) {
        return new WicketRenderObservationDescriptor(
                Region.ROW,
                elementObjectType,
                collectionId,
                CausewayObservationNaming.forType(
                        "render row", elementObjectType));
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

    public Detail minimumDetail() {
        switch (region) {
            case PAGE_PREPARATION:
            case PAGE:
            case ACTION_PROMPT:
                return Detail.PAGE;
            case COLLECTION_PREPARATION:
            case FIELDSET:
            case COLLECTION:
            case TABLE:
            case TABLE_HEADER:
            case TABLE_BODY:
            case TABLE_FOOTER:
                return Detail.REGIONS;
            case ROW_PREPARATION:
            case ROW:
                return Detail.ROWS;
            case PROPERTY:
            case ACTION:
                return Detail.MEMBERS;
            default:
                throw new IllegalStateException("Unclassified Wicket observation region " + region);
        }
    }

    public boolean isStructural() {
        return minimumDetail().ordinal() <= Detail.REGIONS.ordinal();
    }

    public boolean isCollectionAggregate() {
        return region == Region.COLLECTION_PREPARATION || region == Region.COLLECTION;
    }

    public boolean isRowCallback() {
        return region == Region.ROW_PREPARATION || region == Region.ROW;
    }

    public boolean isLogicalCellCallback() {
        return region == Region.PROPERTY || region == Region.ACTION;
    }

    /**
     * Nominates this descriptor as the foreground request's semantic outcome when applicable.
     */
    public void nominateSemanticTraceName() {
        switch (region) {
            case PAGE:
                CausewaySemanticTraceNamer.nominateView(objectType);
                break;
            case ACTION_PROMPT:
                final int parameterSeparator = memberId.indexOf('(');
                final String logicalMemberIdentifier = parameterSeparator >= 0
                        ? memberId.substring(0, parameterSeparator)
                        : memberId;
                CausewaySemanticTraceNamer.nominatePrompt(
                        logicalMemberIdentifier, memberId);
                break;
            default:
                break;
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
