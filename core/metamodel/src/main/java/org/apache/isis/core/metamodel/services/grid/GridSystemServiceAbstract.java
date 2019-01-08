/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.grid;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.HasBookmarking;
import org.apache.isis.applib.layout.component.HasCssClass;
import org.apache.isis.applib.layout.component.HasCssClassFa;
import org.apache.isis.applib.layout.component.HasDescribedAs;
import org.apache.isis.applib.layout.component.HasHidden;
import org.apache.isis.applib.layout.component.HasNamed;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionPositionFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFaFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.DescribedAsFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.HiddenFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.NamedFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.PromptStyleFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.RedirectFacetFromActionXml;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.layout.CssClassFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DescribedAsFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.HiddenFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.NamedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.PagedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.SortedByFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetXml;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.BookmarkPolicyFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.CssClassFaFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.CssClassFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DescribedAsFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.NamedFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.PluralFacetForDomainObjectXml;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.DescribedAsFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.NamedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PromptStyleFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.UnchangingFacetForPropertyXml;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class GridSystemServiceAbstract<G extends org.apache.isis.applib.layout.grid.Grid> implements GridSystemService<G> {

    private static final Logger LOG = LoggerFactory.getLogger(GridSystemServiceAbstract.class);

    private final Class<G> gridImplementation;
    private final String tns;
    private final String schemaLocation;

    public GridSystemServiceAbstract(
            final Class<G> gridImplementation,
            final String tns,
            final String schemaLocation) {
        this.gridImplementation = gridImplementation;
        this.tns = tns;
        this.schemaLocation = schemaLocation;
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public Class<G> gridImplementation() {
        return gridImplementation;
    }

    @Programmatic
    @Override
    public String tns() {
        return tns;
    }

    @Programmatic
    @Override
    public String schemaLocation() {
        return schemaLocation;
    }


    // //////////////////////////////////////

    @Programmatic
    @Override
    public void normalize(final G grid, final Class<?> domainClass) {

        if(!gridImplementation.isAssignableFrom(grid.getClass())) {
            // ignore any other grid implementations
            return;
        }

        final boolean valid =
                validateAndNormalize(
                    grid, domainClass);
        if (valid) {
            overwriteFacets(grid, domainClass);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Grid:\n\n{}\n\n", jaxbService.toXml(grid));
            }
        } else {

            if(!deploymentCategoryProvider.getDeploymentCategory().isProduction()) {
                messageService.warnUser("Grid metadata errors for " + grid.getDomainClass().getName() + "; check the error log");
            }
            LOG.error("Grid metadata errors:\n\n{}\n\n", jaxbService.toXml(grid));
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
     * <p>
     *     This code uses {@link FacetUtil#addOrReplaceFacet(Facet)} because the layout might be changed multiple times.
     * </p>
     */
    protected void overwriteFacets(
            final G fcGrid,
            final Class<?> domainClass) {


        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainClass);

        final Map<String, OneToOneAssociation> oneToOneAssociationById =
                ObjectMember.Util.mapById(getOneToOneAssociations(objectSpec));
        final Map<String, OneToManyAssociation> oneToManyAssociationById =
                ObjectMember.Util.mapById(getOneToManyAssociations(objectSpec));
        final Map<String, ObjectAction> objectActionById =
                ObjectMember.Util.mapById(objectSpec.getObjectActions(Contributed.INCLUDED));


        final AtomicInteger propertySequence = new AtomicInteger(0);
        fcGrid.visit(new Grid.VisitorAdapter() {
            private int collectionSequence = 1;

            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {

                FacetUtil.addOrReplaceFacet(BookmarkPolicyFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
                FacetUtil.addOrReplaceFacet(CssClassFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
                FacetUtil.addOrReplaceFacet(CssClassFaFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
                FacetUtil.addOrReplaceFacet(DescribedAsFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
                FacetUtil.addOrReplaceFacet(NamedFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
                FacetUtil.addOrReplaceFacet(PluralFacetForDomainObjectXml.create(domainObjectLayoutData, objectSpec));
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                final ActionLayoutDataOwner actionLayoutDataOwner = actionLayoutData.getOwner();
                final ObjectAction objectAction = objectActionById.get(actionLayoutData.getId());
                if(objectAction == null) {
                    return;
                }

                String memberOrderName = null;
                int memberOrderSequence;
                if(actionLayoutDataOwner instanceof FieldSet) {
                    final FieldSet fieldSet = (FieldSet) actionLayoutDataOwner;
                    final List<PropertyLayoutData> properties = fieldSet.getProperties();
                    for (PropertyLayoutData propertyLayoutData : properties) {
                        final String propertyId = propertyLayoutData.getId();
                        // any will do; choose the first one that we know is valid
                        if(oneToOneAssociationById.containsKey(propertyId)) {
                            memberOrderName = propertyLayoutData.getId();
                            break;
                        }
                    }
                    memberOrderSequence = actionPropertyGroupSequence++;
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    final PropertyLayoutData propertyLayoutData = (PropertyLayoutData) actionLayoutDataOwner;
                    memberOrderName = propertyLayoutData.getId();
                    memberOrderSequence = actionPropertySequence++;
                } else if(actionLayoutDataOwner instanceof CollectionLayoutData) {
                    final CollectionLayoutData collectionLayoutData = (CollectionLayoutData) actionLayoutDataOwner;
                    memberOrderName = collectionLayoutData.getId();
                    memberOrderSequence = actionCollectionSequence++;
                } else {
                    // don't add: any existing metadata should be preserved
                    memberOrderName = null;
                    memberOrderSequence = actionDomainObjectSequence++;
                }
                if(memberOrderName != null) {
                    FacetUtil.addOrReplaceFacet(
                            new MemberOrderFacetXml(memberOrderName, "" + memberOrderSequence, translationService, objectAction));
                }

                // fix up the action position if required
                if(actionLayoutDataOwner instanceof FieldSet) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutData.setPosition(null);
                }

                FacetUtil.addOrReplaceFacet(ActionPositionFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(BookmarkPolicyFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(CssClassFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(CssClassFaFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(DescribedAsFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(HiddenFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(NamedFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(PromptStyleFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addOrReplaceFacet(RedirectFacetFromActionXml.create(actionLayoutData, objectAction));
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                final OneToOneAssociation oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) {
                    return;
                }

                FacetUtil.addOrReplaceFacet(CssClassFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(DescribedAsFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(HiddenFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(LabelAtFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(MultiLineFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(NamedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(
                        PromptStyleFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(
                        RenderedAdjustedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(
                        UnchangingFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addOrReplaceFacet(TypicalLengthFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));

                // @MemberOrder#name based on owning property group, @MemberOrder#sequence monotonically increasing
                // nb for any given field set the sequence won't reset to zero; however this is what we want so that
                // table columns are shown correctly (by fieldset, then property order within that fieldset).
                final FieldSet fieldSet = propertyLayoutData.getOwner();
                final String groupName = fieldSet.getName();
                final String sequence = "" + (propertySequence.incrementAndGet());
                FacetUtil.addOrReplaceFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToOneAssociation));
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                final OneToManyAssociation oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                FacetUtil.addOrReplaceFacet(CssClassFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(
                        DefaultViewFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(
                        DescribedAsFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(HiddenFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(NamedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(PagedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addOrReplaceFacet(SortedByFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));

                // @MemberOrder#name based on the collection's id (so that each has a single "member group")
                final String groupName = collectionLayoutData.getId();
                final String sequence = "" + collectionSequence++;
                FacetUtil.addOrReplaceFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToManyAssociation));
            }
        });
    }


    protected static List<OneToOneAssociation> getOneToOneAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.PROPERTIES);
        return associations;
    }
    protected  static List<OneToManyAssociation> getOneToManyAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        return associations;
    }



    protected static class Tuple<T> {
        public final T first;
        public final T second;
        private Tuple(final T first, final T second) {
            this.first = first;
            this.second = second;
        }
        public static <T> Tuple<T> of(final T first, final T second) {
            return new Tuple<>(first, second);
        }
    }


    /**
     * Returns a 2-element tuple of [first-second, second-first]
     */
    protected static <T> Tuple<List<T>> surplusAndMissing(final Collection<T> first, final Collection<T> second){
        final List<T> firstNotSecond = Lists.newArrayList(first);
        firstNotSecond.removeAll(second);
        final List<T> secondNotFirst = Lists.newArrayList(second);
        secondNotFirst.removeAll(first);
        return Tuple.of(firstNotSecond, secondNotFirst);
    }



    // //////////////////////////////////////

    @Programmatic
    @Override
    public void complete(final G grid, final Class<?> domainClass) {
        normalize(grid, domainClass);

        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainClass);

        grid.visit(new Grid.VisitorAdapter() {

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                final ObjectAction objectAction = objectSpec.getObjectAction(actionLayoutData.getId());

                setBookmarkingIfAny(actionLayoutData, objectAction);
                setCssClassIfAny(actionLayoutData, objectAction);
                setCssClassFaIfAny(actionLayoutData, objectAction);
                setDescribedAsIfAny(actionLayoutData, objectAction);
                setHiddenIfAny(actionLayoutData, objectAction);
                setNamedIfAny(actionLayoutData, objectAction);
                setActionPositionIfAny(actionLayoutData, objectAction);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                final ObjectAssociation collection = objectSpec.getAssociation(collectionLayoutData.getId());

                setCssClassIfAny(collectionLayoutData, collection);
                setDefaultViewIfAny(collectionLayoutData, collection);
                setDescribedAsIfAny(collectionLayoutData, collection);
                setHiddenIfAny(collectionLayoutData, collection);
                setNamedIfAny(collectionLayoutData, collection);
                setPagedIfAny(collectionLayoutData, collection);
                setSortedByIfAny(collectionLayoutData, collection);
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                final ObjectAssociation property = objectSpec.getAssociation(propertyLayoutData.getId());

                setCssClassIfAny(propertyLayoutData, property);
                setDescribedAsIfAny(propertyLayoutData, property);
                setHiddenIfAny(propertyLayoutData, property);
                setNamedIfAny(propertyLayoutData, property);
                setLabelPositionIfAny(propertyLayoutData, property);
                setMultiLineIfAny(propertyLayoutData, property);
                setRenderedAsDayBeforeIfAny(propertyLayoutData, property);
                setTypicalLengthIfAny(propertyLayoutData, property);
            }

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                setBookmarkingIfAny(domainObjectLayoutData, objectSpec);
                setCssClassIfAny(domainObjectLayoutData, objectSpec);
                setCssClassFaIfAny(domainObjectLayoutData, objectSpec);
                setDescribedAsIfAny(domainObjectLayoutData, objectSpec);
                setNamedIfAny(domainObjectLayoutData, objectSpec);
                setPluralIfAny(domainObjectLayoutData, objectSpec);
            }
        });
    }

    private static boolean isDoOp(final Facet facet) {
        return facet != null && !facet.isNoop();
    }

    protected void setBookmarkingIfAny(
            final HasBookmarking hasBookmarking,
            final FacetHolder facetHolder) {
        final BookmarkPolicyFacet bookmarkPolicyFacet = facetHolder.getFacet(BookmarkPolicyFacet.class);
        if(isDoOp(bookmarkPolicyFacet)) {
            final BookmarkPolicy bookmarking = bookmarkPolicyFacet.value();
            if(bookmarking != null) {
                hasBookmarking.setBookmarking(bookmarking);
            }
        }
    }

    protected void setCssClassIfAny(
            final HasCssClass hasCssClass,
            final FacetHolder facetHolder) {
        final CssClassFacet cssClassFacet = facetHolder.getFacet(CssClassFacet.class);
        if(isDoOp(cssClassFacet)) {
            try {
                // try...finally because CSS class may vary by object, and we pass in only null
                final String cssClass = cssClassFacet.cssClass(null);
                if(!Strings.isNullOrEmpty(cssClass)) {
                    hasCssClass.setCssClass(cssClass);
                }
            } catch(Exception ignore) {
                // ignore
            }
        }
    }

    protected void setCssClassFaIfAny(
            final HasCssClassFa hasCssClassFa,
            final FacetHolder facetHolder) {
        final CssClassFaFacet cssClassFaFacet = facetHolder.getFacet(CssClassFaFacet.class);
        if (isDoOp(cssClassFaFacet)) {
            final String cssClassFa = cssClassFaFacet.value();
            final CssClassFaPosition position = cssClassFaFacet.getPosition();
            if(!Strings.isNullOrEmpty(cssClassFa)) {
                hasCssClassFa.setCssClassFa(cssClassFa);
                hasCssClassFa.setCssClassFaPosition(position.toActionLayoutPosition());
            }
        }
    }

    protected void setDefaultViewIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {
        final DefaultViewFacet defaultViewFacet = facetHolder.getFacet(DefaultViewFacet.class);
        if(isDoOp(defaultViewFacet)) {
            final String defaultView = defaultViewFacet.value();
            if(!Strings.isNullOrEmpty(defaultView)) {
                collectionLayoutData.setDefaultView(defaultView);
            }
        }
    }

    protected void setDescribedAsIfAny(
            final HasDescribedAs hasDescribedAs,
            final FacetHolder facetHolder) {
        final DescribedAsFacet describedAsFacet = facetHolder.getFacet(DescribedAsFacet.class);
        if(isDoOp(describedAsFacet)) {
            final String describedAs = describedAsFacet.value();
            if(!Strings.isNullOrEmpty(describedAs)) {
                hasDescribedAs.setDescribedAs(describedAs);
            }
        }
    }

    protected void setHiddenIfAny(
            final HasHidden hasHidden,
            final FacetHolder facetHolder) {
        final HiddenFacet hiddenFacet = facetHolder.getFacet(HiddenFacet.class);
        if (isDoOp(hiddenFacet)) {
            final Where where = hiddenFacet.where();
            if(where != null) {
                hasHidden.setHidden(where);
            }
        }
    }

    protected void setLabelPositionIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {
        final LabelAtFacet labelAtFacet = facetHolder.getFacet(LabelAtFacet.class);
        if(isDoOp(labelAtFacet)) {
            final LabelPosition labelPosition = labelAtFacet.label();
            if(labelPosition != null) {
                propertyLayoutData.setLabelPosition(labelPosition);
            }
        }
    }

    protected void setMultiLineIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {
        final MultiLineFacet multiLineFacet = facetHolder.getFacet(MultiLineFacet.class);
        if(isDoOp(multiLineFacet)) {
            final int numberOfLines = multiLineFacet.numberOfLines();
            if(numberOfLines > 0) {
                propertyLayoutData.setMultiLine(numberOfLines);
            }
        }
    }

    protected void setNamedIfAny(
            final HasNamed hasNamed,
            final FacetHolder facetHolder) {
        final NamedFacet namedFacet = facetHolder.getFacet(NamedFacet.class);
        if(isDoOp(namedFacet)) {
            final String named = namedFacet.value();
            if(!Strings.isNullOrEmpty(named)){
                hasNamed.setNamed(named);
            }
            final boolean escaped = namedFacet.escaped();
            if(!escaped) {
                hasNamed.setNamedEscaped(escaped);
            }
        }
    }

    protected void setPagedIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {
        final PagedFacet pagedFacet = facetHolder.getFacet(PagedFacet.class);
        if(isDoOp(pagedFacet)) {
            final int value = pagedFacet.value();
            if(value > 0) {
                collectionLayoutData.setPaged(value);
            }
        }
    }

    protected void setPluralIfAny(
            final DomainObjectLayoutData domainObjectLayoutData,
            final FacetHolder facetHolder) {
        final PluralFacet pluralFacet = facetHolder.getFacet(PluralFacet.class);
        if(isDoOp(pluralFacet)) {
            final String plural = pluralFacet.value();
            if(!Strings.isNullOrEmpty(plural)) {
                domainObjectLayoutData.setPlural(plural);
            }
        }
    }


    protected void setActionPositionIfAny(
            final ActionLayoutData actionLayoutData,
            final FacetHolder facetHolder) {
        final ActionPositionFacet actionPositionFacet = facetHolder.getFacet(ActionPositionFacet.class);
        if(isDoOp(actionPositionFacet)) {
            final ActionLayout.Position position = actionPositionFacet.position();
            if(position != null) {
                actionLayoutData.setPosition(position);
            }
        }
    }

    protected void setRenderedAsDayBeforeIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {
        final RenderedAdjustedFacet renderedAdjustedFacet = facetHolder.getFacet(RenderedAdjustedFacet.class);
        if(isDoOp(renderedAdjustedFacet)) {
            final int adjusted = renderedAdjustedFacet.value();
            propertyLayoutData.setRenderedAsDayBefore(adjusted != 0);
        }
    }

    protected void setSortedByIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {
        final SortedByFacet sortedByFacet = facetHolder.getFacet(SortedByFacet.class);
        if(isDoOp(sortedByFacet)) {
            final Class<? extends Comparator<?>> className = sortedByFacet.value();
            if(className != null) {
                collectionLayoutData.setSortedBy(className.getCanonicalName());
            }
        }
    }

    protected void setTypicalLengthIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {
        final TypicalLengthFacet typicalLengthFacet = facetHolder.getFacet(TypicalLengthFacet.class);
        if(isDoOp(typicalLengthFacet)) {
            final int typicalLength = typicalLengthFacet.value();
            if(typicalLength > 0) {
                propertyLayoutData.setTypicalLength(typicalLength);
            }
        }
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


    // //////////////////////////////////////

    @javax.inject.Inject
    protected SpecificationLoader specificationLoader;

    @javax.inject.Inject
    protected TranslationService translationService;

    @javax.inject.Inject
    protected JaxbService jaxbService;

    @javax.inject.Inject
    protected DomainObjectContainer container;

    @javax.inject.Inject
    protected MessageService messageService;

    @javax.inject.Inject
    DeploymentCategoryProvider deploymentCategoryProvider;


}
