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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRep;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public abstract class MemberRep {
    
    @JsonSerialize(include=Inclusion.NON_NULL)
    public static class SelfRep {
        private LinkRep link;
        private String memberType;
        private LinkRep object;
        public LinkRep getLink() {
            return link;
        }
        public void setLink(LinkRep link) {
            this.link = link;
        }
        public String getMemberType() {
            return memberType;
        }
        public void setMemberType(String memberType) {
            this.memberType = memberType;
        }
        public LinkRep getObject() {
            return object;
        }
        public void setObject(LinkRep object) {
            this.object = object;
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
            
            public SelfRep build() {
                SelfRep selfRep = new SelfRep();
                selfRep.setLink(LinkRep.newBuilder(repContext, "link", DomainObjectRep.urlFor(objectAdapter, getOidStringifier())).build());
                return selfRep;
            }

            private OidStringifier getOidStringifier() {
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

    private SelfRep _self;
    private LinkRep details;
    private String disabledReason;
    
    public SelfRep get_self() {
        return _self;
    }

    public void set_self(SelfRep _self) {
        this._self = _self;
    }

    public LinkRep getDetails() {
        return details;
    }

    public void setDetails(LinkRep details) {
        this.details = details;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }
}
