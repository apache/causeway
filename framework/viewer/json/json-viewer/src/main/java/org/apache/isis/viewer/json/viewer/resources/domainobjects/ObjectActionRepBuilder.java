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
import java.util.Set;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.DomainTypeRepBuilder;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.TypeActionRepBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.NullNode;
import org.jboss.resteasy.util.GetRestful;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ObjectActionRepBuilder extends AbstractObjectMemberRepBuilder<ObjectActionRepBuilder, ObjectAction> {

	public static ObjectActionRepBuilder newBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, ObjectAction objectAction) {
        return new ObjectActionRepBuilder(resourceContext, objectAdapter, objectAction);
    }

    protected ObjectActionRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter, ObjectAction objectAction) {
        super(resourceContext, objectAdapter, MemberType.OBJECT_ACTION, objectAction);
        
        putId();
        putMemberType();
    }


    
     private void putExtensionsIsisProprietary(JsonRepresentation extensions) {
        extensions.mapPut("actionType", objectMember.getType());
        withExtensions(extensions );
    }

     private void addLinksFormalDomainModel(JsonRepresentation links, ResourceContext resourceContext) {
         links.arrayAdd(TypeActionRepBuilder.newLinkToBuilder(resourceContext, "typeAction", objectAdapter.getSpecification(), objectMember).build());
     }

     private void addLinksIsisProprietary(JsonRepresentation links, ResourceContext resourceContext) {
       if(objectMember.isContributed()) {
            ObjectAdapter serviceAdapter = contributingServiceAdapter();
            JsonRepresentation contributedByLink = DomainObjectRepBuilder.newLinkToBuilder(resourceContext, "contributedBy", serviceAdapter).build();
            links.arrayAdd(contributedByLink);
        }

       links.arrayAdd(DomainTypeRepBuilder.newLinkToBuilder(resourceContext, "domainType", objectAdapter.getSpecification()).build());
       
        withLinks(links);
    }

    @Override
    public ObjectActionRepBuilder withMutatorsIfEnabled() {
        if(usability().isVetoed()) {
            return cast(this);
        }
        Map<String, MutatorSpec> mutators = memberType.getMutators();
        final ActionSemantics semantics = ActionSemantics.determine(this.resourceContext, objectMember);
        
        final String mutator = semantics.getInvokeKey();
        final MutatorSpec mutatorSpec = mutators.get(mutator);
        appendMutator(mutator, mutatorSpec);
        
        return cast(this);
    }

    protected void appendMutator(String mutator, MutatorSpec mutatorSpec) {
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


    public JsonRepresentation build() {

        putDisabledReasonIfDisabled();
        withMutatorsIfEnabled();
        
        JsonRepresentation extensions = JsonRepresentation.newMap();
        putExtensionsIsisProprietary(extensions);
        
        JsonRepresentation links = JsonRepresentation.newArray();
        addLinksFormalDomainModel(links, resourceContext);
        addLinksIsisProprietary(links, resourceContext);

        return representation;
    }
    
	private ObjectAdapter contributingServiceAdapter() {
    	ObjectSpecification serviceType = objectMember.getOnType();
    	List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
    	for (ObjectAdapter serviceAdapter : serviceAdapters) {
			if(serviceAdapter.getSpecification() == serviceType) {
				return serviceAdapter;
			}
		}
    	// fail fast
    	throw new IllegalStateException("Unable to locate contributing service");
	}

    public ObjectActionRepBuilder withParameterDetails() {
    	List<Object> parameters = Lists.newArrayList();
		for (int i=0; i< objectMember.getParameterCount(); i++) {
			ObjectActionParameter param = objectMember.getParameters().get(i);
			parameters.add(paramDetails(param));
		}
		representation.mapPut("parameters", parameters);
		return this;
	}

	private Object paramDetails(ObjectActionParameter param) {
		final JsonRepresentation paramRep = JsonRepresentation.newMap();
		paramRep.mapPut("name", param.getName());
		paramRep.mapPut("num", param.getNumber());
		paramRep.mapPut("description", param.getDescription());
		Object paramChoices = choicesFor(param);
		if(paramChoices != null) {
			paramRep.mapPut("choices", paramChoices);
		}
		Object paramDefault = defaultFor(param);
		if(paramDefault != null) {
			paramRep.mapPut("default", paramDefault);
		}
		return paramRep;
	}

	private Object choicesFor(ObjectActionParameter param) {
		ObjectAdapter[] choiceAdapters = param.getChoices(objectAdapter);
		if(choiceAdapters == null || choiceAdapters.length == 0) {
			return null;
		}
        List<Object> list = Lists.newArrayList();
        for (final ObjectAdapter choiceAdapter : choiceAdapters) {
        	ObjectSpecification objectSpec = param.getSpecification();
        	list.add(DomainObjectRepBuilder.valueOrRef(resourceContext, choiceAdapter, objectSpec));
        }
        return list;
	}

	private Object defaultFor(ObjectActionParameter param) {
		ObjectAdapter defaultAdapter = param.getDefault(objectAdapter);
		if(defaultAdapter == null) {
			return null;
		}
    	ObjectSpecification objectSpec = param.getSpecification();
    	return DomainObjectRepBuilder.valueOrRef(resourceContext, defaultAdapter, objectSpec);
	}

	@Override
	protected JsonRepresentation mutatorArgs(MutatorSpec mutatorSpec) {
	    JsonRepresentation argMap = JsonRepresentation.newMap();
	    List<ObjectActionParameter> parameters = objectMember.getParameters();
        for(int i=0; i<objectMember.getParameterCount(); i++) {
            argMap.mapPut(parameters.get(i).getName(), argValueFor(i)); 
        }
        return argMap;
    }

	private Object argValueFor(int i) {
    	if(objectMember.isContributed()) {
    		ObjectActionParameter actionParameter = objectMember.getParameters().get(i);
    		if (actionParameter.getSpecification().isOfType(objectAdapter.getSpecification())) {
    			return DomainObjectRepBuilder.newLinkToBuilder(resourceContext, "object", objectAdapter).build();
    		}
    	}
    	return NullNode.instance;
	}

}