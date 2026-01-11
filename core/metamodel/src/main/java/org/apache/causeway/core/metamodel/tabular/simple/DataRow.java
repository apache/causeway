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
package org.apache.causeway.core.metamodel.tabular.simple;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

/**
 * Represents a single domain object (typically an entity instance)
 * and it's associated values as cell elements.
 *
 * @since 2.0 {@index}
 */
public record DataRow(
    @NonNull ManagedObject rowElement) {

    /**
     * Can be none, one or many per table cell.
     * @param column for which to get the cell elements (none, one or many)
     * @param accessMode use PASS_THROUGH for faster processing, when you know,
     * 	that you don't need the full access check processing and also can skip associated event processing
     */
    public Can<ManagedObject> getCellElements(
            final @NonNull DataColumn column,
            final DataTable.AccessMode accessMode) {

    	var initiatedBy = switch(accessMode) {
		    case PASS_THROUGH -> InteractionInitiatedBy.PASS_THROUGH;
		    default -> InteractionInitiatedBy.USER;
		};

        var assoc = column.metamodel();
        return assoc.getSpecialization().fold(
                property-> Can.of(
                		// similar to ManagedProperty#reassessPropertyValue
                		initiatedBy.isPassThrough()
            			|| property.isVisible(rowElement(), InteractionInitiatedBy.USER, VISIBILITY_CONSTRAINT).isAllowed()
                            ? property.get(rowElement(), initiatedBy)
                            : ManagedObject.empty(property.getElementType())),
                collection-> ManagedObjects.unpack(
                		initiatedBy.isPassThrough()
                    	|| collection.isVisible(rowElement(), InteractionInitiatedBy.USER, VISIBILITY_CONSTRAINT).isAllowed()
                            ? collection.get(rowElement(), initiatedBy)
                            : null
                ));
    }

    // we are checking whether a property is visible, constraint by Where.ALL_TABLES (but not by WhatViewer)
    // when accessMode is PASS_THROUGH, we simply assume, that the Where.ALL_TABLES constraint is already honored by previous column filtering logic.
    private final static VisibilityConstraint VISIBILITY_CONSTRAINT = VisibilityConstraint.noViewer(Where.ALL_TABLES);

}
