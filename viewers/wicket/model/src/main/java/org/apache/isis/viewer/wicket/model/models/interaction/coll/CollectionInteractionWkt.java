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
package org.apache.isis.viewer.wicket.model.models.interaction.coll;

import java.util.Optional;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.common.model.HasParentUiModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;
import org.apache.isis.viewer.wicket.model.models.interaction.ObjectUiModelWkt;

/**
 * <pre>
 * IModel[CollectionInteraction] ... List&lt;Order&gt; getOrders();
 * |
 * +-- DataTableModel ... bound to 'getOrders()'
 * </pre>
 *
 * @implSpec the state of the DataTableModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle;
 * it is recreated on load
 *
 * @see HasBookmarkedOwnerAbstract
 */
public class CollectionInteractionWkt
extends HasBookmarkedOwnerAbstract<CollectionInteraction>
implements
    HasCommonContext,
    HasParentUiModel<ObjectUiModelWkt> {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    /**
     * Returns a new <i>Collection Interaction</i> binding to the parent {@link BookmarkedObjectWkt}.
     */
    public static CollectionInteractionWkt bind(
            final BookmarkedObjectWkt bookmarkedObject,
            final OneToManyAssociation coll,
            final Where where) {
        return new CollectionInteractionWkt(bookmarkedObject, coll.getId(), where);
    }

    /**
     * Returns a new <i>Collection Interaction</i> binding to the parent {@link BookmarkedObjectWkt}
     * of given {@link ActionModel}.
     */
    public static CollectionInteractionWkt bind(
            final ActionModel actionModel,
            final Where where) {
        return new CollectionInteractionWkt(
                actionModel.getParentUiModel().bookmarkedObjectModel(),
                actionModel.getMetaModel().getId(), //FIXME[ISIS-2871] wired up incorrectly
                where);
    }

    // -- CONSTRUCTION

    private final String memberId;
    private final Where where;

    private CollectionInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where) {

        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
    }

    public final CollectionInteraction collectionInteraction() {
        return getObject();
    }

    public OneToManyAssociation getMetaModel() {
        return collectionInteraction().getManagedCollection().get().getCollection();
    }

    @Override
    public ObjectUiModelWkt getParentUiModel() {
        return ()->super.getBookmarkedOwner();
    }


    @Override
    protected CollectionInteraction load() {
        dataTableModelLazy =
                _Lazy.threadSafe(()->
                    collectionInteraction().getManagedCollection()
                    .map(DataTableModel::forCollection));

        return CollectionInteraction.start(getBookmarkedOwner(), memberId, where);
    }

    @Override
    public void detach() {
        super.detach();
        if(dataTableModelLazy!=null) {
            dataTableModelLazy.clear();
        }
    }

    // -- DATA TABLE WITH MEMOIZATION (TRANSIENT)

    private transient _Lazy<Optional<DataTableModel>> dataTableModelLazy;

    public final DataTableModel dataTableModel() {
        if(!this.isAttached()) {
            _Blackhole.consume(getObject());
        }
        return dataTableModelLazy.get()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }

}
