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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridSystemService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionPositionFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.CssClassFaFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.CssClassFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberDescribedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberNamedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.PromptStyleFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.RedirectFacetFromActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.CssClassFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.HiddenFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberDescribedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberNamedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.PagedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.SortedByFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.tabledec.CollectionLayoutTableDecorationFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.BookmarkPolicyFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.CssClassFaFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.CssClassFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectDescribedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectNamedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.tabledec.DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberDescribedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberNamedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.PromptStyleFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.UnchangingFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.layout.LayoutFacetUtil.MetamodelToGridOverridingVisitor;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.apache.causeway.core.metamodel.facetapi.FacetUtil.updateFacet;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(onConstructor_ = {@Inject}, access = AccessLevel.PROTECTED)
@Log4j2
public abstract class GridSystemServiceAbstract<G extends org.apache.causeway.applib.layout.grid.Grid>
implements GridSystemService<G> {

    protected final SpecificationLoader specificationLoader;
    protected final TranslationService translationService;
    protected final JaxbService jaxbService;
    protected final MessageService messageService;
    protected final CausewaySystemEnvironment causewaySystemEnvironment;

    @Override
    public void normalize(final G grid, final Class<?> domainClass) {

        if(!gridImplementation().isAssignableFrom(grid.getClass())) {
            // ignore any other grid implementations
            return;
        }

        final boolean valid = validateAndNormalize(grid, domainClass);
        if (valid) {
            overwriteFacets(grid, domainClass);
            if(log.isDebugEnabled()) {
                log.debug("Grid:\n\n{}\n\n", jaxbService.toXml(grid));
            }
        } else {

            if(causewaySystemEnvironment.isPrototyping()) {
                messageService.warnUser("Grid metadata errors for " + grid.getDomainClass().getName() + "; check the error log");
            }
            log.error("Grid metadata errors:\n\n{}\n\n", jaxbService.toXml(grid));
        }
    }


    /**
     * Mandatory hook method for subclasses, where they must ensure that all object members (properties, collections
     * and actions) are in the grid metadata, typically by deriving this information from other existing metadata
     * (eg facets from annotations) or just by applying default rules.
     */
    protected abstract boolean validateAndNormalize(
            final Grid grid,
            final Class<?> domainClass);


    /**
     * Overwrites (replaces) any existing facets in the metamodel with info taken from the grid.
     *
     * @implNote This code uses {@link FacetUtil#updateFacet(Class, java.util.function.Predicate, java.util.Optional, org.apache.causeway.core.metamodel.facetapi.FacetHolder)}
     * because the layout might be reloaded from XML if reloading is supported.
     */
    private void overwriteFacets(
            final G fcGrid,
            final Class<?> domainClass) {

        val objectSpec = specificationLoader.specForTypeElseFail(domainClass);

        val oneToOneAssociationById = ObjectMember.mapById(objectSpec.streamProperties(MixedIn.INCLUDED));
        val oneToManyAssociationById = ObjectMember.mapById(objectSpec.streamCollections(MixedIn.INCLUDED));
        val objectActionById = ObjectMember.mapById(objectSpec.streamRuntimeActions(MixedIn.INCLUDED));

        final AtomicInteger propertySequence = new AtomicInteger(0);
        fcGrid.visit(new Grid.VisitorAdapter() {
            private int collectionSequence = 1;

            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {

                updateFacet(
                        BookmarkPolicyFacetForDomainObjectLayoutXml.type(),
                        BookmarkPolicyFacetForDomainObjectLayoutXml.class::isInstance,
                        BookmarkPolicyFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        CssClassFacetForDomainObjectLayoutXml.type(),
                        CssClassFacetForDomainObjectLayoutXml.class::isInstance,
                        CssClassFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        CssClassFaFacetForDomainObjectLayoutXml.type(),
                        CssClassFaFacetForDomainObjectLayoutXml.class::isInstance,
                        CssClassFaFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        ObjectDescribedFacetForDomainObjectLayoutXml.type(),
                        ObjectDescribedFacetForDomainObjectLayoutXml.class::isInstance,
                        ObjectDescribedFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        ObjectNamedFacetForDomainObjectLayoutXml.type(),
                        ObjectNamedFacetForDomainObjectLayoutXml.class::isInstance,
                        ObjectNamedFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml.type(),
                        DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml.class::isInstance,
                        DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                val actionLayoutDataOwner = actionLayoutData.getOwner();
                val objectAction = objectActionById.get(actionLayoutData.getId());
                if(objectAction == null) {
                    return;
                }

                GroupIdAndName groupIdAndName = null;
                int memberOrderSequence;
                if(actionLayoutDataOwner instanceof FieldSet) {
                    val fieldSet = (FieldSet) actionLayoutDataOwner;
                    for (val propertyLayoutData : fieldSet.getProperties()) {
                        // any will do; choose the first one that we know is valid
                        if(oneToOneAssociationById.containsKey(propertyLayoutData.getId())) {
                            groupIdAndName = GroupIdAndName.forPropertyLayoutData(propertyLayoutData)
                                    .orElse(null);
                            break;
                        }
                    }
                    memberOrderSequence = actionPropertyGroupSequence++;
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    groupIdAndName = GroupIdAndName
                            .forPropertyLayoutData((PropertyLayoutData) actionLayoutDataOwner)
                            .orElse(null);
                    memberOrderSequence = actionPropertySequence++;
                } else if(actionLayoutDataOwner instanceof CollectionLayoutData) {
                    groupIdAndName = GroupIdAndName
                            .forCollectionLayoutData((CollectionLayoutData) actionLayoutDataOwner)
                            .orElse(null);
                    memberOrderSequence = actionCollectionSequence++;
                } else {
                    // don't add: any existing metadata should be preserved
                    groupIdAndName = null;
                    memberOrderSequence = actionDomainObjectSequence++;
                }
                updateFacet(LayoutOrderFacetForLayoutXml.create(memberOrderSequence, objectAction));

                updateFacet(
                        LayoutGroupFacetForLayoutXml.type(),
                        LayoutGroupFacetForLayoutXml.class::isInstance,
                        LayoutGroupFacetForLayoutXml.create(groupIdAndName, objectAction),
                        objectAction);

                // fix up the action position if required
                if(actionLayoutDataOwner instanceof FieldSet) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutData.setPosition(org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutData.setPosition(org.apache.causeway.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutData.setPosition(null);
                }

                updateFacet(
                        ActionPositionFacetForActionLayoutXml.type(),
                        ActionPositionFacetForActionLayoutXml.class::isInstance,
                        ActionPositionFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        BookmarkPolicyFacetForActionLayoutXml.type(),
                        BookmarkPolicyFacetForActionLayoutXml.class::isInstance,
                        BookmarkPolicyFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        CssClassFacetForActionLayoutXml.type(),
                        CssClassFacetForActionLayoutXml.class::isInstance,
                        CssClassFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        CssClassFaFacetForActionLayoutXml.type(),
                        CssClassFaFacetForActionLayoutXml.class::isInstance,
                        CssClassFaFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        MemberDescribedFacetForActionLayoutXml.type(),
                        MemberDescribedFacetForActionLayoutXml.class::isInstance,
                        MemberDescribedFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        HiddenFacetForActionLayoutXml.type(),
                        HiddenFacetForActionLayoutXml.class::isInstance,
                        HiddenFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        MemberNamedFacetForActionLayoutXml.type(),
                        MemberNamedFacetForActionLayoutXml.class::isInstance,
                        MemberNamedFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        PromptStyleFacetForActionLayoutXml.type(),
                        PromptStyleFacetForActionLayoutXml.class::isInstance,
                        PromptStyleFacetForActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        RedirectFacetFromActionLayoutXml.type(),
                        RedirectFacetFromActionLayoutXml.class::isInstance,
                        RedirectFacetFromActionLayoutXml.create(actionLayoutData, objectAction),
                        objectAction);

            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                val oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) {
                    return;
                }

                updateFacet(
                        CssClassFacetForPropertyLayoutXml.type(),
                        CssClassFacetForPropertyLayoutXml.class::isInstance,
                        CssClassFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MemberDescribedFacetForPropertyLayoutXml.type(),
                        MemberDescribedFacetForPropertyLayoutXml.class::isInstance,
                        MemberDescribedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        HiddenFacetForPropertyLayoutXml.type(),
                        HiddenFacetForPropertyLayoutXml.class::isInstance,
                        HiddenFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        LabelAtFacetForPropertyLayoutXml.type(),
                        LabelAtFacetForPropertyLayoutXml.class::isInstance,
                        LabelAtFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MultiLineFacetForPropertyLayoutXml.type(),
                        MultiLineFacetForPropertyLayoutXml.class::isInstance,
                        MultiLineFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MemberNamedFacetForPropertyLayoutXml.type(),
                        MemberNamedFacetForPropertyLayoutXml.class::isInstance,
                        MemberNamedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        PromptStyleFacetForPropertyLayoutXml.type(),
                        PromptStyleFacetForPropertyLayoutXml.class::isInstance,
                        PromptStyleFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        RenderedAdjustedFacetForPropertyLayoutXml.type(),
                        RenderedAdjustedFacetForPropertyLayoutXml.class::isInstance,
                        RenderedAdjustedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        UnchangingFacetForPropertyLayoutXml.type(),
                        UnchangingFacetForPropertyLayoutXml.class::isInstance,
                        UnchangingFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        TypicalLengthFacetForPropertyLayoutXml.type(),
                        TypicalLengthFacetForPropertyLayoutXml.class::isInstance,
                        TypicalLengthFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                // Layout group-name based on owning property group, Layout sequence monotonically increasing
                // nb for any given field set the sequence won't reset to zero; however this is what we want so that
                // table columns are shown correctly (by fieldset, then property order within that fieldset).
                final FieldSet fieldSet = propertyLayoutData.getOwner();

                updateFacet(LayoutOrderFacetForLayoutXml.create(propertySequence.incrementAndGet(), oneToOneAssociation));

                updateFacet(
                        LayoutGroupFacetForLayoutXml.type(),
                        LayoutGroupFacetForLayoutXml.class::isInstance,
                        LayoutGroupFacetForLayoutXml.create(fieldSet, oneToOneAssociation),
                        oneToOneAssociation);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                val oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                updateFacet(
                        CssClassFacetForCollectionLayoutXml.type(),
                        CssClassFacetForCollectionLayoutXml.class::isInstance,
                        CssClassFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        DefaultViewFacetForCollectionLayoutXml.type(),
                        DefaultViewFacetForCollectionLayoutXml.class::isInstance,
                        DefaultViewFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        CollectionLayoutTableDecorationFacetForCollectionLayoutXml.type(),
                        CollectionLayoutTableDecorationFacetForCollectionLayoutXml.class::isInstance,
                        CollectionLayoutTableDecorationFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        MemberDescribedFacetForCollectionLayoutXml.type(),
                        MemberDescribedFacetForCollectionLayoutXml.class::isInstance,
                        MemberDescribedFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        HiddenFacetForCollectionLayoutXml.type(),
                        HiddenFacetForCollectionLayoutXml.class::isInstance,
                        HiddenFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        MemberNamedFacetForCollectionLayoutXml.type(),
                        MemberNamedFacetForCollectionLayoutXml.class::isInstance,
                        MemberNamedFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        PagedFacetForCollectionLayoutXml.type(),
                        PagedFacetForCollectionLayoutXml.class::isInstance,
                        PagedFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        SortedByFacetForCollectionLayoutXml.type(),
                        SortedByFacetForCollectionLayoutXml.class::isInstance,
                        SortedByFacetForCollectionLayoutXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(LayoutOrderFacetForLayoutXml.create(collectionSequence++, oneToManyAssociation));
            }
        });
    }

    @Value(staticConstructor = "of")
    protected static class SurplusAndMissing {
        public final Set<String> surplus;
        public final Set<String> missing;
    }

    protected static SurplusAndMissing surplusAndMissing(final Set<String> first, final Set<String> second){
        val firstNotSecond = _Sets.minus(first, second, LinkedHashSet::new); // preserve order
        val secondNotFirst = _Sets.minus(second, first, LinkedHashSet::new); // preserve order
        return SurplusAndMissing.of(firstNotSecond, secondNotFirst);
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public void complete(final G grid, final Class<?> domainClass) {
        normalize(grid, domainClass);
        val objectSpec = specificationLoader.specForTypeElseFail(domainClass);
        grid.visit(MetamodelToGridOverridingVisitor.of(objectSpec));
    }


    @Programmatic
    @Override
    public void minimal(final G grid, final Class<?> domainClass) {
        normalize(grid, domainClass);
        grid.visit(new Grid.VisitorAdapter() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actionLayoutData.getOwner().getActions().remove(actionLayoutData);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                collectionLayoutData.getOwner().getCollections().remove(collectionLayoutData);
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                propertyLayoutData.getOwner().getProperties().remove(propertyLayoutData);
            }

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                final DomainObjectLayoutDataOwner owner = domainObjectLayoutData.getOwner();
                owner.setDomainObject(new DomainObjectLayoutData());
            }
        });
    }



}
