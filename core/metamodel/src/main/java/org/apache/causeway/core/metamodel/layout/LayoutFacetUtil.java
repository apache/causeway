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
package org.apache.causeway.core.metamodel.layout;

import java.util.Comparator;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.HasBookmarking;
import org.apache.causeway.applib.layout.component.HasCssClass;
import org.apache.causeway.applib.layout.component.HasCssClassFa;
import org.apache.causeway.applib.layout.component.HasDescribedAs;
import org.apache.causeway.applib.layout.component.HasHidden;
import org.apache.causeway.applib.layout.component.HasNamed;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.functions._Functions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.causeway.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.daterenderedadjust.DateRenderAdjustFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LayoutFacetUtil {

    public record LayoutDataFactory(MetamodelToGridOverridingVisitor helper) {

        public LayoutDataFactory(final ObjectSpecification objectSpec) {
            this(new MetamodelToGridOverridingVisitor(objectSpec));
        }

        public ActionLayoutData createActionLayoutData(final String id) {
            var layoutData = new ActionLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public CollectionLayoutData createCollectionLayoutData(final String id) {
            var layoutData = new CollectionLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public PropertyLayoutData createPropertyLayoutData(final String id) {
            var layoutData = new PropertyLayoutData(id);
            helper.visit(layoutData);
            return layoutData;
        }

        public DomainObjectLayoutData createDomainObjectLayoutData() {
            var layoutData = new DomainObjectLayoutData();
            helper.visit(layoutData);
            return layoutData;
        }

    }

    public record MetamodelToGridOverridingVisitor(ObjectSpecification objectSpec) implements BSElementVisitor {

        @Override
        public void visit(final ActionLayoutData actionLayoutData) {
            objectSpec.getAction(actionLayoutData.getId())
            .ifPresent(objectAction->{
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
                setPagedIfAny(collectionLayoutData, collection, objectSpec);
                setTableDecoratorIfAny(collectionLayoutData, collection, objectSpec);
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
            setPagedIfAny(domainObjectLayoutData, objectSpec);
            setTableDecoratorIfAny(domainObjectLayoutData, objectSpec);
        }
    }

    // -- HELPER

    private void setBookmarkingIfAny(
        final HasBookmarking hasBookmarking,
        final FacetHolder facetHolder) {

        var bookmarkPolicyFacet = facetHolder.getFacet(BookmarkPolicyFacet.class);
        if(isNonFallback(bookmarkPolicyFacet)) {
            final BookmarkPolicy bookmarking = bookmarkPolicyFacet.value();
            if(bookmarking != null) {
                hasBookmarking.setBookmarking(bookmarking);
            }
        }
    }

    private void setCssClassIfAny(
        final HasCssClass hasCssClass,
        final FacetHolder facetHolder) {

        var cssClassFacet = facetHolder.getFacet(CssClassFacet.class);
        if(isNonFallback(cssClassFacet)) {
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

    private void setCssClassFaIfAny(
        final HasCssClassFa hasCssClassFa,
        final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(FaFacet.class)
        .map(FaFacet::getSpecialization)
        .ifPresent(specialization->
        specialization.accept(
            faStaticFacet->{
                final String cssClassFa = faStaticFacet.getLayers().toQuickNotation();
                if(!_Strings.isNullOrEmpty(cssClassFa)) {
                    hasCssClassFa.setCssClassFa(cssClassFa);
                    hasCssClassFa.setCssClassFaPosition(faStaticFacet.getLayers().position());
                }
            },
            _Functions.noopConsumer())); // not supported for imperative fa-icons
    }

    private void setDefaultViewIfAny(
        final CollectionLayoutData collectionLayoutData,
        final FacetHolder facetHolder) {

        var defaultViewFacet = facetHolder.getFacet(DefaultViewFacet.class);
        if(isNonFallback(defaultViewFacet)) {
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
        .filter(ObjectNamedFacet::isNounPresent)
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

    private void setPagedIfAny(
        final DomainObjectLayoutData domainObjectLayoutData,
        final FacetHolder facetHolder) {

        var pagedFacet = FacetUtil.lookupFacetIn(PagedFacet.class, facetHolder).orElse(null);
        if(isNonFallback(pagedFacet)) {
            final int value = pagedFacet.value();
            if(value > 0) {
                domainObjectLayoutData.setPaged(value);
            }
        }
    }

    private void setTableDecoratorIfAny(
        final DomainObjectLayoutData domainObjectLayoutData,
        final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(TableDecoratorFacet.class)
            .map(TableDecoratorFacet::value)
            .filter(it->it!=TableDecorator.Default.class)
            .ifPresent(domainObjectLayoutData::setTableDecorator);
    }

    private void setMemberNamedIfAny(
        final HasNamed hasNamed,
        final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(MemberNamedFacet.class)
        .map(MemberNamedFacet::getSpecialization)
        .ifPresent(specialization->
        specialization.accept(
            hasStaticText->{
                var describedAs = hasStaticText.translated();
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
                var describedAs = hasStaticText.translated();
                if(_Strings.isNotEmpty(describedAs)) {
                    hasDescribedAs.setDescribedAs(describedAs);
                }
            },
            _Functions.noopConsumer())); // not supported for imperative text
    }

    private void setHiddenIfAny(
        final HasHidden hasHidden,
        final FacetHolder facetHolder) {

        var hiddenFacet = facetHolder.getFacet(HiddenFacet.class);
        if (isNonFallback(hiddenFacet)) {
            final Where where = hiddenFacet.where();
            if(where != null) {
                hasHidden.setHidden(where);
            }
        }
    }

    private void setLabelPositionIfAny(
        final PropertyLayoutData propertyLayoutData,
        final FacetHolder facetHolder) {

        var labelAtFacet = facetHolder.getFacet(LabelAtFacet.class);
        if(isNonFallback(labelAtFacet)) {
            final LabelPosition labelPosition = labelAtFacet.label();
            if(labelPosition != null) {
                propertyLayoutData.setLabelPosition(labelPosition);
            }
        }
    }

    private void setMultiLineIfAny(
        final PropertyLayoutData propertyLayoutData,
        final FacetHolder facetHolder) {

        var multiLineFacet = facetHolder.getFacet(MultiLineFacet.class);
        if(isNonFallback(multiLineFacet)) {
            final int numberOfLines = multiLineFacet.numberOfLines();
            if(numberOfLines > 0) {
                propertyLayoutData.setMultiLine(numberOfLines);
            }
        }
    }

    private void setPagedIfAny(
        final CollectionLayoutData collectionLayoutData,
        final FacetHolder facetHolder, final ObjectSpecification objectSpec) {

        var pagedFacet = FacetUtil.lookupFacetIn(PagedFacet.class, facetHolder, objectSpec).orElse(null);
        if(isNonFallback(pagedFacet)) {
            final int value = pagedFacet.value();
            if(value > 0) {
                collectionLayoutData.setPaged(value);
            }
        }
    }

    private void setTableDecoratorIfAny(
        final CollectionLayoutData collectionLayoutData,
        final FacetHolder facetHolder, final ObjectSpecification objectSpec) {

        var tableDecoratorFacet = FacetUtil.lookupFacetIn(TableDecoratorFacet.class, facetHolder, objectSpec).orElse(null);
        if(isNonFallback(tableDecoratorFacet)) {
            final Class<? extends TableDecorator> value = tableDecoratorFacet.value();
            if(value != TableDecorator.Default.class) {
                collectionLayoutData.setTableDecorator(value);
            }
        }
    }

    private void setActionPositionIfAny(
        final ActionLayoutData actionLayoutData,
        final FacetHolder facetHolder) {

        var actionPositionFacet = facetHolder.getFacet(ActionPositionFacet.class);
        if(isNonFallback(actionPositionFacet)) {
            final ActionLayout.Position position = actionPositionFacet.position();
            if(position != null) {
                actionLayoutData.setPosition(position);
            }
        }
    }

    private void setRenderedAsDayBeforeIfAny(
        final PropertyLayoutData propertyLayoutData,
        final FacetHolder facetHolder) {

        facetHolder.lookupNonFallbackFacet(DateRenderAdjustFacet.class)
        .ifPresent(dateRenderAdjustFacet->
        propertyLayoutData.setDateRenderAdjustDays(dateRenderAdjustFacet.getDateRenderAdjustDays()));
    }

    private void setSortedByIfAny(
        final CollectionLayoutData collectionLayoutData,
        final FacetHolder facetHolder) {

        var sortedByFacet = facetHolder.getFacet(SortedByFacet.class);
        if(isNonFallback(sortedByFacet)) {
            final Class<? extends Comparator<?>> cls = sortedByFacet.value();
            if(cls != null
                && cls.getCanonicalName()!=null) {
                collectionLayoutData.setSortedBy(cls.getName());
            }
        }
    }

    private void setTypicalLengthIfAny(
        final PropertyLayoutData propertyLayoutData,
        final FacetHolder facetHolder) {

        var typicalLengthFacet = facetHolder.getFacet(TypicalLengthFacet.class);
        if(isNonFallback(typicalLengthFacet)) {
            final int typicalLength = typicalLengthFacet.value();
            if(typicalLength > 0) {
                propertyLayoutData.setTypicalLength(typicalLength);
            }
        }
    }

    private static boolean isNonFallback(final Facet facet) {
        return facet != null
            && !facet.precedence().isFallback();
    }

}
