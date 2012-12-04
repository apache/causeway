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

package org.apache.isis.core.progmodel.facets.object.membergroups.annotation;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupsFacet;
import org.apache.isis.core.progmodel.facets.object.membergroups.MemberGroupsFacetAbstract;

public class MemberGroupsFacetAnnotation extends MemberGroupsFacetAbstract {

    private static List<String> asList(final String[] value) {
        return value == null || value.length == 0 
                ? Arrays.asList(MemberGroupsFacet.DEFAULT_GROUP) 
                : Arrays.asList(value);
    }
    
    public MemberGroupsFacetAnnotation(final MemberGroups annotation, final FacetHolder holder) {
        super(asList(annotation.value()), holder);
    }

}
