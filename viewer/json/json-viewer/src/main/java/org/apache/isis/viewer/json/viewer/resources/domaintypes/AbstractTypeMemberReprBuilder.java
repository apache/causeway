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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;

import com.google.common.base.Strings;

public abstract class AbstractTypeMemberReprBuilder<R extends ReprRendererAbstract<R, SpecAndFeature<T>>, T extends ObjectMember> 
        extends AbstractTypeFeatureReprBuilder<R, T> {

    protected MemberType memberType;

    public AbstractTypeMemberReprBuilder(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    public ObjectSpecification getObjectSpecification() {
        return objectSpecification;
    }
    
    public T getObjectFeature() {
        return objectFeature;
    }

    /**
     * null if the feature is an object action param.
     * @return
     */
    public MemberType getMemberType() {
        return memberType;
    }
    
    @Override
    public R with(SpecAndFeature<T> specAndFeature) {
        super.with(specAndFeature);
        memberType = MemberType.determineFrom(objectFeature);
        
        // done eagerly so can use as criteria for x-ro-follow-links

        if(memberType != null) {
            ObjectMember objectMember = (ObjectMember) objectFeature;
            putId(objectMember);
            putMemberType(objectMember);
        }

        return cast(this);
    }


    protected void putId(ObjectMember objectMember) {
        representation.mapPut(memberType.getJsProp(), objectMember.getId());
    }

    protected void putMemberType(ObjectMember objectMember) {
        representation.mapPut("memberType", memberType.getName());
    }

    protected void addLinkToParentIfProvided() {
        if(parentSpec == null) {
            return;
        }
        final LinkBuilder parentLinkBuilder = 
                DomainTypeReprRenderer.newLinkToBuilder(resourceContext, Rel.UP, parentSpec);
        getLinks().arrayAdd(parentLinkBuilder.build());
    }


    protected void addLinkSelfIfRequired() {
        if(!includesSelf) {
            return;
        } 
        
        final ObjectMember objectMember = (ObjectMember)getObjectFeature();
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(
                getResourceContext(), Rel.SELF, getRepresentationType(), 
                "domainTypes/%s/%s%s", 
                getObjectSpecification().getFullIdentifier(), 
                getMemberType().getUrlPart(), 
                objectMember.getId());
        getLinks().arrayAdd(linkBuilder.build());
    }
    


}