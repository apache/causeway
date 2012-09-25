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

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;


public class BookmarkedPagesModel extends ModelAbstract<List<PageParameters>> implements Iterable<PageParameters>{

    private static final long serialVersionUID = 1L;
    
    private List<PageParameters> list = Lists.newArrayList();
    private transient PageParameters current;
    
    public void bookmarkPage(final BookmarkableModel<?> isisModel) {
        final PageParameters pageParameters = isisModel.asPageParameters();
        
        // ignore if doesn't provide a page type for subsequent disambiguation
        PageType pageType = PageParameterNames.PAGE_TYPE.getEnumFrom(pageParameters, PageType.class);
        if(pageType==null) {
            return;
        }
        
        // ignore if doesn't provide a title for rendering
        if(PageParameterNames.PAGE_TITLE.getStringFrom(pageParameters)==null) {
            return;
        }

        if(!list.contains(pageParameters)) {
            list.add(pageParameters);
        }
        current = pageParameters;
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

}