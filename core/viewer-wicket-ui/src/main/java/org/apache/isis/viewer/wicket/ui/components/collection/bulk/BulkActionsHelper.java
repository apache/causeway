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
package org.apache.isis.viewer.wicket.ui.components.collection.bulk;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collection.AssociatedWithActionsHelper;

/**
 * See also {@link AssociatedWithActionsHelper}.
 */
public class BulkActionsHelper implements Serializable {

    private final EntityCollectionModel collectionModel;

    private static final long serialVersionUID = 1L;

    public BulkActionsHelper(final EntityCollectionModel collectionModel) {
        this.collectionModel = collectionModel;
    }

    public List<ObjectAction> getBulkActions(final IsisSessionFactory isisSessionFactory) {

        if(collectionModel.isParented()) {
            return Collections.emptyList();
        }

        final ObjectSpecification objectSpec = getObjectSpecification(isisSessionFactory);

        final List<ActionType> actionTypes = inferActionTypes(isisSessionFactory);
        final Stream<ObjectAction> objectActions = objectSpec.streamObjectActions(actionTypes, Contributed.INCLUDED);

        return objectActions
                .filter(ObjectAction.Predicates.bulk())
                .collect(Collectors.toList());
    }

    private ObjectSpecification getObjectSpecification(final IsisSessionFactory isisSessionFactory) {
        return collectionModel.getTypeOfSpecification();
    }

    private List<ActionType> inferActionTypes(final IsisSessionFactory isisSessionFactory) {
        final List<ActionType> actionTypes = Lists.newArrayList();
        actionTypes.add(ActionType.USER);
        final DeploymentCategory deploymentCategory = isisSessionFactory.getDeploymentCategory();
        if ( !deploymentCategory.isProduction()) {
            actionTypes.add(ActionType.PROTOTYPE);
        }
        return actionTypes;
    }

}
