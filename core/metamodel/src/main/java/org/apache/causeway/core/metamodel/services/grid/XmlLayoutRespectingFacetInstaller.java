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

import static org.apache.causeway.core.metamodel.facetapi.FacetUtil.updateFacet;
import static org.apache.causeway.core.metamodel.facetapi.FacetUtil.updateFacetIfPresent;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionPositionFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.CssClassFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.FaFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberDescribedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberNamedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.CssClassFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.HiddenFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberDescribedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberNamedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.PagedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.SortedByFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.tabledec.TableDecoratorFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.BookmarkPolicyFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.CssClassFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.FaFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectDescribedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectNamedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.tabledec.TableDecoratorFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberDescribedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberNamedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.UnchangingFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter @Accessors(fluent = true)
final class XmlLayoutRespectingFacetInstaller {
    private final SpecificationLoader specLoader;

    /**
     * Overwrites (replaces) any existing facets in the metamodel with info taken from the grid.
     *
     * @implNote This code uses {@link FacetUtil#updateFacet(Facet)}
     * because the layout might be reloaded from XML if reloading is supported.
     */
    void installFacets(
            final LayoutKey layoutKey,
            final BSGrid bsGrid) {

        var objectSpec = specLoader.specForTypeElseFail(layoutKey.domainClass());

        var oneToOneAssociationById = ObjectMember.mapById(objectSpec.streamProperties(MixedIn.INCLUDED));
        var oneToManyAssociationById = ObjectMember.mapById(objectSpec.streamCollections(MixedIn.INCLUDED));
        var objectActionById = ObjectMember.mapById(objectSpec.streamRuntimeActions(MixedIn.INCLUDED));

        // governs, whether annotations win over XML grid, based on whether XML grid is fallback or 'explicit'
        var precedence = bsGrid.fallback()
                ? Facet.Precedence.LOW // fallback case: XML layout is overruled by layout from annotations
                : Facet.Precedence.HIGH; // non-fallback case: XML layout overrules layout from annotations

        bsGrid.visit(new BSElementVisitor() {
            private int propertySequence = 1;
            private int collectionSequence = 1;

            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {

                var qualifier = layoutKey.layoutIfAny();

                updateFacetIfPresent(
                        BookmarkPolicyFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
                updateFacetIfPresent(
                        CssClassFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
                updateFacetIfPresent(
                        FaFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
                updateFacetIfPresent(
                        ObjectDescribedFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
                updateFacetIfPresent(
                        ObjectNamedFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
                updateFacetIfPresent(
                        TableDecoratorFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence, qualifier));
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {

                var actionLayoutDataOwner = actionLayoutData.getOwner();
                var objectAction = objectActionById.get(actionLayoutData.getId());
                if(objectAction == null) return;

                var qualifier = layoutKey.layoutIfAny();

                {
                    GroupIdAndName groupIdAndName = null;
                    int memberOrderSequence;
                    if(actionLayoutDataOwner instanceof FieldSet) {
                        for (var propertyLayoutData : ((FieldSet)actionLayoutDataOwner).getProperties()) {
                            // any will do; choose the first one that we know is valid
                            if(oneToOneAssociationById.containsKey(propertyLayoutData.getId())) {
                                groupIdAndName = GroupIdAndName
                                        .forPropertyLayoutData(propertyLayoutData)
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
                    updateFacet(
                            LayoutOrderFacetForLayoutXml.create(memberOrderSequence, objectAction, precedence, qualifier));

                    //XXX hotfix: always override LayoutGroupFacetFromActionLayoutAnnotation, otherwise actions are not shown - don't know why
                    var precedenceHotfix = bsGrid.fallback()
                            ? Facet.Precedence.DEFAULT
                            : Facet.Precedence.HIGH;

                    updateFacetIfPresent(
                            LayoutGroupFacetForLayoutXml.create(groupIdAndName, objectAction, precedenceHotfix, qualifier));
                }

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

                updateFacetIfPresent(
                        ActionPositionFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        CssClassFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        FaFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        MemberDescribedFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        HiddenFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        MemberNamedFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence, qualifier));

                updateFacetIfPresent(
                        PromptStyleFacet.createForActionLayoutXml(actionLayoutData, objectAction, precedence, qualifier));
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                var oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) return;

                var qualifier = layoutKey.layoutIfAny();

                updateFacetIfPresent(
                        CssClassFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        MemberDescribedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        HiddenFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        LabelAtFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        MultiLineFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        MemberNamedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        PromptStyleFacet.createForPropertyLayoutXml(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        RenderedAdjustedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        UnchangingFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        TypicalLengthFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence, qualifier));

                // Layout group-name based on owning property group, Layout sequence monotonically increasing
                // nb for any given field set the sequence won't reset to zero; however this is what we want so that
                // table columns are shown correctly (by fieldset, then property order within that fieldset).
                final FieldSet fieldSet = propertyLayoutData.getOwner();

                updateFacet(
                        LayoutOrderFacetForLayoutXml.create(propertySequence++, oneToOneAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        LayoutGroupFacetForLayoutXml.create(fieldSet, oneToOneAssociation, precedence, qualifier));
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                var oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) return;

                var qualifier = layoutKey.layoutIfAny();

                updateFacetIfPresent(
                        CssClassFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        DefaultViewFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        TableDecoratorFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        MemberDescribedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        HiddenFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        MemberNamedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        PagedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacetIfPresent(
                        SortedByFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence, qualifier));

                updateFacet(LayoutOrderFacetForLayoutXml
                        .create(collectionSequence++, oneToManyAssociation, precedence, qualifier));
            }

        });
    }

}
