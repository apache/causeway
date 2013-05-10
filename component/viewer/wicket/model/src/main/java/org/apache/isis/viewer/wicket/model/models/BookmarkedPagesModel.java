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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;


public class BookmarkedPagesModel extends ModelAbstract<List<PageParameters>> implements Iterable<PageParameters>{

    private static final long serialVersionUID = 1L;
    
    private List<PageParameters> list = Lists.newArrayList();
    private transient PageParameters current;
    
    public void bookmarkPage(final BookmarkableModel<?> bookmarkableModel) {
        final PageParameters candidatePP = bookmarkableModel.asPageParameters();
        
        // ignore if doesn't provide a page type for subsequent disambiguation
        PageType pageType = PageParameterNames.PAGE_TYPE.getEnumFrom(candidatePP, PageType.class);
        if(pageType==null) {
            return;
        }
        
        // ignore if doesn't provide a title for rendering
        String candidateTitle = PageParameterNames.PAGE_TITLE.getStringFrom(candidatePP);
        if(candidateTitle==null) {
            return;
        }
        
        // look to see if exists already; if found then then update title if need be.
        // the convoluted logic here is because we remove the temporarily remove the title
        // in order to do the check.
        final String pageTitleKey = PageParameterNames.PAGE_TITLE.toString();
        try {
            // temporarily remove to do comparison
            candidatePP.remove(pageTitleKey);
            
            for (PageParameters eachPP : list) {
                String pageTitle = PageParameterNames.PAGE_TITLE.getStringFrom(eachPP);
                try {
                    eachPP.remove(pageTitleKey);
                    if(eachPP.equals(candidatePP)) {
                       pageTitle = candidateTitle; // update the existing
                       current = eachPP;
                       return;
                    }
                } finally {
                    eachPP.add(PageParameterNames.PAGE_TITLE.toString(), pageTitle);
                }
            }
        } finally {
            PageParameterNames.PAGE_TITLE.addStringTo(candidatePP, candidateTitle);
        }
        
        // if get here, then didn't find.
        list.add(candidatePP);
        Collections.sort(list, new Comparator<PageParameters>() {

            @Override
            public int compare(PageParameters o1, PageParameters o2) {
                PageType pageType1 = PageParameterNames.PAGE_TYPE.getEnumFrom(o1, PageType.class);
                PageType pageType2 = PageParameterNames.PAGE_TYPE.getEnumFrom(o2, PageType.class);
                
                final int pageTypeComparison = pageType1.compareTo(pageType2);
                if(pageTypeComparison != 0) {
                    return pageTypeComparison;
                }
                
                if(pageType1 == PageType.ENTITY) {
                    // sort by entity type
                    final String className1 = classNameOf(o1);
                    final String className2 = classNameOf(o2);
                    
                    final int classNameComparison = className1.compareTo(className2);
                    if(classNameComparison != 0) {
                        return classNameComparison;
                    }
                }
                String title1 = PageParameterNames.PAGE_TITLE.getStringFrom(o1);
                String title2 = PageParameterNames.PAGE_TITLE.getStringFrom(o2);
                return title1.compareTo(title2);
            }

            private String classNameOf(PageParameters o1) {
                String oidStr1 = PageParameterNames.OBJECT_OID.getStringFrom(o1);
                RootOid oid1 = getOidMarshaller().unmarshal(oidStr1, RootOid.class);
                ObjectSpecId objectSpecId1 = oid1.getObjectSpecId();
                final String className1 = getSpecificationLoader().lookupBySpecId(objectSpecId1).getIdentifier().getClassName();
                return className1;
            }
        });
        current = candidatePP;
    }

    
    @Override
    protected List<PageParameters> load() {
        return list;
    }

    @Override
    public Iterator<PageParameters> iterator() {
        return Iterators.unmodifiableIterator(list.iterator());
    }
    
    public boolean isCurrent(PageParameters pageParameters) {
        return Objects.equal(current, pageParameters);
    }

    public static String titleFrom(final PageParameters pageParameters) {
        return PageParameterNames.PAGE_TITLE.getStringFrom(pageParameters);
    }

    public static RootOid oidFrom(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        if(oidStr == null) {
            return null;
        }
        return IsisContext.getOidMarshaller().unmarshal(oidStr, RootOid.class);
    }

    public void clear() {
        list.clear();
    }

    public boolean isEmpty() {
        return list.isEmpty();
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