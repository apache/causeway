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

import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkRepBuilder;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;

public abstract class AbstractMemberRepBuilder<R extends RepresentationBuilder<R>, T extends ObjectMember> extends RepresentationBuilder<R> {

    protected final ObjectAdapter objectAdapter;
    protected final MemberType memberType;
    protected final T objectMember;

    public AbstractMemberRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, MemberType memberType, T objectMember) {
        super(resourceContext);
        this.objectAdapter = objectAdapter;
        this.memberType = memberType;
        this.objectMember = objectMember;
    }

    protected void putSelfIfRequired(MemberRepType memberRepType) {
        if(!memberRepType.isStandalone()) {
            return;
        }
        
        JsonRepresentation selfRep = JsonRepresentation.newMap();
        String url = AbstractMemberRepBuilder.urlForMember(objectAdapter, memberType, objectMember, getOidStringifier());
        JsonRepresentation memberLinkRep = LinkRepBuilder.newBuilder(resourceContext, "member", url).build();
        selfRep.put("link", memberLinkRep);
        
        JsonRepresentation linkTo = DomainObjectRepBuilder.newLinkToBuilder(resourceContext, objectAdapter, getOidStringifier()).build();
        selfRep.put("object", linkTo);

        representation.put("self", selfRep);
    }

    public R withMemberType() {
        representation.put("memberType", memberType.name().toLowerCase());
        return (R) this;
    }

    protected void putIdRep() {
        representation.put(memberType.key(), objectMember.getId());
    }

    protected void putMutatorsIfRequired(MemberRepType memberRepType) {
        if(!memberRepType.isStandalone() || usability().isVetoed()) {
            return;
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        for(String mutator: mutators.keySet()) {
            MutatorSpec mutatorSpec = mutators.get(mutator);
            if(hasMemberFacet(mutatorSpec.mutatorFacetType)) {
                String urlForMember = urlForMember(mutatorSpec.suffix);
                JsonRepresentation arguments = mutatorArgs(mutatorSpec);
                JsonRepresentation detailsLink = 
                    LinkRepBuilder.newBuilder(resourceContext, mutator, urlForMember)
                        .withHttpMethod(mutatorSpec.httpMethod)
                        .withArguments(arguments)
                        .build();
                representation.put(mutator, detailsLink);
            }
        }
    }

    private JsonRepresentation mutatorArgs(MutatorSpec mutatorSpec) {
    	return appendMutatorArgs(mutatorSpec);
    }

    protected JsonRepresentation appendMutatorArgs(MutatorSpec mutatorSpec) {
		if(mutatorSpec.arguments.isNone()) {
    		return JsonRepresentation.newMap();
    	}
        if(mutatorSpec.arguments.isOne()) {
            JsonRepresentation argValues = JsonRepresentation.newArray(1);
            return argValues;
        }
        throw new UnsupportedOperationException("should be overridden if bodyArgs is not 0 or 1");
	}

    
    protected void putValueIfRequired(MemberRepType memberRepType) {
        if(!memberRepType.hasValueFor(memberType)) {
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

    protected void putDetailsIfRequired(MemberRepType memberRepType) {
        if(!memberRepType.isInline()) {
            return;
        } 
        String urlForMember = urlForMember();
        JsonRepresentation detailsLink = LinkRepBuilder.newBuilder(resourceContext, memberType.name().toLowerCase(), urlForMember).build();
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