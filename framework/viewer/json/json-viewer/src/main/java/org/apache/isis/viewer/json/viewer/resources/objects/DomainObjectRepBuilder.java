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
package org.apache.isis.viewer.json.viewer.resources.objects;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.RepContext;
import org.apache.isis.viewer.json.viewer.representations.LinkRepBuilder;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;
import org.apache.isis.viewer.json.viewer.util.OidUtils;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class DomainObjectRepBuilder extends RepresentationBuilder {

    public static DomainObjectRepBuilder newBuilder(RepContext representationContext, ObjectAdapter objectAdapter) {
        return new DomainObjectRepBuilder(representationContext, objectAdapter);
    }

    private final ObjectAdapter objectAdapter;
    private final Map<String, JsonRepresentation> members = Maps.newLinkedHashMap();
    
    public DomainObjectRepBuilder(RepContext repContext, ObjectAdapter objectAdapter) {
        super(repContext);
        this.objectAdapter = objectAdapter;
    }
    
    public JsonRepresentation build() {
        RepContext repContext = this.repContext.underAttribute("_self");
        JsonRepresentation selfLink = LinkRepBuilder.newBuilder(repContext, "object", url()).build();
        JsonRepresentation selfType = LinkRepBuilder.newTypeBuilder(repContext, objectAdapter.getSpecification()).build();
        String title = objectAdapter.titleString();
        JsonRepresentation iconLink = LinkRepBuilder.newBuilder(repContext, "icon", icon()).build();
        JsonRepresentation self = JsonRepresentation.newMap();
        self.put("link", selfLink);
        self.put("type", selfType);
        self.put("oid", OidUtils.getOidStr(objectAdapter, getOidStringifier()));
        self.put("title", title);
        self.put("icon", iconLink);
        representation.put("_self", self);
        withAllMembers(objectAdapter);
        if(!members.isEmpty()) {
            for(Map.Entry<String, JsonRepresentation> entry: members.entrySet()) {
                String memberId = entry.getKey();
                JsonRepresentation memberRep = entry.getValue();
                representation.put(memberId, memberRep);
            }
        }
        return representation;
    }

    private String icon() {
        String iconName = objectAdapter.getIconName();
        return "images/" + iconName + ".png";
    }

    private String url() {
        return urlFor(objectAdapter, getOidStringifier());
    }

    private void withAllMembers(final ObjectAdapter objectAdapter) {
        List<ObjectAssociation> associations = objectAdapter.getSpecification().getAssociations();
        for (ObjectAssociation assoc : associations) {
            Consent visibility = assoc.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
            String id = assoc.getId();
            if(assoc instanceof OneToOneAssociation) {
                OneToOneAssociation property = (OneToOneAssociation)assoc;
                JsonRepresentation propertyRep = PropertyRepBuilder.newBuilder(repContext.underAttribute(id), objectAdapter, property).build();
                withMember(id, propertyRep);
            }
            if(assoc instanceof OneToManyAssociation) {
                OneToManyAssociation collection = (OneToManyAssociation) assoc;
                JsonRepresentation collectionRep = CollectionRepBuilder.newBuilder(repContext.underAttribute(id), objectAdapter, collection).build();
                withMember(id, collectionRep);
            }
        }
        
        List<ObjectAction> actions = objectAdapter.getSpecification().getObjectActions(Contributed.INCLUDED);
        withActions(objectAdapter, actions);
    }

	private void withActions(final ObjectAdapter objectAdapter,
			List<ObjectAction> actions) {
		for (ObjectAction action : actions) {
            Consent visibility = action.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
        	if(action.getType().isSet()) {
        		ObjectActionSet objectActionSet = (ObjectActionSet) action;
        		List<ObjectAction> subactions = objectActionSet.getActions();
        		withActions(objectAdapter, subactions);
        	} else {
                final String id = action.getId();
                JsonRepresentation actionRep = ActionRepBuilder.newBuilder(repContext.underAttribute(id), objectAdapter, action).build();
                withMember(id, actionRep);
        	}
        }
	}

    private void withMember(String id, JsonRepresentation propertyRep) {
        members.put(id, propertyRep);
    }
    

    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////
    
	public static String urlFor(ObjectAdapter objectAdapter, OidStringifier oidStringifier) {
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        return "objects/" + oidStr;
    }

    
    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Function<ObjectAdapter, JsonRepresentation> fromAdapter(final RepContext repContext) {
        return new Function<ObjectAdapter, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(ObjectAdapter input) {
                return newBuilder(repContext, input).build();
            }
        };
    }

    public static Function<JsonRepresentation, JsonRepresentation> selfOf() {
        return new Function<JsonRepresentation, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(JsonRepresentation input) {
                return input.getRepresentation("_self");
            }
        };
    }


    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Object valueOrRef(RepContext repContext,
			final ObjectAdapter objectAdapter, ObjectSpecification objectSpec, OidStringifier oidStringifier, Localization localization) {
		ValueFacet valueFacet = objectSpec.getFacet(ValueFacet.class);
		if(valueFacet != null) {
			EncodableFacet encodeableFacet = objectSpec.getFacet(EncodableFacet.class);
			return encodeableFacet.toEncodedString(objectAdapter);
		}
		TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
		String title = titleFacet.title(objectAdapter, localization);
		return LinkRepBuilder.newObjectBuilder(repContext, objectAdapter, oidStringifier).withTitle(title).build();
	}


}