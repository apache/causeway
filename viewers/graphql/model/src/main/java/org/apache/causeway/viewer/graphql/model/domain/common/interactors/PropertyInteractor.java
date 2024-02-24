/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.model.domain.common.interactors;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface PropertyInteractor
        extends MemberInteractor<OneToOneAssociation> {

    void addGqlArgument(
            OneToOneAssociation otoa,
            GraphQLFieldDefinition.Builder fieldBuilder,
            TypeMapper.InputContext inputContext);

    default MemberInteractor<OneToOneAssociation> asHolderMemberDetails() {
        return new MemberInteractor<>() {
            @Override public OneToOneAssociation getObjectMember() {return PropertyInteractor.this.getObjectMember();}
            @Override public ObjectSpecification getObjectSpecification() {return PropertyInteractor.this.getObjectSpecification();}
            @Override public SchemaType getSchemaType() {return PropertyInteractor.this.getSchemaType();}
        };
    }
}
