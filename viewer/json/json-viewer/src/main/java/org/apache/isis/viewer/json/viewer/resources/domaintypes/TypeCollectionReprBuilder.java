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
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;

public class TypeCollectionReprBuilder extends AbstractTypeMemberReprBuilder<TypeCollectionReprBuilder, OneToManyAssociation> {

    public static TypeCollectionReprBuilder newBuilder(ResourceContext representationContext, ObjectSpecification objectSpecification, OneToManyAssociation collection) {
        return new TypeCollectionReprBuilder(representationContext, objectSpecification, collection);
    }

    public static LinkReprBuilder newLinkToBuilder(ResourceContext resourceContext, String rel, ObjectSpecification objectSpecification, OneToManyAssociation collection) {
        String typeFullName = objectSpecification.getFullIdentifier();
        String collectionId = collection.getId();
        String url = "domainTypes/" + typeFullName + "/collections/" + collectionId;
        return LinkReprBuilder.newBuilder(resourceContext, rel, url);
    }

    public TypeCollectionReprBuilder(ResourceContext resourceContext, ObjectSpecification objectSpecification, OneToManyAssociation collection) {
        super(resourceContext, objectSpecification, MemberType.OBJECT_COLLECTION, collection);
    }

    public JsonRepresentation build() {
        return representation;
    }


}