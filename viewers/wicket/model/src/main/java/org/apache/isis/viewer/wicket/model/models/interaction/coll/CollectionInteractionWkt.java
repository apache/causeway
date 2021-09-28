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
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.common.model.HasParentUiModel;
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

    /**
     * Returns a new <i>Collection Interaction</i> binding to the parent {@link BookmarkedObjectWkt}.
     */
    public static CollectionInteractionWkt bind(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where) {
        return new CollectionInteractionWkt(bookmarkedObject, memberId, where);
    }

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

    @Override
    public ObjectUiModelWkt getParentUiModel() {
        return ()->super.getBookmarkedOwner();
    }


    @Override
    protected CollectionInteraction load() {
        dataTableModelLazy =
                _Lazy.threadSafe(()->
                    collectionInteraction().getManagedCollection()
                    .map(DataTableModel::new));

        return CollectionInteraction.start(getBookmarkedOwner(), memberId, where);
    }

    @Override
    public void detach() {
        super.detach();
        dataTableModelLazy.clear();
    }

    // -- DATA TABLE WITH MEMOIZATION (TRANSIENT)

    private transient _Lazy<Optional<DataTableModel>> dataTableModelLazy;

    public final DataTableModel dataTableModelModel() {
        _Assert.assertTrue(this.isAttached(), "model is not attached");
        return dataTableModelLazy.get()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }


}
