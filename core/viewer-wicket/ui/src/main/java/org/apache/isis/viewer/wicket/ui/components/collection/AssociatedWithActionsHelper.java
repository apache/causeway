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
package org.apache.isis.viewer.wicket.ui.components.collection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.plugins.environment.IsisSystemEnvironment;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

/**
 * See also {@link BulkActionsHelper}.
 */
public class AssociatedWithActionsHelper implements Serializable {
    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel collectionModel;

    public AssociatedWithActionsHelper(final EntityCollectionModel collectionModel) {
        this.collectionModel = collectionModel;
    }

    public List<ObjectAction> getAssociatedActions(final IsisSessionFactory isisSessionFactory) {

        if(collectionModel.isStandalone()) {
            return Collections.emptyList();
        }
        final OneToManyAssociation collection = collectionModel.getCollectionMemento()
                .getCollection(isisSessionFactory.getSpecificationLoader());

        final ObjectSpecification objectSpec = getObjectSpecification(isisSessionFactory);

        final List<ActionType> actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpec.streamObjectActions(actionTypes, Contributed.INCLUDED);

        return objectActions
                .filter(ObjectAction.Predicates.associatedWithAndWithCollectionParameterFor(collection))
                .collect(Collectors.toList());
    }

    private ObjectSpecification getObjectSpecification(final IsisSessionFactory isisSessionFactory) {
        final ObjectAdapterMemento parentOam = collectionModel.getParentObjectAdapterMemento();
        final ObjectAdapter parentAdapter = parentOam.getObjectAdapter();
        return parentAdapter.getSpecification();
    }

    private static List<ActionType> inferActionTypes() {
        final List<ActionType> actionTypes = _Lists.newArrayList();
        actionTypes.add(ActionType.USER);
        if ( IsisSystemEnvironment.get().isPrototyping() ) {
            actionTypes.add(ActionType.PROTOTYPE);
        }
        return actionTypes;
    }

}
