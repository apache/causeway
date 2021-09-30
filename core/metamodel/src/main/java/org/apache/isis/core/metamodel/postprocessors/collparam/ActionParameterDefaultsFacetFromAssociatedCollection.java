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
package org.apache.isis.core.metamodel.postprocessors.collparam;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacetAbstract;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;

public class ActionParameterDefaultsFacetFromAssociatedCollection
extends ActionParameterDefaultsFacetAbstract {

    public static ActionParameterDefaultsFacetFromAssociatedCollection create(
            final ObjectActionParameter param) {
        return new ActionParameterDefaultsFacetFromAssociatedCollection(param);
    }

    private int paramIndex;

    private ActionParameterDefaultsFacetFromAssociatedCollection(final ObjectActionParameter param) {
        super(param);
        this.paramIndex = param.getNumber();
    }

    @Override
    public Can<ManagedObject> getDefault(@NonNull final ParameterNegotiationModel pendingArgs) {
        //FIXME[ISIS-2871] rather then filling in all (as proof of concept), fill in only those selected
        return pendingArgs.getObservableParamChoices(paramIndex).getValue();
    }


}
