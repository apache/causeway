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
package org.apache.isis.core.metamodel.layout;

import java.util.Comparator;

import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.BookmarkPolicy;
import org.apache.isis.applib.annotations.LabelPosition;
import org.apache.isis.applib.annotations.Where;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.HasBookmarking;
import org.apache.isis.applib.layout.component.HasCssClass;
import org.apache.isis.applib.layout.component.HasCssClassFa;
import org.apache.isis.applib.layout.component.HasDescribedAs;
import org.apache.isis.applib.layout.component.HasHidden;
import org.apache.isis.applib.layout.component.HasNamed;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.NounForm;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.daterenderedadjust.DateRenderAdjustFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 *
 * @since 2.0
 *
 */
@UtilityClass
public class LayoutFacetUtil {

    public void setBookmarkingIfAny(
            final HasBookmarking hasBookmarking,
            final FacetHolder facetHolder) {

        val bookmarkPolicyFacet = facetHolder.getFacet(BookmarkPolicyFacet.class);
        if(isDoOp(bookmarkPolicyFacet)) {
            final BookmarkPolicy bookmarking = bookmarkPolicyFacet.value();
            if(bookmarking != null) {
                hasBookmarking.setBookmarking(bookmarking);
            }
        }
    }

    public void setCssClassIfAny(
            final HasCssClass hasCssClass,
            final FacetHolder facetHolder) {

        val cssClassFacet = facetHolder.getFacet(CssClassFacet.class);
        if(isDoOp(cssClassFacet)) {
            try {
                // try...finally because CSS class may vary by object, and we pass in only null
                final String cssClass = cssClassFacet.cssClass(null);
                if(!_Strings.isNullOrEmpty(cssClass)) {
                    hasCssClass.setCssClass(cssClass);
                }
            } catch(Exception ignore) {
                // ignore
            }
        }
    }

    public void setCssClassFaIfAny(
            final HasCssClassFa hasCssClassFa,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(CssClassFaFacet.class)
        .map(CssClassFaFacet::getSpecialization)
        .ifPresent(specialization->
            specialization.accept(
                    hasStaticFaIcon->{
                        final String cssClassFa = hasStaticFaIcon.asSpaceSeparated();
                        if(!_Strings.isNullOrEmpty(cssClassFa)) {
                            hasCssClassFa.setCssClassFa(cssClassFa);
                            hasCssClassFa.setCssClassFaPosition(hasStaticFaIcon.getPosition());
                        }
                    },
                    _Functions.noopConsumer())); // not supported for imperative fa-icons
    }

    public void setDefaultViewIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {

        val defaultViewFacet = facetHolder.getFacet(DefaultViewFacet.class);
        if(isDoOp(defaultViewFacet)) {
            final String defaultView = defaultViewFacet.value();
            if(_Strings.isNotEmpty(defaultView)) {
                collectionLayoutData.setDefaultView(defaultView);
            }
        }
    }

    private void setObjectNamedIfAny(
            final HasNamed hasNamed,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(ObjectNamedFacet.class)
        .filter(namedFacet->namedFacet.getSupportedNounForms().contains(NounForm.SINGULAR))
        .map(ObjectNamedFacet::singularTranslated)
        .ifPresent(hasNamed::setNamed);
    }

    private void setObjectDescribedIfAny(
            final HasDescribedAs hasDescribedAs,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .map(ObjectDescribedFacet::translated)
        .filter(_Strings::isNotEmpty)
        .ifPresent(hasDescribedAs::setDescribedAs);
    }

    private void setMemberNamedIfAny(
            final HasNamed hasNamed,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(MemberNamedFacet.class)
        .map(MemberNamedFacet::getSpecialization)
        .ifPresent(specialization->
            specialization.accept(
                    hasStaticText->{
                        val describedAs = hasStaticText.translated();
                        if(_Strings.isNotEmpty(describedAs)) {
                            hasNamed.setNamed(describedAs);
                        }
                    },
                    _Functions.noopConsumer())); // not supported for imperative text
    }

    private void setMemberDescribedIfAny(
            final HasDescribedAs hasDescribedAs,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(MemberDescribedFacet.class)
        .map(MemberDescribedFacet::getSpecialization)
        .ifPresent(specialization->
            specialization.accept(
                    hasStaticText->{
                        val describedAs = hasStaticText.translated();
                        if(_Strings.isNotEmpty(describedAs)) {
                            hasDescribedAs.setDescribedAs(describedAs);
                        }
                    },
                    _Functions.noopConsumer())); // not supported for imperative text
    }

    public void setHiddenIfAny(
            final HasHidden hasHidden,
            final FacetHolder facetHolder) {

        val hiddenFacet = facetHolder.getFacet(HiddenFacet.class);
        if (isDoOp(hiddenFacet)) {
            final Where where = hiddenFacet.where();
            if(where != null) {
                hasHidden.setHidden(where);
            }
        }
    }

    public void setLabelPositionIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {

        val labelAtFacet = facetHolder.getFacet(LabelAtFacet.class);
        if(isDoOp(labelAtFacet)) {
            final LabelPosition labelPosition = labelAtFacet.label();
            if(labelPosition != null) {
                propertyLayoutData.setLabelPosition(labelPosition);
            }
        }
    }

    public void setMultiLineIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {

        val multiLineFacet = facetHolder.getFacet(MultiLineFacet.class);
        if(isDoOp(multiLineFacet)) {
            final int numberOfLines = multiLineFacet.numberOfLines();
            if(numberOfLines > 0) {
                propertyLayoutData.setMultiLine(numberOfLines);
            }
        }
    }



    public void setPagedIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {

        val pagedFacet = facetHolder.getFacet(PagedFacet.class);
        if(isDoOp(pagedFacet)) {
            final int value = pagedFacet.value();
            if(value > 0) {
                collectionLayoutData.setPaged(value);
            }
        }
    }

    public void setPluralIfAny(
            final DomainObjectLayoutData domainObjectLayoutData,
            final FacetHolder facetHolder) {

        facetHolder
        .lookupNonFallbackFacet(ObjectNamedFacet.class)
        .filter(namedFacet->namedFacet.getSupportedNounForms().contains(NounForm.PLURAL))
        .map(ObjectNamedFacet::pluralTranslated)
        .ifPresent(domainObjectLayoutData::setPlural);
    }

    public void setActionPositionIfAny(
            final ActionLayoutData actionLayoutData,
            final FacetHolder facetHolder) {

        val actionPositionFacet = facetHolder.getFacet(ActionPositionFacet.class);
        if(isDoOp(actionPositionFacet)) {
            final ActionLayout.Position position = actionPositionFacet.position();
            if(position != null) {
                actionLayoutData.setPosition(position);
            }
        }
    }

    public void setRenderedAsDayBeforeIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(DateRenderAdjustFacet.class)
        .ifPresent(dateRenderAdjustFacet->
            propertyLayoutData.setDateRenderAdjustDays(dateRenderAdjustFacet.getDateRenderAdjustDays()));
    }

    public void setSortedByIfAny(
            final CollectionLayoutData collectionLayoutData,
            final FacetHolder facetHolder) {

        val sortedByFacet = facetHolder.getFacet(SortedByFacet.class);
        if(isDoOp(sortedByFacet)) {
            final Class<? extends Comparator<?>> cls = sortedByFacet.value();
            if(cls != null
                    && cls.getCanonicalName()!=null) {
                collectionLayoutData.setSortedBy(cls.getName());
            }
        }
    }

    public void setTypicalLengthIfAny(
            final PropertyLayoutData propertyLayoutData,
            final FacetHolder facetHolder) {

        val typicalLengthFacet = facetHolder.getFacet(TypicalLengthFacet.class);
        if(isDoOp(typicalLengthFacet)) {
            final int typicalLength = typicalLengthFacet.value();
            if(typicalLength > 0) {
                propertyLayoutData.setTypicalLength(typicalLength);
            }
        }
    }

    public static class LayoutDataFactory {

        private final MetamodelToGridOverridingVisitor helper;

        public static LayoutDataFactory of(final ObjectSpecification objectSpec) {
            return new LayoutDataFactory(objectSpec);
        }

        private LayoutDataFactory(final ObjectSpecification objectSpec) {
            this.helper = MetamodelToGridOverridingVisitor.of(objectSpec);
        }

        public ActionLayoutData createActionLayoutData(final String id) {
            val layoutData = new ActionLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public CollectionLayoutData createCollectionLayoutData(final String id) {
            val layoutData = new CollectionLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public PropertyLayoutData createPropertyLayoutData(final String id) {
            val layoutData = new PropertyLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public DomainObjectLayoutData createDomainObjectLayoutData() {
            val layoutData = new DomainObjectLayoutData();
            helper.visit(layoutData);
            return layoutData;
        }

    }

    @RequiredArgsConstructor(staticName = "of")
    public static class MetamodelToGridOverridingVisitor extends Grid.VisitorAdapter  {

        private final @NonNull ObjectSpecification objectSpec;

        @Override
        public void visit(final ActionLayoutData actionLayoutData) {
            objectSpec.getAction(actionLayoutData.getId())
            .ifPresent(objectAction->{
                setBookmarkingIfAny(actionLayoutData, objectAction);
                setCssClassIfAny(actionLayoutData, objectAction);
                setCssClassFaIfAny(actionLayoutData, objectAction);
                setMemberDescribedIfAny(actionLayoutData, objectAction);
                setHiddenIfAny(actionLayoutData, objectAction);
                setMemberNamedIfAny(actionLayoutData, objectAction);
                setActionPositionIfAny(actionLayoutData, objectAction);
            });
        }

        @Override
        public void visit(final CollectionLayoutData collectionLayoutData) {
            objectSpec.getAssociation(collectionLayoutData.getId())
            .ifPresent(collection->{
                setCssClassIfAny(collectionLayoutData, collection);
                setDefaultViewIfAny(collectionLayoutData, collection);
                setMemberDescribedIfAny(collectionLayoutData, collection);
                setHiddenIfAny(collectionLayoutData, collection);
                setMemberNamedIfAny(collectionLayoutData, collection);
                setPagedIfAny(collectionLayoutData, collection);
                setSortedByIfAny(collectionLayoutData, collection);
            });
        }

        @Override
        public void visit(final PropertyLayoutData propertyLayoutData) {
            objectSpec.getAssociation(propertyLayoutData.getId())
            .ifPresent(property->{
                setCssClassIfAny(propertyLayoutData, property);
                setMemberDescribedIfAny(propertyLayoutData, property);
                setHiddenIfAny(propertyLayoutData, property);
                setMemberNamedIfAny(propertyLayoutData, property);
                setLabelPositionIfAny(propertyLayoutData, property);
                setMultiLineIfAny(propertyLayoutData, property);
                setRenderedAsDayBeforeIfAny(propertyLayoutData, property);
                setTypicalLengthIfAny(propertyLayoutData, property);
            });
        }

        @Override
        public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
            setBookmarkingIfAny(domainObjectLayoutData, objectSpec);
            setCssClassIfAny(domainObjectLayoutData, objectSpec);
            setCssClassFaIfAny(domainObjectLayoutData, objectSpec);
            setObjectDescribedIfAny(domainObjectLayoutData, objectSpec);
            setObjectNamedIfAny(domainObjectLayoutData, objectSpec);
            setPluralIfAny(domainObjectLayoutData, objectSpec);
        }
    }

    // -- HELPER

    private static boolean isDoOp(final Facet facet) {
        return facet != null
                && !facet.getPrecedence().isFallback();
    }

}
