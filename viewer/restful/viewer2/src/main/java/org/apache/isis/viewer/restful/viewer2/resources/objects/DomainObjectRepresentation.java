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

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.RepresentationContext;
import org.apache.isis.viewer.restful.viewer2.ResourceContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepresentation;
import org.apache.isis.viewer.restful.viewer2.representations.TypeRepresentation;

import com.google.common.base.Function;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class DomainObjectRepresentation {

    public static Function<ObjectAdapter, DomainObjectRepresentation> fromAdapter(final RepresentationContext representationContext) {
        return new Function<ObjectAdapter, DomainObjectRepresentation>() {
            @Override
            public DomainObjectRepresentation apply(ObjectAdapter input) {
                return newBuilder(representationContext, input).build();
            }
        };
    }


    @JsonSerialize(include=Inclusion.NON_NULL)
    public static class SelfRepresentation {
        private LinkRepresentation link;
        private TypeRepresentation type;
        private String title;
        private LinkRepresentation icon;
        
        public SelfRepresentation(LinkRepresentation link) {
            this.link = link;
        }

        public LinkRepresentation getLink() {
            return link;
        }

        public void setLink(LinkRepresentation link) {
            this.link = link;
        }

        public TypeRepresentation getType() {
            return type;
        }

        public void setType(TypeRepresentation type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LinkRepresentation getIcon() {
            return icon;
        }

        public void setIcon(LinkRepresentation icon) {
            this.icon = icon;
        }
    }
    
    private SelfRepresentation _self;
    private List<PropertyRepresentation> properties;
    private List<CollectionRepresentation> collections;
    private List<ActionRepresentation> actions;
    
    public SelfRepresentation get_self() {
        return _self;
    }
    public void set_self(SelfRepresentation self) {
        this._self = self;
    }
    public List<PropertyRepresentation> getProperties() {
        return properties;
    }
    public void setProperties(List<PropertyRepresentation> properties) {
        this.properties = properties;
    }
    public List<CollectionRepresentation> getCollections() {
        return collections;
    }
    public void setCollections(List<CollectionRepresentation> collections) {
        this.collections = collections;
    }
    public List<ActionRepresentation> getActions() {
        return actions;
    }
    public void setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
    }
    

    public static Builder newBuilder(RepresentationContext representationContext, ObjectAdapter objectAdapter) {
        return new Builder(representationContext, objectAdapter);
    }

    public static class Builder {

        private final RepresentationContext representationContext;
        private final ObjectAdapter objectAdapter;
        
        public Builder(RepresentationContext representationContext, ObjectAdapter objectAdapter) {
            this.representationContext = representationContext;
            this.objectAdapter = objectAdapter;
        }
        
        public DomainObjectRepresentation build() {
            DomainObjectRepresentation domainObject = new DomainObjectRepresentation();
            LinkRepresentation selfLink = LinkRepresentation.newBuilder(representationContext, "link", urlFor(objectAdapter)).build();
            TypeRepresentation selfType = TypeRepresentation.newBuilder(representationContext, "type", objectAdapter.getSpecification()).build();
            String title = objectAdapter.titleString();
            LinkRepresentation iconLink = LinkRepresentation.newBuilder(representationContext, "icon", iconFor(objectAdapter)).build();
            SelfRepresentation self = new SelfRepresentation(selfLink);
            self.setTitle(title);
            self.setIcon(iconLink);
            self.setType(selfType);
            domainObject.set_self(self);
            return domainObject;
        }

        private String iconFor(ObjectAdapter objectAdapter) {
            String iconName = objectAdapter.getIconName();
            return "images/" + iconName + ".png";
        }

        private String urlFor(ObjectAdapter objectAdapter) {
            String className = objectAdapter.getSpecification().getFullIdentifier();
            OidStringifier oidStringifier = getOidStringifier();
            String oidStr = oidStringifier.enString(objectAdapter.getOid());
            return className + "|" + oidStr;
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
    }
}
