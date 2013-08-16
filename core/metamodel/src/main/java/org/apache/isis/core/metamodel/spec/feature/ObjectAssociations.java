/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.spec.feature;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributedMember;

public final class ObjectAssociations {

    private ObjectAssociations() {
    }

    public static Function<String, OneToOneAssociation> fromId(final ObjectSpecification noSpec) {
        return new Function<String, OneToOneAssociation>() {
            @Override
            public OneToOneAssociation apply(final String id) {
                return (OneToOneAssociation) noSpec.getAssociation(id);
            }
        };
    }

    public static Map<String, List<ObjectAssociation>> groupByMemberOrderName(List<ObjectAssociation> associations) {
        Map<String, List<ObjectAssociation>> associationsByGroup = Maps.newHashMap();
        for(ObjectAssociation association: associations) {
            addAssociationIntoGroup(associationsByGroup, association);
        }
        return associationsByGroup;
    }

    private static void addAssociationIntoGroup(Map<String, List<ObjectAssociation>> associationsByGroup, ObjectAssociation association) {
        final MemberOrderFacet memberOrderFacet = association.getFacet(MemberOrderFacet.class);
        if(memberOrderFacet != null) {
            final String name = memberOrderFacet.name();
            if(!Strings.isNullOrEmpty(name)) {
                getFrom(associationsByGroup, name).add(association);
                return;
            }
        }
        getFrom(associationsByGroup, "General").add(association);
    }

    private static List<ObjectAssociation> getFrom(Map<String, List<ObjectAssociation>> associationsByGroup, final String groupName) {
        List<ObjectAssociation> list = associationsByGroup.get(groupName);
        if(list == null) {
            list = Lists.newArrayList();
            associationsByGroup.put(groupName, list);
        }
        return list;
    }

    public static Function<ObjectAssociation, String> toName() {
        return new Function<ObjectAssociation, String>() {
            @Override
            public String apply(final ObjectAssociation oa) {
                return oa.getName();
            }
        };
    }

    public static Function<ObjectAssociation, String> toId() {
        return new Function<ObjectAssociation, String>() {
            @Override
            public String apply(final ObjectAssociation oa) {
                return oa.getId();
            }
        };
    }
    
    public static Predicate<ObjectAssociation> being(final Contributed contributed) {
        return new Predicate<ObjectAssociation>(){
            @Override
            public boolean apply(final ObjectAssociation t) {
                return contributed.isIncluded() || 
                       !(t instanceof ContributedMember);
            }
        };
    }


}
