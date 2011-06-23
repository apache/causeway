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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.HttpMethod;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepBuilder;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;
import org.apache.isis.viewer.restful.viewer2.representations.RepresentationBuilder;

public class PropertyRepBuilder extends RepresentationBuilder {

    public static PropertyRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        return new PropertyRepBuilder(repContext, objectAdapter, otoa);
    }


    private final ObjectAdapter objectAdapter;
    private final OneToOneAssociation otoa;

    public PropertyRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        super(repContext);
        this.objectAdapter = objectAdapter;
        this.otoa = otoa;
    }

    public Representation build() {
        if(addSelf()) {
            Representation selfLink = LinkRepBuilder.newBuilder(repContext, "link", urlFor(objectAdapter, otoa)).build();
            selfLink.put("method", HttpMethod.GET);
            Representation selfRep = MemberSelfRepBuilder.newBuilder(repContext, objectAdapter, otoa).build();
            representation.put("self", selfRep);
        }
        Representation type = LinkRepBuilder.newTypeBuilder(repContext, "type", otoa.getSpecification()).build();
        representation.put("type", type);
        representation.put("value", obtainValue());
        Consent usability = otoa.isUsable(getSession(), objectAdapter);
        representation.put("disabledReason", usability.getReason());
        Representation detailsLink = LinkRepBuilder.newBuilder(repContext, "details", urlFor(objectAdapter, otoa)).build(); // TODO: same as self.link ???
        representation.put("details", detailsLink);
        return representation;
    }

    /**
     * Only add the _self attribute if this representation is being
     * generated in a context that is not 'under' an attribute of an
     * owning {@link Representation representation}.
     */
    private boolean addSelf() {
        return !repContext.hasAttribute();
    }

    private Object obtainValue() {
        ObjectAdapter valueAdapter = otoa.get(objectAdapter);
        if(valueAdapter == null) {
            return null;
        } 
        ObjectSpecification otoaSpec = otoa.getSpecification();
        ValueFacet valueFacet = otoaSpec.getFacet(ValueFacet.class);
        if(valueFacet != null) {
            EncodableFacet encodableFacet = otoaSpec.getFacet(EncodableFacet.class);
            return encodableFacet.toEncodedString(valueAdapter);
        } 
        TitleFacet titleFacet = otoaSpec.getFacet(TitleFacet.class);
        if (titleFacet == null) {
            // fallback
            titleFacet = otoaSpec.getFacet(TitleFacet.class);
        }
        String title = titleFacet.title(valueAdapter, getLocalization());
        return LinkRepBuilder.newBuilder(repContext, "value", urlFor(valueAdapter)).withTitle(title).build();
    }

    private String urlFor(ObjectAdapter adapter) {
        return DomainObjectRepBuilder.urlFor(adapter, getOidStringifier());
    }

    private String urlFor(ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        OidStringifier oidStringifier = getOidStringifier();
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        return "objects/" + oidStr + "/property/" + otoa.getId();
    }

}