/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;

import com.google.common.collect.Lists;

public class CollectionRepBuilder extends AbstractMemberRepBuilder<CollectionRepBuilder, OneToManyAssociation> {

    public static CollectionRepBuilder newBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        return new CollectionRepBuilder(resourceContext, objectAdapter, otma);
    }

    public CollectionRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        super(resourceContext, objectAdapter, MemberType.COLLECTION, otma);

        MemberRepType memberRepType = MemberRepType.STANDALONE;

        putSelfIfRequired(memberRepType);
        putIdRep();
        withMemberType();
        putValueIfRequired(memberRepType);
        putDisabledReason();
        putMutatorsIfRequired(memberRepType);
        putDetailsIfRequired(memberRepType);
    }

    public JsonRepresentation build() {
        return representation;
    }

    @Override
    protected Object valueRep() {
        ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if(valueAdapter == null) {
            return null;
        }
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(valueAdapter);
        List<JsonRepresentation> list = Lists.newArrayList();
        for (final ObjectAdapter elementAdapter : facet.iterable(valueAdapter)) {

            LinkBuilder newBuilder = DomainObjectRepBuilder.newLinkToBuilder(resourceContext, elementAdapter, getOidStringifier());

			list.add(newBuilder.build());
        }
        
        return list;
    }

}