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

import java.util.List;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
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

    private final @NonNull Variant variant;

    protected EntityCollectionModelAbstract(
            final @NonNull CollectionInteractionWkt delegate,
            final @NonNull Variant variant) {
        super(delegate);
        this.variant = variant;

        val collMeta = getMetaModel();

        //TODO don't memoize
        this.identifier = collMeta.getFeatureIdentifier();

        this.pageSize = collMeta.getPageSize()
            .orElse(getVariant().getPageSizeDefault());
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

}
