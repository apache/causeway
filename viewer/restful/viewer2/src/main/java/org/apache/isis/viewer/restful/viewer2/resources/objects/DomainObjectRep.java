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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRep;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

@JsonSerialize(include=Inclusion.NON_NULL)
public class DomainObjectRep extends LinkedHashMap<String, Object>{

    private static final long serialVersionUID = 1L;

    public static Function<ObjectAdapter, DomainObjectRep> fromAdapter(final RepContext representationContext) {
        return new Function<ObjectAdapter, DomainObjectRep>() {
            @Override
            public DomainObjectRep apply(ObjectAdapter input) {
                return newBuilder(representationContext, input).build();
            }
        };
    }

    public static Function<DomainObjectRep, DomainObjectRep.SelfRep> selfOf() {
        return new Function<DomainObjectRep, SelfRep>() {
            @Override
            public SelfRep apply(DomainObjectRep input) {
                return (SelfRep) input.get("_self");
            }
        };
    }


    @JsonSerialize(include=Inclusion.NON_NULL)
    public static class SelfRep {
        private LinkRep link;
        private LinkRep type;
        private String title;
        private LinkRep icon;
        
        public SelfRep(LinkRep link) {
            this.link = link;
        }

        public LinkRep getLink() {
            return link;
        }

        public void setLink(LinkRep link) {
            this.link = link;
        }

        public LinkRep getType() {
            return type;
        }

        public void setType(LinkRep type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LinkRep getIcon() {
            return icon;
        }

        public void setIcon(LinkRep icon) {
            this.icon = icon;
        }
    }
    
    

    public static Builder newBuilder(RepContext representationContext, ObjectAdapter objectAdapter) {
        return new Builder(representationContext, objectAdapter);
    }

    public static class Builder {

        private final RepContext repContext;
        private final ObjectAdapter objectAdapter;
        private final Map<String, MemberRep> members = Maps.newLinkedHashMap();
        
        public Builder(RepContext repContext, ObjectAdapter objectAdapter) {
            this.repContext = repContext;
            this.objectAdapter = objectAdapter;
        }
        
        public DomainObjectRep build() {
            DomainObjectRep domainObject = new DomainObjectRep();
            LinkRep selfLink = LinkRep.newBuilder(repContext, "link", urlFor(objectAdapter)).build();
            LinkRep selfType = LinkRep.newTypeBuilder(repContext, "type", objectAdapter.getSpecification()).build();
            String title = objectAdapter.titleString();
            LinkRep iconLink = LinkRep.newBuilder(repContext, "icon", iconFor(objectAdapter)).build();
            SelfRep self = new SelfRep(selfLink);
            self.setTitle(title);
            self.setIcon(iconLink);
            self.setType(selfType);
            domainObject.put("_self", self);
            if(!members.isEmpty()) {
                for(Map.Entry<String, MemberRep> entry: members.entrySet()) {
                    String memberId = entry.getKey();
                    MemberRep memberRep = entry.getValue();
                    domainObject.put(memberId, memberRep);
                }
            }
            return domainObject;
        }

        private String iconFor(ObjectAdapter objectAdapter) {
            String iconName = objectAdapter.getIconName();
            return "images/" + iconName + ".png";
        }

        private String urlFor(ObjectAdapter objectAdapter) {
            OidStringifier oidStringifier = getOidStringifier();
            return DomainObjectRep.urlFor(objectAdapter, oidStringifier);
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

        public void withProperty(String id, PropertyRep propertyRep) {
            members.put(id, propertyRep);
        }

    }
    
    public static String urlFor(ObjectAdapter objectAdapter, OidStringifier oidStringifier) {
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        return "objects/" + oidStr;
    }

}
