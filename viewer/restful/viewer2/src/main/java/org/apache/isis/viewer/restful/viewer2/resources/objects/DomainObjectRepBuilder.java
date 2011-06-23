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

import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRepBuilder;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;
import org.apache.isis.viewer.restful.viewer2.representations.RepresentationBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class DomainObjectRepBuilder extends RepresentationBuilder{

    public static DomainObjectRepBuilder newBuilder(RepContext representationContext, ObjectAdapter objectAdapter) {
        return new DomainObjectRepBuilder(representationContext, objectAdapter);
    }

    private final ObjectAdapter objectAdapter;
    private final Map<String, Representation> members = Maps.newLinkedHashMap();
    
    public DomainObjectRepBuilder(RepContext repContext, ObjectAdapter objectAdapter) {
        super(repContext);
        this.objectAdapter = objectAdapter;
    }
    
    public Representation build() {
        RepContext repContext = this.repContext.underAttribute("_self");
        Representation selfLink = LinkRepBuilder.newBuilder(repContext, "link", urlFor(objectAdapter)).build();
        Representation selfType = LinkRepBuilder.newTypeBuilder(repContext, "type", objectAdapter.getSpecification()).build();
        String title = objectAdapter.titleString();
        Representation iconLink = LinkRepBuilder.newBuilder(repContext, "icon", iconFor(objectAdapter)).build();
        Representation self = new Representation();
        self.put("link", selfLink);
        self.put("type", selfType);
        self.put("title", title);
        self.put("icon", iconLink);
        representation.put("_self", self);
        if(!members.isEmpty()) {
            for(Map.Entry<String, Representation> entry: members.entrySet()) {
                String memberId = entry.getKey();
                Representation memberRep = entry.getValue();
                representation.put(memberId, memberRep);
            }
        }
        return representation;
    }

    public void withProperty(String id, Representation propertyRep) {
        members.put(id, propertyRep);
    }

    private String iconFor(ObjectAdapter objectAdapter) {
        String iconName = objectAdapter.getIconName();
        return "images/" + iconName + ".png";
    }

    private String urlFor(ObjectAdapter objectAdapter) {
        OidStringifier oidStringifier = getOidStringifier();
        return urlFor(objectAdapter, oidStringifier);
    }

    public static String urlFor(ObjectAdapter objectAdapter, OidStringifier oidStringifier) {
        String oidStr = oidStringifier.enString(objectAdapter.getOid());
        return "objects/" + oidStr;
    }

    public static Function<ObjectAdapter, Representation> fromAdapter(final RepContext repContext) {
        return new Function<ObjectAdapter, Representation>() {
            @Override
            public Representation apply(ObjectAdapter input) {
                return newBuilder(repContext, input).build();
            }
        };
    }

    public static Function<Representation, Representation> selfOf() {
        return new Function<Representation, Representation>() {
            @Override
            public Representation apply(Representation input) {
                return (Representation) input.get("_self");
            }
        };
    }

}