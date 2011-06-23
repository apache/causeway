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
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepBuilder;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;
import org.apache.isis.viewer.restful.viewer2.representations.RepresentationBuilder;

public class MemberSelfRepBuilder extends RepresentationBuilder {

    public static MemberSelfRepBuilder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        return new MemberSelfRepBuilder(repContext, objectAdapter, otoa);
    }

    private final ObjectAdapter objectAdapter;
    private final ObjectMember objectMember;

    public MemberSelfRepBuilder(RepContext repContext, ObjectAdapter objectAdapter, ObjectMember objectMember) {
        super(repContext);
        this.objectAdapter = objectAdapter;
        this.objectMember = objectMember;
    }
    
    public Representation build() {
        representation.put("link", LinkRepBuilder.newBuilder(repContext, "link", DomainObjectRepBuilder.urlFor(objectAdapter, getOidStringifier())).build());
        return representation;
    }
}