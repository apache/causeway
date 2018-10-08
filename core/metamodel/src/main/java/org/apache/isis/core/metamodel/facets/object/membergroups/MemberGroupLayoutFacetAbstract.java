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

package org.apache.isis.core.metamodel.facets.object.membergroups;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class MemberGroupLayoutFacetAbstract extends FacetAbstract implements MemberGroupLayoutFacet {

    private final ColumnSpans columns;
    private final List<String> left;
    private final List<String> middle;
    private final List<String> right;

    public static Class<? extends Facet> type() {
        return MemberGroupLayoutFacet.class;
    }
    
    protected static List<String> asListWithDefaultGroup(final String[] value) {
        return value == null || value.length == 0 
                ? Arrays.asList(MemberGroupLayoutFacet.DEFAULT_GROUP) 
                : Arrays.asList(value);
    }

    protected static List<String> asList(final String[] value) {
        return Arrays.asList(value);
    }
    
    public MemberGroupLayoutFacetAbstract(
            final ColumnSpans columns,
            final List<String> left, final List<String> middle, final List<String> right, 
            FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.columns = columns != null? columns: ColumnSpans.asSpans(4,0,0,8);
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public ColumnSpans getColumnSpans() {
        return columns;
    }

    @Override
    public List<String> getLeft() {
        return left;
    }

    @Override
    public List<String> getMiddle() {
        return middle;
    }

    @Override
    public List<String> getRight() {
        return right;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("columns", "" + columns);
        attributeMap.put("left", left);
        attributeMap.put("middle", middle);
        attributeMap.put("right", right);
    }
}
