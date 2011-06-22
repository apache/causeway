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

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.HttpMethod;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRep;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class PropertyRep extends MemberRep {

    private SelfRep _self;
    private LinkRep type;
    private Object value;

    
    public SelfRep get_self() {
        return _self;
    }

    public void set_self(SelfRep _self) {
        this._self = _self;
    }

    public LinkRep getType() {
        return type;
    }

    public void setType(LinkRep type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static Builder newBuilder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
        return new Builder(repContext, objectAdapter, otoa);
        
    }
    public static class Builder {

        private final RepContext repContext;
        private final ObjectAdapter objectAdapter;
        private final OneToOneAssociation otoa;

        public Builder(RepContext repContext, ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
            this.repContext = repContext;
            this.objectAdapter = objectAdapter;
            this.otoa = otoa;
        }

        public PropertyRep build() {
            PropertyRep propertyRep = new PropertyRep();
            if(addSelf()) {
                LinkRep selfLink = LinkRep.newBuilder(repContext, "link", urlFor(objectAdapter, otoa)).build();
                selfLink.setMethod(HttpMethod.GET);
                SelfRep selfRep = SelfRep.newBuilder(repContext, objectAdapter, otoa).build();
                propertyRep.set_self(selfRep);
            }
            LinkRep type = LinkRep.newTypeBuilder(repContext, "type", otoa.getSpecification()).build();
            propertyRep.setType(type);
            Consent usability = otoa.isUsable(getSession(), objectAdapter);
            propertyRep.setDisabledReason(usability.getReason());
            LinkRep detailsLink = LinkRep.newBuilder(repContext, "details", urlFor(objectAdapter, otoa)).build(); // TODO: same as self.link ???
            propertyRep.setDetails(detailsLink);
            propertyRep.setValue(obtainValue());
            return propertyRep;
        }

        /**
         * Only add the _self attribute if this representation is being
         * generated in a context that is not 'under' an attribute of an
         * owning {@link DomainObjectRep representation}.
         */
        private boolean addSelf() {
            return !repContext.hasAttribute();
        }

        private Object obtainValue() {
            ObjectAdapter valueAdapter = otoa.get(objectAdapter);
            if(valueAdapter == null) {
                return null;
            } 
            ObjectSpecification otoaSpec = otoa.getSpecification();
            ValueFacet valueFacet = otoaSpec.getFacet(ValueFacet.class);
            if(valueFacet != null) {
                EncodableFacet encodableFacet = otoaSpec.getFacet(EncodableFacet.class);
                return encodableFacet.toEncodedString(valueAdapter);
            } 
            TitleFacet titleFacet = otoaSpec.getFacet(TitleFacet.class);
            if (titleFacet == null) {
                // fallback
                titleFacet = otoaSpec.getFacet(TitleFacet.class);
            }
            String title = titleFacet.title(valueAdapter, getLocalization());
            return LinkRep.newBuilder(repContext, "value", urlFor(valueAdapter)).withTitle(title).build();
        }

        private String urlFor(ObjectAdapter adapter) {
            return DomainObjectRep.urlFor(adapter, getOidStringifier());
        }

        private String urlFor(ObjectAdapter objectAdapter, OneToOneAssociation otoa) {
            OidStringifier oidStringifier = getOidStringifier();
            String oidStr = oidStringifier.enString(objectAdapter.getOid());
            return "objects/" + oidStr + "/property/" + otoa.getId();
        }

        protected OidStringifier getOidStringifier() {
            return getOidGenerator().getOidStringifier();
        }

        protected OidGenerator getOidGenerator() {
            return getPersistenceSession().getOidGenerator();
        }

        protected PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession();
        }

        protected AuthenticationSession getSession() {
            return IsisContext.getAuthenticationSession();
        }
        
        protected Localization getLocalization() {
            return IsisContext.getLocalization();
        }

    }

}
