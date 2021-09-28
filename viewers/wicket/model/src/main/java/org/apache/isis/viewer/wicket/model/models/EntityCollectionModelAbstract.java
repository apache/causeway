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

import org.apache.wicket.model.ChainingModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.interaction.coll.CollectionInteractionWkt;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Represents a collection (a member) of an entity.
 *
 * @implSpec
 * <pre>
 * EntityCollectionModel --chained-to--> CollectionInteractionWkt (delegate)
 * </pre>
 */
public abstract class EntityCollectionModelAbstract
extends ChainingModel<DataTableModel>
implements EntityCollectionModel {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override}) private final @NonNull Identifier identifier; //TODO don't memoize
    @Getter private final int pageSize; //TODO don't memoize
    @Getter private final @Nullable Class<? extends Comparator<?>> sortedBy;

    private final @NonNull Variant variant;

    protected EntityCollectionModelAbstract(
            final @NonNull CollectionInteractionWkt delegate,
            final @NonNull Variant variant) {
        super(delegate);
        this.variant = variant;

        val objectMember = getMetaModel();

        //TODO don't memoize
        this.identifier = objectMember.getFeatureIdentifier();

        val typeOfSpecification = objectMember.lookupFacet(TypeOfFacet.class)
                .map(TypeOfFacet::valueSpec)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("Action or Collection MetaModel must have a TypeOfFacet"));

        final Can<FacetHolder> facetHolders = Can.of(objectMember, typeOfSpecification);

        //TODO remove, as should be provided by the DataTableModel
        this.pageSize = facetHolders.stream()
            .map(facetHolder->facetHolder.getFacet(PagedFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(PagedFacet::value)
            .orElse(getVariant().getPageSizeDefault());
        //TODO remove, as should be provided by the DataTableModel
        this.sortedBy = facetHolders.stream()
            .map(facetHolder->facetHolder.getFacet(SortedByFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(SortedByFacet::value)
            .orElse(null);
    }

    public final CollectionInteractionWkt delegate() {
        return (CollectionInteractionWkt) super.getTarget();
    }

    public final CollectionInteraction collectionInteraction() {
        return delegate().collectionInteraction();
    }

    @Override
    public final DataTableModel getObject() {
        return delegate().dataTableModel();
    }

    @Override
    public final DataTableModel getDataTableModel() {
        return getObject();
    }

    @Override
    public OneToManyAssociation getMetaModel() {
        return delegate().getMetaModel();
    }

    @Override
    public final IsisAppCommonContext getCommonContext() {
        return delegate().getCommonContext();
    }

    // -- VARIANT SUPPORT

    @Override
    public final Variant getVariant() {
        return variant;
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

    // -- DEPRECATIONS(?)

    @Override
    public final String getName() {
        return getDataTableModel().getTitle().getValue();
    }

    @Override
    public int getCount() {
        return getDataTableModel().getDataElements().getValue().size();
    }

    // -- TOGGLE SUPPORT

    @Deprecated
    @Getter private LinkedHashMap<String, ObjectMemento> toggledMementos;

    @Deprecated
    @Override
    public final Can<ObjectMemento> getToggleMementosList() {
        return Can.ofCollection(this.toggledMementos.values());
    }

    @Deprecated
    @Override
    public final void clearToggleMementosList() {
        this.toggledMementos.clear();
    }

    @Deprecated
    @Override
    public final boolean toggleSelectionOn(final ManagedObject selectedAdapter) {
//        final ObjectMemento selectedAsMemento = super.getMementoService().mementoForObject(selectedAdapter);
//        final String selectedKey = selectedAsMemento.asString();
//        final boolean isSelected = _Maps.toggleElement(toggledMementos, selectedKey, selectedAsMemento);
//        return isSelected;
        return false;
    }

}
