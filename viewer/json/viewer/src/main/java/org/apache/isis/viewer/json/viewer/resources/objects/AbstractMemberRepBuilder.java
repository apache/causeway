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
package org.apache.isis.viewer.json.viewer.resources.objects;

import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.viewer.RepContext;
import org.apache.isis.viewer.json.viewer.representations.LinkRepBuilder;
import org.apache.isis.viewer.json.viewer.representations.Representation;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;

import com.google.common.collect.Lists;

public abstract class AbstractMemberRepBuilder<T extends ObjectMember> extends RepresentationBuilder {

    protected final ObjectAdapter objectAdapter;
    protected final MemberType memberType;
    protected final T objectMember;
    protected final MemberRepType memberRepType;

    public AbstractMemberRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, MemberType memberType, T objectMember) {
        super(repContext);
        this.objectAdapter = objectAdapter;
        this.memberType = memberType;
        this.objectMember = objectMember;
        this.memberRepType = repContext.hasAttribute()?MemberRepType.INLINE:MemberRepType.STANDALONE;
    }

    protected void putSelfIfRequired() {
        if(memberRepType.hasSelf()) {
            Representation selfRep = MemberSelfRepBuilder.newBuilder(repContext, objectAdapter, memberType, objectMember).build();
            representation.put("self", selfRep);
        }
    }

    protected void putMemberTypeRep() {
        representation.put("memberType", memberType);
    }

    protected void putTypeRep() {
        Representation typeRep = LinkRepBuilder.newTypeBuilder(repContext, "type", memberType.specFor(objectMember)).build();
        representation.put("type", typeRep);
    }

    protected void putMutatorsIfRequired() {
        if(!memberRepType.hasMutators() || usability().isVetoed()) {
            return;
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        for(String mutator: mutators.keySet()) {
            MutatorSpec mutatorSpec = mutators.get(mutator);
            if(hasMemberFacet(mutatorSpec.mutatorFacetType)) {
                String urlForMember = urlForMember(mutatorSpec.suffix);
                List<String> body = mutatorArgValues(mutatorSpec);
                Representation detailsLink = 
                    LinkRepBuilder.newBuilder(repContext, mutator, urlForMember)
                        .withHttpMethod(mutatorSpec.httpMethod)
                        .withBody(body)
                        .build();
                representation.put(mutator, detailsLink);
            }
        }
    }

    /**
     * Can be optionally overridden by members that are able
     * to provide argument values (eg contributed actions).
     */
    protected List<String> mutatorArgValues(MutatorSpec mutatorSpec) {
        List<String> values = Lists.newArrayList();
        if(mutatorSpec.argSpec.isOne()) {
            values.add("{arg}");
        }
        return values;
    }


    protected void putValueIfRequired() {
        if(!memberRepType.hasValue(memberType)) {
            return;
        } 
        representation.put("value", valueRep());
        return;
    }

    /**
     * Members that can provide a value should override.
     */
    protected Object valueRep() {
        return null;
    }


    protected final void putDisabledReason() {
        String disabledReasonRep = usability().getReason();
        representation.put("disabledReason", disabledReasonRep);
    }

    protected void putDetailsIfRequired() {
        if(!memberRepType.hasLinkToDetails()) {
            return;
        } 
        String urlForMember = urlForMember();
        Representation detailsLink = LinkRepBuilder.newBuilder(repContext, "details", urlForMember).build();
        representation.put("details", detailsLink);
    }

    /**
     * For Resources to call.
     */
    public boolean isMemberVisible() {
        return visibility().isAllowed();
    }


    protected <F extends Facet> F getMemberSpecFacet(Class<F> facetType) {
        ObjectSpecification otoaSpec = objectMember.getSpecification();
        return otoaSpec.getFacet(facetType);
    }

    protected boolean hasMemberFacet(Class<? extends Facet> facetType) {
        return objectMember.getFacet(facetType) != null;
    }


    protected String urlForObject() {
        return DomainObjectRepBuilder.urlFor(objectAdapter, getOidStringifier());
    }

    protected String urlForMember(String... parts) {
        return urlForMember(objectAdapter, memberType, objectMember, getOidStringifier(), parts);
    }

    protected Consent usability() {
        return objectMember.isUsable(getSession(), objectAdapter);
    }

    protected Consent visibility() {
        return objectMember.isVisible(getSession(), objectAdapter);
    }

    
    /////////////////////////////////////////////////////////////////
    // statics
    /////////////////////////////////////////////////////////////////

    public static String urlForMember(ObjectAdapter objectAdapter, MemberType memberType, ObjectMember objectMember,
        OidStringifier oidStringifier, String... parts) {
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        StringBuilder buf = new StringBuilder();
        buf.append("objects/").append(oidStr);
        buf.append("/").append(memberType.urlPart()).append(objectMember.getId());
        for(String part: parts) {
            if(part == null) {
                continue;
            }
            buf.append("/").append(part);
        }
        return buf.toString();
    }

}