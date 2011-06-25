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
package org.apache.isis.viewer.restful.viewer2.resources.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.progmodel.facets.CollectionUtils;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepBuilder;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;

import com.google.common.collect.Lists;

public class CollectionRepBuilder extends AbstractMemberRepBuilder<OneToManyAssociation> {

    public static CollectionRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        return new CollectionRepBuilder(repContext, objectAdapter, otma);
    }

    public CollectionRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        super(repContext, objectAdapter, MemberType.COLLECTION, otma);
    }

    public Representation build() {
        putSelfIfRequired();
        putTypeRep();
        putMemberTypeRep();
        putValueIfRequired();
        putDisabledReason();
        putMutatorsIfRequired();
        putDetailsIfRequired();
        return representation;
    }

    protected List<String> mutatorArgValues(MutatorSpec mutatorSpec) {
        List<String> values = Lists.newArrayList();
        values.add(null);
        return values;
    }

    @Override
    protected Object valueRep() {
        ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if(valueAdapter == null) {
            return null;
        }
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(valueAdapter);
        List<Representation> list = Lists.newArrayList();
        for (final ObjectAdapter elementAdapter : facet.iterable(valueAdapter)) {
            String url = DomainObjectRepBuilder.urlFor(elementAdapter, getOidStringifier());
            list.add(LinkRepBuilder.newBuilder(repContext, "value", url).build());
        }
        
        return list;
    }

}