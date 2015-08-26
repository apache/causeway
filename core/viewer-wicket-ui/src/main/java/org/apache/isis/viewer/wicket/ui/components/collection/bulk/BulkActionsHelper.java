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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

public class BulkActionsHelper implements Serializable {

    private final EntityCollectionModel model;

    private static final long serialVersionUID = 1L;

    public BulkActionsHelper(final EntityCollectionModel model) {
        this.model = model;
    }

    private EntityCollectionModel getModel() {
        return model;
    }

    public List<ObjectAction> getBulkActions() {
        final EntityCollectionModel model = getModel();

        if(model.isParented()) {
            return Collections.emptyList();
        }

        final ObjectSpecification typeSpec = model.getTypeOfSpecification();

        List<ObjectAction> objectActions = typeSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED, Filters.<ObjectAction>any());

        if ( isExploring() || isPrototyping()) {
            List<ObjectAction> explorationActions = typeSpec.getObjectActions(ActionType.EXPLORATION, Contributed.INCLUDED, Filters.<ObjectAction>any());
            List<ObjectAction> prototypeActions = typeSpec.getObjectActions(ActionType.PROTOTYPE, Contributed.INCLUDED, Filters.<ObjectAction>any());
            objectActions.addAll(explorationActions);
            objectActions.addAll(prototypeActions);
        }
        if (isDebugMode()) {
            List<ObjectAction> debugActions = typeSpec.getObjectActions(ActionType.DEBUG, Contributed.INCLUDED, Filters.<ObjectAction>any());
            objectActions.addAll(debugActions);
        }

        List<ObjectAction> flattenedActions = objectActions;

        return Lists.newArrayList(Iterables.filter(flattenedActions, BULK));
    }


    @SuppressWarnings("deprecation")
    private static final Predicate<ObjectAction> BULK = Filters.asPredicate(ObjectAction.Filters.bulk());


    //region > from context

    public boolean isExploring() {
        return IsisContext.getDeploymentType().isExploring();
    }
    public boolean isPrototyping() {
        return IsisContext.getDeploymentType().isPrototyping();
    }

    /**
     * Protected so can be overridden in testing if required.
     */
    protected boolean isDebugMode() {
        // TODO: need to figure out how to switch into debug mode;
        // probably call a Debug toggle page, and stuff into
        // Session.getMetaData()
        return true;
    }

    //endregion

}
