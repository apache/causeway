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

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.HasFacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class ObjectSpecificationPostProcessorAbstract
implements ObjectSpecificationPostProcessor {

    @Getter(onMethod_ = {@Override})
    private final @NonNull MetaModelContext metaModelContext;

    protected ObjectSpecificationPostProcessorAbstract(final MetaModelContext metaModelContext) {
        super();
        this.metaModelContext = metaModelContext;
    }

    @Override
    public final void postProcess(final ObjectSpecification objectSpecification) {

        doPostProcess(objectSpecification);

        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
                .forEach(objectAction -> {
                    objectAction.streamParameters()
                    .forEach(param -> doPostProcess(objectSpecification, objectAction, param));
                });

        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
                .forEach(act -> doPostProcess(objectSpecification, act));

        objectSpecification.streamProperties(MixedIn.INCLUDED).
                forEach(prop -> doPostProcess(objectSpecification, prop));

        objectSpecification.streamCollections(MixedIn.INCLUDED).
                forEach(coll -> doPostProcess(objectSpecification, coll));

    }

    protected abstract void doPostProcess(ObjectSpecification objSpec);
    protected abstract void doPostProcess(ObjectSpecification objSpec, ObjectAction act);
    protected abstract void doPostProcess(ObjectSpecification objSpec, ObjectAction act, ObjectActionParameter param);
    protected abstract void doPostProcess(ObjectSpecification objSpec, OneToOneAssociation prop);
    protected abstract void doPostProcess(ObjectSpecification objSpec, OneToManyAssociation coll);

    protected static FacetedMethod facetedMethodFor(final ObjectMember objectMember) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the action/association itself.
        val objectMemberImpl = (ObjectMemberAbstract) objectMember;
        return objectMemberImpl.getFacetedMethod();
    }

    protected static FacetHolder peerFor(final ObjectActionParameter param) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the param itself.
        var paramImpl = (HasFacetHolder) param;
        return paramImpl.getFacetHolder();
    }

}
