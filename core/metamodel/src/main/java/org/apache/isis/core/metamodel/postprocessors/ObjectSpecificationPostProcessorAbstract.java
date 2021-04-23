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

package org.apache.isis.core.metamodel.postprocessors;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Setter;
import lombok.val;

public abstract class ObjectSpecificationPostProcessorAbstract
    implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public final void postProcess(ObjectSpecification objectSpecification) {

        doPostProcess(objectSpecification);

        val actionTypes = inferActionTypes();
        objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED)
                .flatMap(ObjectAction::streamParameters)
                .forEach(this::doPostProcess);

        objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED)
                .forEach(this::doPostProcess);

        objectSpecification.streamProperties(MixedIn.INCLUDED).
                forEach(this::doPostProcess);

        objectSpecification.streamCollections(MixedIn.INCLUDED).
                forEach(this::doPostProcess);

    }

    protected abstract void doPostProcess(ObjectSpecification objectSpecification);
    protected abstract void doPostProcess(ObjectAction act);
    protected abstract void doPostProcess(ObjectActionParameter param);
    protected abstract void doPostProcess(OneToOneAssociation prop);
    protected abstract void doPostProcess(OneToManyAssociation coll);

    private ImmutableEnumSet<ActionType> inferActionTypes() {
        return metaModelContext.getSystemEnvironment().isPrototyping()
                ? ActionType.USER_AND_PROTOTYPE
                : ActionType.USER_ONLY;
    }

}
