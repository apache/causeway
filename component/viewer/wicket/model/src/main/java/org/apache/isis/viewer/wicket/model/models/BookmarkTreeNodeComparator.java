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
        PageType pageType1 = PageParameterNames.PAGE_TYPE.getEnumFrom(o1.pageParameters, PageType.class);
        PageType pageType2 = PageParameterNames.PAGE_TYPE.getEnumFrom(o2.pageParameters, PageType.class);
        
        final int pageTypeComparison = pageType1.compareTo(pageType2);
        if(pageTypeComparison != 0) {
            return pageTypeComparison;
        }
        
        if(pageType1 == PageType.ENTITY) {
            // sort by entity type
            final String className1 = classNameOf(o1.pageParameters);
            final String className2 = classNameOf(o2.pageParameters);
            
            final int classNameComparison = className1.compareTo(className2);
            if(classNameComparison != 0) {
                return classNameComparison;
            }
        }
        String title1 = PageParameterNames.PAGE_TITLE.getStringFrom(o1.pageParameters);
        String title2 = PageParameterNames.PAGE_TITLE.getStringFrom(o2.pageParameters);
        return title1.compareTo(title2);
    }

    private String classNameOf(PageParameters pp) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pp);
        RootOid oid = getOidMarshaller().unmarshal(oidStr, RootOid.class);
        ObjectSpecId objectSpecId = oid.getObjectSpecId();
        final String className = getSpecificationLoader().lookupBySpecId(objectSpecId).getIdentifier().getClassName();
        return className;
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