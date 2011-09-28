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

import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.DomainTypeReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.TypeCollectionReprBuilder;

import com.google.common.collect.Lists;

public class ObjectCollectionReprBuilder extends AbstractObjectMemberReprBuilder<ObjectCollectionReprBuilder, OneToManyAssociation> {

    public static ObjectCollectionReprBuilder newBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        return new ObjectCollectionReprBuilder(resourceContext, objectAdapter, otma);
    }

    public ObjectCollectionReprBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, OneToManyAssociation otma) {
        super(resourceContext, objectAdapter, MemberType.OBJECT_COLLECTION, otma);

        putId();
        putMemberType();
    }

    
    public JsonRepresentation build() {
        putDisabledReasonIfDisabled();
        
        JsonRepresentation extensions = JsonRepresentation.newMap();
        putExtensionsIsisProprietary(extensions);
        withExtensions(extensions );
        
        JsonRepresentation links = JsonRepresentation.newArray();
        addLinksFormalDomainModel(links, resourceContext);
        addLinksIsisProprietary(links, resourceContext);
        withLinks(links);

        return representation;
    }


    
    /////////////////////////////////////////////////////
    // mutators
    /////////////////////////////////////////////////////

    @Override
    public ObjectCollectionReprBuilder withMutatorsIfEnabled() {
        if(usability().isVetoed()) {
            return cast(this);
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        for(String mutator: mutators.keySet()) {
            MutatorSpec mutatorSpec = mutators.get(mutator);
            if(hasMemberFacet(mutatorSpec.mutatorFacetType)) {
                
                JsonRepresentation arguments = mutatorArgs(mutatorSpec);
                JsonRepresentation detailsLink = 
                        linkToBuilder.linkToMember(mutator, memberType, objectMember, mutatorSpec.suffix)
                        .withHttpMethod(mutatorSpec.httpMethod)
                        .withArguments(arguments)
                        .build();
                representation.mapPut(mutator, detailsLink);
            }
        }
        return cast(this);
    }


    protected JsonRepresentation mutatorArgs(MutatorSpec mutatorSpec) {
        final JsonRepresentation repr = JsonRepresentation.newMap();
        if(mutatorSpec.arguments.isNone()) {
            return repr;
        }
        if(mutatorSpec.arguments.isOne()) {
            JsonRepresentation argValues = JsonRepresentation.newArray(1);
            return argValues;
        }
        throw new UnsupportedOperationException("should be overridden if bodyArgs is not 0 or 1");
    }
    

    @Override
    protected Object valueRep() {
        ObjectAdapter valueAdapter = objectMember.get(objectAdapter);
        if(valueAdapter == null) {
            return null;
        }
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(valueAdapter);
        List<JsonRepresentation> list = Lists.newArrayList();
        for (final ObjectAdapter elementAdapter : facet.iterable(valueAdapter)) {

            LinkReprBuilder newBuilder = DomainObjectReprBuilder.newLinkToBuilder(resourceContext, "object", elementAdapter);

			list.add(newBuilder.build());
        }
        
        return list;
    }

    /////////////////////////////////////////////////////
    // extensions and links
    /////////////////////////////////////////////////////
    
    private void putExtensionsIsisProprietary(JsonRepresentation extensions) {
        final CollectionSemantics semantics = CollectionSemantics.determine(resourceContext, objectMember);
        extensions.mapPut("collectionSemantics", semantics.name().toLowerCase());
    }

    private void addLinksFormalDomainModel(JsonRepresentation links, ResourceContext resourceContext) {
        links.arrayAdd(TypeCollectionReprBuilder.newLinkToBuilder(resourceContext, "typeCollection", objectAdapter.getSpecification(), objectMember).build());
    }

    private void addLinksIsisProprietary(JsonRepresentation links, ResourceContext resourceContext) {
        links.arrayAdd(DomainTypeReprBuilder.newLinkToBuilder(resourceContext, "domainType", objectAdapter.getSpecification()).build());
    }


}