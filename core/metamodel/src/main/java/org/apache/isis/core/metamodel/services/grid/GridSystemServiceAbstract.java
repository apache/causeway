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
package org.apache.isis.core.metamodel.services.grid;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionPositionFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFaFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.HiddenFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.MemberDescribedFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.MemberNamedFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.PromptStyleFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.RedirectFacetFromActionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.CssClassFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.HiddenFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.MemberDescribedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.MemberNamedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.PagedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.SortedByFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromXml;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.BookmarkPolicyFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.CssClassFaFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.CssClassFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DescribedAsFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.ObjectNamedFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MemberDescribedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MemberNamedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PromptStyleFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.UnchangingFacetForPropertyXml;
import org.apache.isis.core.metamodel.layout.LayoutFacetUtil.MetamodelToGridOverridingVisitor;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.core.metamodel.facetapi.FacetUtil.updateFacet;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(onConstructor_ = {@Inject}, access = AccessLevel.PROTECTED)
@Log4j2
public abstract class GridSystemServiceAbstract<G extends org.apache.isis.applib.layout.grid.Grid>
implements GridSystemService<G> {

    protected final SpecificationLoader specificationLoader;
    protected final TranslationService translationService;
    protected final JaxbService jaxbService;
    protected final MessageService messageService;
    protected final IsisSystemEnvironment isisSystemEnvironment;

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

            if(isisSystemEnvironment.isPrototyping()) {
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
     * @implNote This code uses {@link FacetUtil#updateFacet(Class, java.util.function.Predicate, java.util.Optional, org.apache.isis.core.metamodel.facetapi.FacetHolder)}
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
                        BookmarkPolicyFacetForDomainObjectXml.type(),
                        BookmarkPolicyFacetForDomainObjectXml.class::isInstance,
                        BookmarkPolicyFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        CssClassFacetForDomainObjectXml.type(),
                        CssClassFacetForDomainObjectXml.class::isInstance,
                        CssClassFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        CssClassFaFacetForDomainObjectXml.type(),
                        CssClassFaFacetForDomainObjectXml.class::isInstance,
                        CssClassFaFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        DescribedAsFacetForDomainObjectXml.type(),
                        DescribedAsFacetForDomainObjectXml.class::isInstance,
                        DescribedAsFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec),
                        objectSpec);
                updateFacet(
                        ObjectNamedFacetForDomainObjectXml.type(),
                        ObjectNamedFacetForDomainObjectXml.class::isInstance,
                        ObjectNamedFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec),
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
                updateFacet(LayoutOrderFacetFromXml.create(memberOrderSequence, objectAction));

                updateFacet(
                        LayoutGroupFacetFromXml.type(),
                        LayoutGroupFacetFromXml.class::isInstance,
                        LayoutGroupFacetFromXml.create(groupIdAndName, objectAction),
                        objectAction);

                // fix up the action position if required
                if(actionLayoutDataOwner instanceof FieldSet) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotations.ActionLayout.Position.BELOW ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotations.ActionLayout.Position.RIGHT) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotations.ActionLayout.Position.PANEL);
                    }
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotations.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotations.ActionLayout.Position.PANEL) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotations.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutData.setPosition(null);
                }

                updateFacet(
                        ActionPositionFacetForActionXml.type(),
                        ActionPositionFacetForActionXml.class::isInstance,
                        ActionPositionFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        BookmarkPolicyFacetForActionXml.type(),
                        BookmarkPolicyFacetForActionXml.class::isInstance,
                        BookmarkPolicyFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        CssClassFacetForActionXml.type(),
                        CssClassFacetForActionXml.class::isInstance,
                        CssClassFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        CssClassFaFacetForActionXml.type(),
                        CssClassFaFacetForActionXml.class::isInstance,
                        CssClassFaFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        MemberDescribedFacetForActionXml.type(),
                        MemberDescribedFacetForActionXml.class::isInstance,
                        MemberDescribedFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        HiddenFacetForActionXml.type(),
                        HiddenFacetForActionXml.class::isInstance,
                        HiddenFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        MemberNamedFacetForActionXml.type(),
                        MemberNamedFacetForActionXml.class::isInstance,
                        MemberNamedFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        PromptStyleFacetForActionXml.type(),
                        PromptStyleFacetForActionXml.class::isInstance,
                        PromptStyleFacetForActionXml.create(actionLayoutData, objectAction),
                        objectAction);

                updateFacet(
                        RedirectFacetFromActionXml.type(),
                        RedirectFacetFromActionXml.class::isInstance,
                        RedirectFacetFromActionXml.create(actionLayoutData, objectAction),
                        objectAction);

            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                val oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) {
                    return;
                }

                updateFacet(
                        CssClassFacetForPropertyXml.type(),
                        CssClassFacetForPropertyXml.class::isInstance,
                        CssClassFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MemberDescribedFacetForPropertyXml.type(),
                        MemberDescribedFacetForPropertyXml.class::isInstance,
                        MemberDescribedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        HiddenFacetForPropertyXml.type(),
                        HiddenFacetForPropertyXml.class::isInstance,
                        HiddenFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        LabelAtFacetForPropertyXml.type(),
                        LabelAtFacetForPropertyXml.class::isInstance,
                        LabelAtFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MultiLineFacetForPropertyXml.type(),
                        MultiLineFacetForPropertyXml.class::isInstance,
                        MultiLineFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        MemberNamedFacetForPropertyXml.type(),
                        MemberNamedFacetForPropertyXml.class::isInstance,
                        MemberNamedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        PromptStyleFacetForPropertyXml.type(),
                        PromptStyleFacetForPropertyXml.class::isInstance,
                        PromptStyleFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        RenderedAdjustedFacetForPropertyXml.type(),
                        RenderedAdjustedFacetForPropertyXml.class::isInstance,
                        RenderedAdjustedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        UnchangingFacetForPropertyXml.type(),
                        UnchangingFacetForPropertyXml.class::isInstance,
                        UnchangingFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                updateFacet(
                        TypicalLengthFacetForPropertyXml.type(),
                        TypicalLengthFacetForPropertyXml.class::isInstance,
                        TypicalLengthFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation),
                        oneToOneAssociation);

                // Layout group-name based on owning property group, Layout sequence monotonically increasing
                // nb for any given field set the sequence won't reset to zero; however this is what we want so that
                // table columns are shown correctly (by fieldset, then property order within that fieldset).
                final FieldSet fieldSet = propertyLayoutData.getOwner();

                updateFacet(LayoutOrderFacetFromXml.create(propertySequence.incrementAndGet(), oneToOneAssociation));

                updateFacet(
                        LayoutGroupFacetFromXml.type(),
                        LayoutGroupFacetFromXml.class::isInstance,
                        LayoutGroupFacetFromXml.create(fieldSet, oneToOneAssociation),
                        oneToOneAssociation);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                val oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                updateFacet(
                        CssClassFacetForCollectionXml.type(),
                        CssClassFacetForCollectionXml.class::isInstance,
                        CssClassFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        DefaultViewFacetForCollectionXml.type(),
                        DefaultViewFacetForCollectionXml.class::isInstance,
                        DefaultViewFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        MemberDescribedFacetForCollectionXml.type(),
                        MemberDescribedFacetForCollectionXml.class::isInstance,
                        MemberDescribedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        HiddenFacetForCollectionXml.type(),
                        HiddenFacetForCollectionXml.class::isInstance,
                        HiddenFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        MemberNamedFacetForCollectionXml.type(),
                        MemberNamedFacetForCollectionXml.class::isInstance,
                        MemberNamedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        PagedFacetForCollectionXml.type(),
                        PagedFacetForCollectionXml.class::isInstance,
                        PagedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(
                        SortedByFacetForCollectionXml.type(),
                        SortedByFacetForCollectionXml.class::isInstance,
                        SortedByFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation),
                        oneToManyAssociation);

                updateFacet(LayoutOrderFacetFromXml.create(collectionSequence++, oneToManyAssociation));
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
