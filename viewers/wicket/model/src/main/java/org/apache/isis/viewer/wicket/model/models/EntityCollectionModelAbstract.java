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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class EntityCollectionModelAbstract
extends ModelAbstract<List<ManagedObject>>
implements EntityCollectionModel {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override}) private final @NonNull Identifier identifier;
    @Getter private final int pageSize;
    @Getter private final @Nullable Class<? extends Comparator<?>> sortedBy;

    protected EntityCollectionModelAbstract(
            final @NonNull IsisAppCommonContext commonContext,
            final @NonNull ObjectMember objectMember) {
        super(commonContext);
        this.identifier = objectMember.getFeatureIdentifier();

        val typeOfSpecification = objectMember.lookupFacet(TypeOfFacet.class)
                .map(TypeOfFacet::valueSpec)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("Action or Collection MetaModel must have a TypeOfFacet"));

        this.typeOfSpecification = Optional.of(typeOfSpecification); // as an optimization: memoize transient
        this.elementType = typeOfSpecification.getCorrespondingClass();

        final Can<FacetHolder> facetHolders = Can.of(objectMember, typeOfSpecification);

        this.pageSize = facetHolders.stream()
            .map(facetHolder->facetHolder.getFacet(PagedFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(PagedFacet::value)
            .orElse(getVariant().getPageSizeDefault());
        this.sortedBy = facetHolders.stream()
            .map(facetHolder->facetHolder.getFacet(SortedByFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(SortedByFacet::value)
            .orElse(null);

        this.toggledMementos = _Maps.<String, ObjectMemento>newLinkedHashMap();
    }

    // -- SORTING

    /**
     * An element comparator corresponding to associated {@link SortedByFacet}.
     * The comparator operates on elements of type {@link ManagedObject}.
     * @return non-null
     */
    protected Comparator<ManagedObject> getElementComparator(){

        if(sortedBy == null) {
            return (a, b) -> 0; // no-op comparator, works with Stream#sort
        }

        val pojoComparator = _Casts.<Comparator<Object>>uncheckedCast(_InstanceUtil.createInstance(sortedBy));
        getCommonContext().injectServicesInto(pojoComparator);

        return (a, b) -> pojoComparator.compare(a.getPojo(), b.getPojo());
    }


    // -- TYPE OF (ELEMENT TYPE)

    @Getter(value = AccessLevel.PROTECTED) private final @NonNull Class<?> elementType;

    private transient Optional<ObjectSpecification> typeOfSpecification;

    @Override
    public final ObjectSpecification getTypeOfSpecification() {
        if(typeOfSpecification==null) {
            typeOfSpecification = getSpecificationLoader().specForType(elementType);
        }
        return typeOfSpecification.orElse(null);
    }

    // -- LINKS PROVIDER

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    public final void setLinkAndLabels(final @NonNull Iterable<LinkAndLabel> linkAndLabels) {
        this.linkAndLabels.clear();
        linkAndLabels.forEach(this.linkAndLabels::add);
    }

    @Override
    public final Can<LinkAndLabel> getLinks() {
        return Can.ofCollection(linkAndLabels);
    }

    // -- TOGGLE SUPPORT

    @Getter private LinkedHashMap<String, ObjectMemento> toggledMementos;

    @Override
    public final Can<ObjectMemento> getToggleMementosList() {
        return Can.ofCollection(this.toggledMementos.values());
    }

    @Override
    public final void clearToggleMementosList() {
        this.toggledMementos.clear();
    }

    @Override
    public final boolean toggleSelectionOn(final ManagedObject selectedAdapter) {
        final ObjectMemento selectedAsMemento = super.getMementoService().mementoForObject(selectedAdapter);
        final String selectedKey = selectedAsMemento.asString();
        final boolean isSelected = _Maps.toggleElement(toggledMementos, selectedKey, selectedAsMemento);
        return isSelected;
    }

}
