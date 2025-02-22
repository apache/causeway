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
package org.apache.causeway.viewer.wicket.viewer.wicketapp;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;

/**
 * Uses Wicket's default page factory, except for {@link DomainObjectPage}s which require special instantiation:
 * <p>
 * Constructor
 * {@link DomainObjectPage#DomainObjectPage(MetaModelContext, PageParameters)}
 * needs the common-context as argument.
 *
 * @since 2.0
 */
record _PageFactory(
        CausewayWicketApplication holder,
        IPageFactory delegate) implements IPageFactory {

    @Override
    public <C extends IRequestablePage> C newPage(final Class<C> pageClass, final PageParameters parameters) {
        if(DomainObjectPage.class.equals(pageClass)) {
            return _Casts.uncheckedCast(DomainObjectPage.forPageParameters(parameters));
        }
        return delegate.newPage(pageClass, parameters);
    }

    @Override
    public <C extends IRequestablePage> C newPage(final Class<C> pageClass) {
        if(DomainObjectPage.class.equals(pageClass)) {
            //TODO whenever this happens we should redirect to home,
            // almost certainly the session has timed out
            var pageTimeoutPageClass = holder.getPageClassRegistry().getPageClass(PageType.HOME_AFTER_PAGETIMEOUT);
            return _Casts.uncheckedCast(delegate.newPage(pageTimeoutPageClass));
        }
        return delegate.newPage(pageClass);
    }

    @Override
    public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass) {
        if(DomainObjectPage.class.equals(pageClass)) return true;
        return delegate.isBookmarkable(pageClass);
    }

}
