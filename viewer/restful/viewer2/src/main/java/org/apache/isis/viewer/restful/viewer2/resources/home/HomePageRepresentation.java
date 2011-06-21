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
package org.apache.isis.viewer.restful.viewer2.resources.home;

import org.apache.isis.viewer.restful.viewer2.representations.LinkRepresentation;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class HomePageRepresentation {
    
    @JsonSerialize(include=Inclusion.NON_NULL)
    public static class SelfRepresentation {
        private LinkRepresentation link;

        public SelfRepresentation(LinkRepresentation link) {
            this.link = link;
        }

        public LinkRepresentation getLink() {
            return link;
        }

        public void setLink(LinkRepresentation link) {
            this.link = link;
        }
    }
    
    private SelfRepresentation _self;
    
    private LinkRepresentation services;
    private LinkRepresentation user;

    public SelfRepresentation get_self() {
        return _self;
    }

    public void set_self(SelfRepresentation self) {
        this._self = self;
    }
    
    public LinkRepresentation getServices() {
        return services;
    }

    public void setServices(LinkRepresentation services) {
        this.services = services;
    }

    public LinkRepresentation getUser() {
        return user;
    }

    public void setUser(LinkRepresentation user) {
        this.user = user;
    }
    

}
