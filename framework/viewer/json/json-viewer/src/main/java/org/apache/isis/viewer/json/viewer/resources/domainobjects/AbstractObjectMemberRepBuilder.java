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
import org.apache.isis.viewer.json.viewer.representations.LinkToBuilder;
import org.apache.isis.viewer.json.viewer.representations.AbstractRepresentationBuilder;

public abstract class AbstractObjectMemberRepBuilder<R extends AbstractRepresentationBuilder<R>, T extends ObjectMember> extends AbstractRepresentationBuilder<R> {

    protected final ObjectAdapter objectAdapter;
    protected final MemberType memberType;
    protected final T objectMember;

    public AbstractObjectMemberRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, MemberType memberType, T objectMember) {
        super(resourceContext);
        this.objectAdapter = objectAdapter;
        this.memberType = memberType;
        this.objectMember = objectMember;
    }

    public R withSelf() {
        String url = AbstractObjectMemberRepBuilder.urlForMember(objectAdapter, memberType, objectMember, getOidStringifier());
        JsonRepresentation self = LinkToBuilder.newBuilder(resourceContext, "self", url).build();
        representation.mapPut("self", self);
        
        return cast(this);
    }

    protected void putMemberType() {
        representation.mapPut("memberType", memberType.getName());
    }

    protected void putId() {
        representation.mapPut(memberType.getJsProp(), objectMember.getId());
    }

    public R withMutatorsIfEnabled() {
        if(usability().isVetoed()) {
            return cast(this);
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        for(String mutator: mutators.keySet()) {
            MutatorSpec mutatorSpec = mutators.get(mutator);
            if(hasMemberFacet(mutatorSpec.mutatorFacetType)) {
                String urlForMember = urlForMember(mutatorSpec.suffix);
                JsonRepresentation arguments = mutatorArgs(mutatorSpec);
                JsonRepresentation detailsLink = 
                    LinkToBuilder.newBuilder(resourceContext, mutator, urlForMember)
                        .withHttpMethod(mutatorSpec.httpMethod)
                        .withArguments(arguments)
                        .build();
                representation.mapPut(mutator, detailsLink);
            }
        }
        return cast(this);
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

    
    protected R withValue() {
        representation.mapPut("value", valueRep());
        return cast(this);
    }

    /**
     * Members that can provide a value should override.
     */
    protected Object valueRep() {
        return null;
    }

    protected final void putDisabledReasonIfDisabled() {
        String disabledReasonRep = usability().getReason();
        representation.mapPut("disabledReason", disabledReasonRep);
    }

    public R withDetailsLink() {
        String urlForMember = urlForMember();
        JsonRepresentation detailsLink = LinkToBuilder.newBuilder(resourceContext, memberType.getDetailsRel(), urlForMember).build();
        representation.mapPut(memberType.getDetailsRel(), detailsLink);
        
        return cast(this);
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