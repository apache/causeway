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
package org.apache.causeway.applib.services.columnorder;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PrecedingParamsPolicy;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.metamodel.MetaModelService.AssociationsLookup;
import org.apache.causeway.applib.util.Listing;
import org.apache.causeway.applib.util.Listing.MergePolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.springframework.lang.Nullable;

import lombok.RequiredArgsConstructor;

/**
 * Allows uploading of column order definition, that overrules the default lookup for such information.
 *
 * @since 4.0 {@index}
 */
@Action(
        domainEvent = Object_patchColumnOrder.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING)
@ActionLayout(
        cssClassFa = "solid file-arrow-up",
		describedAs = "Uploads table column order, to be stored in memory for this object type. "
				+ "It overrules the default column order definition lookup. "
				+ "On application restart this information is lost.",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.2.4"
)
//framework provided domain objects and mixins should explicitly specify their introspection policy
@DomainObject(nature=Nature.MIXIN, mixinMethod = "act", introspection = Introspection.ANNOTATION_REQUIRED)
@RequiredArgsConstructor
public class Object_patchColumnOrder {

	public static class ActionDomainEvent
	extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_patchColumnOrder> {}

	@Inject MetaModelService metaModelService;

    private final Object mixee;

    @MemberSupport public Object act(

            @Parameter
            @ParameterLayout(describedAs = "The 'Feature', for which the patch is to be applied (in-memory), "
                    + "that is, "
                    + "either for a one-to-many relation (a PARENTED Collection), "
                    + "or a domain-type (applies to all STANDALONE Collections of that element-type). "
                    + "The Feature either represents a particular one-to-many relation of this domain-type or "
                    + "represents the domain-type itself or one of the domain-type's super types. "
                    + "The Apache Causeway Programming Model also supports {parent-type, element-type} scoped "
                    + "column order definitions, which are not covered by patching yet.")
    		final Identifier featureId,

    		@Parameter(precedingParamsPolicy = PrecedingParamsPolicy.RESET)
            @ParameterLayout(multiLine = 20, describedAs = "Automaticly filled in are all currently enabled and available "
            		+ "column-ids, one per line. Those available (but not enabled) are commented out. "
            		+ "Reorder or remove column-ids as desired.")
            final String columnListing) {

    	var listing = listingHandler().parseListing(columnListing);
    	var columns = Can.ofStream(listing.streamEnabled());

    	metaModelService.patchColumnOrder(featureId, columns);
        return mixee;
    }

    @MemberSupport public List<Identifier> choicesFeatureId() {
        return Stream.concat(
                metaModelService.streamTypeHierarchy(mixee.getClass()),
                metaModelService.streamCollections(mixee.getClass()))
    		.collect(Collectors.toList());
    }

    @MemberSupport public String defaultColumnListing(final @Nullable Identifier featureId) {
        if(featureId==null)
            return "# no feature selected";

        if(featureId.type().isCollection())
            return listing(
	                metaModelService.parentedAssociationsForColumnRendering(mixee, featureId, AssociationsLookup.AVAILABLE),
	                metaModelService.parentedAssociationsForColumnRendering(mixee, featureId, AssociationsLookup.ENABLED))
        		.toString();

        if(featureId.type().isClass())
            return listing(
	                metaModelService.standaloneAssociationsForColumnRendering(featureId.logicalType(), AssociationsLookup.AVAILABLE),
	                metaModelService.standaloneAssociationsForColumnRendering(featureId.logicalType(), AssociationsLookup.ENABLED))
        		.toString();

        throw _Exceptions.illegalArgument("unsupported feature type %s", featureId.type());
    }

    // -- HELPER

    private Listing<String> listing(final Stream<Identifier> availableIds, final Stream<Identifier> enabledIds) {
        // all column candidates
        var available = listingHandler()
        		.createListing(availableIds.map(Identifier::memberLogicalName));

        // all columns currently rendered
        var enabled = listingHandler()
        		.createListing(enabledIds.map(Identifier::memberLogicalName));

        return enabled.merge(MergePolicy.ADD_NEW_AS_DISABLED, available, "#AVAILABLE" /* custom merge header */);
    }

    private final static Listing.LineAdapter<String> listingHandler() {
    	return Listing.lineAdapter(String.class, UnaryOperator.identity(), UnaryOperator.identity(), UnaryOperator.identity());
    }

}
