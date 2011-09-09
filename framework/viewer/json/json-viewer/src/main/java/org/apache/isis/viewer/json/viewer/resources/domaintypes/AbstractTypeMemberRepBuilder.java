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
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;

public abstract class AbstractTypeMemberRepBuilder<R extends RepresentationBuilder<R>, T extends ObjectMember> extends RepresentationBuilder<R> {

    protected final ObjectSpecification objectSpecification;
    protected final MemberType memberType;
    protected final T objectMember;

    public AbstractTypeMemberRepBuilder(ResourceContext resourceContext, ObjectSpecification objectSpecification, MemberType memberType, T objectMember) {
        super(resourceContext);
        this.objectSpecification = objectSpecification;
        this.memberType = memberType;
        this.objectMember = objectMember;
    }

    
}