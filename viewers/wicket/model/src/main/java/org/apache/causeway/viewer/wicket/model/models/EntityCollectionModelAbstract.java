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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.List;

import org.apache.wicket.model.ChainingModel;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.interactive.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataTableModelWkt;

import lombok.NonNull;

/**
 * Represents a collection (a member) of an entity.
 *
 * @implSpec
 * <pre>
 * EntityCollectionModel --chained-to--> CollectionInteractionWkt (delegate)
 * </pre>
 */
public abstract class EntityCollectionModelAbstract
extends ChainingModel<DataTableInteractive>
implements EntityCollectionModel {

    private static final long serialVersionUID = 1L;

    private final @NonNull Variant variant;

    protected EntityCollectionModelAbstract(
            final DataTableModelWkt dataTableModelWkt,
            final @NonNull Variant variant) {
        super(dataTableModelWkt);
        this.variant = variant;
    }

    public final DataTableModelWkt delegate() {
        return (DataTableModelWkt) super.getTarget();
    }

    @Override
    public final DataTableInteractive getObject() {
        return delegate().getObject();
    }

    @Override
    public final DataTableInteractive getDataTableModel() {
        return getObject();
    }

    @Override
    public ObjectMember getMetaModel() {
        return getDataTableModel()
                .getMetaModel();
    }

    @Override
    public Identifier getIdentifier() {
        return getMetaModel().getFeatureIdentifier();
    }

    @Override
    public final ManagedObject getParentObject() {
        return delegate().getBookmarkedOwner();
    }

    @Override
    public final void setSearchArgument(final String searchArg) {
        delegate().setSearchArgument(searchArg);
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

}
