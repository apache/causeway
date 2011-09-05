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
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;
import org.apache.isis.viewer.json.viewer.util.OidUtils;

import com.google.common.base.Function;

public class DomainObjectRepBuilder extends RepresentationBuilder<DomainObjectRepBuilder> {

    public static DomainObjectRepBuilder newBuilder(ResourceContext representationContext, ObjectAdapter objectAdapter) {
        return new DomainObjectRepBuilder(representationContext, objectAdapter);
    }

    private final ObjectAdapter objectAdapter;
    
    public DomainObjectRepBuilder(ResourceContext resourceContext, ObjectAdapter objectAdapter) {
        super(resourceContext);
        this.objectAdapter = objectAdapter;
        withSelf();
        withMembers();
    }

    public DomainObjectRepBuilder withSelf() {
        JsonRepresentation self = JsonRepresentation.newMap();
        JsonRepresentation selfLink = LinkBuilder.newBuilder(resourceContext, "object", url()).build();
        //JsonRepresentation selfType = LinkRepBuilder.newTypeBuilder(resourceContext, objectAdapter.getSpecification()).build();
        String title = objectAdapter.titleString();
        //JsonRepresentation iconLink = LinkRepBuilder.newBuilder(resourceContext, "icon", icon()).build();
        self.put("link", selfLink);
        self.put("oid", OidUtils.getOidStr(objectAdapter, getOidStringifier()));
        self.put("title", title);
//        self.put("type", selfType);
//        self.put("icon", iconLink);
        representation.put("self", self);
        return this;
    }

    private String icon() {
        String iconName = objectAdapter.getIconName();
        return "images/" + iconName + ".png";
    }

    private String url() {
        return urlFor(objectAdapter, getOidStringifier());
    }


    public DomainObjectRepBuilder withMembers() {
        JsonRepresentation members = JsonRepresentation.newArray();
        addAllMembers(objectAdapter, members);
        representation.put("members", members);
        return this;
    }

    private void addAllMembers(final ObjectAdapter objectAdapter, JsonRepresentation members) {
        List<ObjectAssociation> associations = objectAdapter.getSpecification().getAssociations();
        for (ObjectAssociation assoc : associations) {
            Consent visibility = assoc.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
            String id = assoc.getId();
            if(assoc instanceof OneToOneAssociation) {
                OneToOneAssociation property = (OneToOneAssociation)assoc;
                JsonRepresentation propertyRep = PropertyRepBuilder.newBuilder(resourceContext, objectAdapter, property).build();
                members.put(id, propertyRep);
            }
            if(assoc instanceof OneToManyAssociation) {
                OneToManyAssociation collection = (OneToManyAssociation) assoc;
                JsonRepresentation collectionRep = CollectionRepBuilder.newBuilder(resourceContext, objectAdapter, collection).build();
                members.put(id, collectionRep);
            }
        }
        
        List<ObjectAction> actions = objectAdapter.getSpecification().getObjectActions(Contributed.INCLUDED);
        addActions(objectAdapter, actions, members);
    }

	private void addActions(final ObjectAdapter objectAdapter,
			List<ObjectAction> actions, JsonRepresentation members) {
		for (ObjectAction action : actions) {
            Consent visibility = action.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
        	if(action.getType().isSet()) {
        		ObjectActionSet objectActionSet = (ObjectActionSet) action;
        		List<ObjectAction> subactions = objectActionSet.getActions();
        		addActions(objectAdapter, subactions, members);
        	} else {
                final String id = action.getId();
                JsonRepresentation actionRep = ActionRepBuilder.newBuilder(resourceContext, objectAdapter, action).build();
                members.put(id, actionRep);
        	}
        }
	}

    public JsonRepresentation build() {
        withLinks();
        withMetadata();
        return representation;
    }

    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////
    
	public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, ObjectAdapter elementAdapter, OidStringifier oidStringifier) {
    	String url = urlFor(elementAdapter, oidStringifier);
        return LinkBuilder.newBuilder(resourceContext, "object", url);
    }

    public static String urlFor(ObjectAdapter objectAdapter, OidStringifier oidStringifier) {
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        return "objects/" + oidStr;
    }

    
    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Function<ObjectAdapter, JsonRepresentation> fromAdapter(final ResourceContext resourceContext) {
        return new Function<ObjectAdapter, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(ObjectAdapter input) {
                return newBuilder(resourceContext, input).build();
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

    public static Object valueOrRef(ResourceContext resourceContext,
			final ObjectAdapter objectAdapter, ObjectSpecification objectSpec, OidStringifier oidStringifier, Localization localization) {
		ValueFacet valueFacet = objectSpec.getFacet(ValueFacet.class);
		if(valueFacet != null) {
			EncodableFacet encodeableFacet = objectSpec.getFacet(EncodableFacet.class);
			return encodeableFacet.toEncodedString(objectAdapter);
		}
		TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
		String title = titleFacet.title(objectAdapter, localization);
		return DomainObjectRepBuilder.newLinkToBuilder(resourceContext, objectAdapter, oidStringifier).withTitle(title).build();
	}


}