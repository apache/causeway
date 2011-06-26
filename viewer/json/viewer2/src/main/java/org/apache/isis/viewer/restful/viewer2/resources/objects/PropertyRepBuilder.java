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

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepBuilder;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;

public class PropertyRepBuilder extends AbstractMemberRepBuilder<OneToOneAssociation> {

    public static PropertyRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        return new PropertyRepBuilder(repContext, objectAdapter, otoa);
    }

    public PropertyRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        super(repContext, objectAdapter, MemberType.PROPERTY, otoa);
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


    @Override
    protected Object valueRep() {
        ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if(valueAdapter == null) {
            return null;
        } 
        ValueFacet valueFacet = getMemberSpecFacet(ValueFacet.class);
        if(valueFacet != null) {
            EncodableFacet encodableFacet = getMemberSpecFacet(EncodableFacet.class);
            return encodableFacet.toEncodedString(valueAdapter);
        } 
        TitleFacet titleFacet = getMemberSpecFacet(TitleFacet.class);
        String title = titleFacet.title(valueAdapter, getLocalization());
        return LinkRepBuilder.newBuilder(repContext, "value", urlForObject()).withTitle(title).build();
    }

}