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

package org.apache.isis.viewer.wicket.model.models;

import java.util.Comparator;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

final class BookmarkTreeNodeComparator implements Comparator<BookmarkTreeNode> {
    
    @Override
    public int compare(BookmarkTreeNode o1, BookmarkTreeNode o2) {
        
        final PageType pageType1 = o1.getPageType();
        final PageType pageType2 = o2.getPageType();
        
        final int pageTypeComparison = pageType1.compareTo(pageType2);
        if(pageTypeComparison != 0) {
            return pageTypeComparison;
        }
        
        final RootOid oid1 = o1.getOidNoVer();
        final RootOid oid2 = o2.getOidNoVer();
        
        // sort by entity type
        final String className1 = classNameOf(oid1);
        final String className2 = classNameOf(oid2);
        
        final int classNameComparison = className1.compareTo(className2);
        if(classNameComparison != 0) {
            return classNameComparison;
        }
        
        final String title1 = o1.getTitle();
        final String title2 = o2.getTitle();
        
        return title1.compareTo(title2);
    }

    private String classNameOf(RootOid oid) {
        ObjectSpecId objectSpecId = oid.getObjectSpecId();
        return getSpecificationLoader().lookupBySpecId(objectSpecId).getIdentifier().getClassName();
    }

    private RootOid oidOf(PageParameters pp) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pp);
        return getOidMarshaller().unmarshal(oidStr, RootOid.class);
    }
    
    //////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////
    
    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }
    
    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}