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
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;

public class MemberSelfRepBuilder extends RepresentationBuilder<MemberSelfRepBuilder> {

    public static MemberSelfRepBuilder newBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, MemberType memberType, ObjectMember objectMember) {
        return new MemberSelfRepBuilder(resourceContext, objectAdapter, memberType, objectMember);
    }

    private final ObjectAdapter objectAdapter;
    private final MemberType memberType;
    private final ObjectMember objectMember;

    public MemberSelfRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, MemberType memberType, ObjectMember objectMember) {
        super(resourceContext);
        this.objectAdapter = objectAdapter;
        this.memberType = memberType;
        this.objectMember = objectMember;
    }
    
    public JsonRepresentation build() {
        representation.mapPut("link", memberLinkRep());
        representation.mapPut("object", domainObjectLinkRep());
        return representation;
    }

    private JsonRepresentation memberLinkRep() {
        String url = AbstractObjectMemberRepBuilder.urlForMember(objectAdapter, memberType, objectMember, getOidStringifier());
        return LinkBuilder.newBuilder(resourceContext, "member", url).build();
    }
    
    private JsonRepresentation domainObjectLinkRep() {
        return DomainObjectRepBuilder.newLinkToBuilder(resourceContext, "object", objectAdapter, getOidStringifier()).build();
    }

}