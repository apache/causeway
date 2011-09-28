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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractReprBuilder;

public abstract class AbstractObjectMemberReprBuilder<R extends AbstractReprBuilder<R>, T extends ObjectMember> extends AbstractReprBuilder<R> {

    protected ObjectAdapterLinkToBuilder linkToBuilder;
    
    protected final ObjectAdapter objectAdapter;
    protected final MemberType memberType;
    protected final T objectMember;

    public AbstractObjectMemberReprBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, MemberType memberType, T objectMember) {
        super(resourceContext);
        this.objectAdapter = objectAdapter;
        this.memberType = memberType;
        this.objectMember = objectMember;
        usingLinkToBuilder(new DomainObjectLinkToBuilder());
    }

    public R usingLinkToBuilder(ObjectAdapterLinkToBuilder linkToBuilder) {
        this.linkToBuilder = linkToBuilder.usingResourceContext(resourceContext).with(objectAdapter);
        return cast(this);
    }

    public R withSelf() {
        representation.mapPut("self", linkToBuilder.linkToMember("self", memberType, objectMember).build());
        return cast(this);
    }

    protected void putId() {
        representation.mapPut(memberType.getJsProp(), objectMember.getId());
    }

    protected void putMemberType() {
        representation.mapPut("memberType", memberType.getName());
    }


    public abstract R withMutatorsIfEnabled();

    protected abstract JsonRepresentation mutatorArgs(MutatorSpec mutatorSpec);
    
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
        representation.mapPut(memberType.getDetailsRel(), 
                linkToBuilder.linkToMember(memberType.getDetailsRel(), memberType, objectMember).build());
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

    protected Consent usability() {
        return objectMember.isUsable(getSession(), objectAdapter);
    }

    protected Consent visibility() {
        return objectMember.isVisible(getSession(), objectAdapter);
    }

    
}