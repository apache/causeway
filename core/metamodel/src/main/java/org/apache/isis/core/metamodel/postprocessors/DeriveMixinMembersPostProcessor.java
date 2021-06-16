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

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.Getter;
import lombok.NonNull;

public class DeriveMixinMembersPostProcessor
implements ObjectSpecificationPostProcessor {

    @Getter(onMethod_ = {@Override})
    private final @NonNull MetaModelContext metaModelContext;

    @Inject
    public DeriveMixinMembersPostProcessor(final MetaModelContext metaModelContext) {
        this.metaModelContext = metaModelContext;
    }

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // calling count on these 3 streams so these are actually consumed,
        // as a side-effect the meta-model gets (further) populated

        // all the actions of this type
        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED).count();

        // and all the collections of this type
        objectSpecification.streamCollections(MixedIn.INCLUDED).count();

        // and all the properties of this type
        objectSpecification.streamProperties(MixedIn.INCLUDED).count();

    }


}
