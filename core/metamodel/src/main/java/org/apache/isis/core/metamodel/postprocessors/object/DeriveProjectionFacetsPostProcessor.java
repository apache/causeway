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

package org.apache.isis.core.metamodel.postprocessors.object;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.EditingEnabledFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.projection.ProjectionFacetFromProjectingProperty;
import org.apache.isis.core.metamodel.facets.object.projection.ident.IconFacetDerivedFromProjectionFacet;
import org.apache.isis.core.metamodel.facets.object.projection.ident.TitleFacetDerivedFromProjectionFacet;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObject;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFacets;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

import lombok.Setter;
import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveProjectionFacetsPostProcessor
implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        deriveProjectionFacets(objectSpecification);
    }

    private static void deriveProjectionFacets(final ObjectSpecification objectSpecification) {
        val projectionFacet = ProjectionFacetFromProjectingProperty.create(objectSpecification);
        if (projectionFacet == null) {
            return;
        }
        FacetUtil.addFacet(projectionFacet);
        val titleFacet = objectSpecification.getFacet(TitleFacet.class);
        if(canOverwrite(titleFacet)) {
            FacetUtil.addFacet(new TitleFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val iconFacet = objectSpecification.getFacet(IconFacet.class);
        if(canOverwrite(iconFacet)) {
            FacetUtil.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val cssClassFacet = objectSpecification.getFacet(CssClassFacet.class);
        if(canOverwrite(cssClassFacet)) {
            FacetUtil.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
    }

    private static boolean canOverwrite(final Facet facet) {
        return facet == null || facet.isFallback() || facet.isDerived();
    }


}
