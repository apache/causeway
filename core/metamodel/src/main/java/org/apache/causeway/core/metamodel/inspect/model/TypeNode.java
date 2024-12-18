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
package org.apache.causeway.core.metamodel.inspect.model;

import java.io.Serializable;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

record TypeNode(
    String logicalName)
implements MMNode, Serializable {

    @Override
    public String title() {
        return logicalName;
    }
    
    @Override
    public String iconName() {
        return "";
    }

    @Override
    public Stream<MMNode> streamChildNodes() {
        var spec = MetaModelContext.instance()
            .map(MetaModelContext::getSpecificationLoader)
            .flatMap(specLoader->specLoader.specForLogicalTypeName(logicalName))
            .orElse(null);
        if(spec==null) return Stream.empty();

        return _Streams.<MMNode>concat(
            Stream.of(
                    MMNodeFactory.facetGroup(spec.streamFacets(), this)),
            spec.streamActions(ActionScope.PRODUCTION_ONLY, MixedIn.INCLUDED)
                .map(action->MMNodeFactory.action(action, this)),
            spec.streamProperties(MixedIn.INCLUDED)
                .map(prop->MMNodeFactory.property(prop, this)),
            spec.streamCollections(MixedIn.INCLUDED)
                .map(coll->MMNodeFactory.collection(coll, this)));
    }

}
