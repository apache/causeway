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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;


public class CausewayAjaxPagingNavigation extends AjaxPagingNavigation {

    private static final long serialVersionUID = 1L;

    /** Attribute for active state */
    private final AttributeModifier activeAttribute = AttributeModifier.append("class", "active");

    public CausewayAjaxPagingNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }

    public CausewayAjaxPagingNavigation(String id, IPageable pageable) {
        super(id, pageable);
    }

    @Override
    protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
        return new CausewayAjaxPagingNavigationLink(id, pageable, pageIndex);
    }

    @Override
    protected void populateItem(final LoopItem loopItem) {
        super.populateItem(loopItem);
        if ((getStartIndex() + loopItem.getIndex()) == pageable.getCurrentPage()) {
            loopItem.add(activeAttribute);
        }
    }
}
