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
package org.apache.isis.testdomain.conf;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.SystemMapper;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Configuration
@Import({
    IsisModuleViewerWicketViewer.class,
})
public class Configuration_usingWicket {

    @Bean @Singleton @Inject
    public IsisAppCommonContext commonContext(final MetaModelContext mmc) {
        final var wicketApplication = wicketApplication(mmc);
        ThreadContext.setApplication(wicketApplication);
        return wicketApplication.getCommonContext();
    }

    @Bean
    public RequestCycleFactory requestCycleFactory() {
        return new RequestCycleFactory();
    }

    // -- HELPER -- APPLICATION (WICKET)

    private WicketApplication_forTesting wicketApplication(final MetaModelContext mmc) {
        return mmc.getServiceInjector().injectServicesInto(
                new WicketApplication_forTesting());
    }

    private static class WicketApplication_forTesting
    extends MockApplication
    implements HasCommonContext {

        @Inject MetaModelContext mmc;

        private IsisAppCommonContext commonContext;

        public WicketApplication_forTesting() {
            setRootRequestMapper(new SystemMapper(this));
        }

        @Override
        public IsisAppCommonContext getCommonContext() {
            if(commonContext==null) {
                commonContext = IsisAppCommonContext.of(mmc);
            }
            return commonContext;
        }

    }

    // -- HELPER -- REQUEST CYCLE (WICKET)

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestCycleFactory {

        public void newRequestCycle(
                final Class<? extends IRequestablePage> pageClass,
                final PageParameters pageParameters) {

            final var url = Application.get().getRootRequestMapper().mapHandler(
                    new BookmarkablePageRequestHandler(new PageProvider(pageClass, pageParameters)));

            final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
            final ServletWebRequest servletWebRequest =
                    new ServletWebRequest(mockHttpServletRequest, "", url);//Url.parse("/wicket"));
            final MockWebResponse mockWebResponse = new MockWebResponse();

            ThreadContext.setRequestCycle(new RequestCycle(new RequestCycleContext(
                    servletWebRequest,
                    mockWebResponse,
                    Mockito.mock(IRequestMapper.class),
                    Mockito.mock(IExceptionMapper.class))));



        }

        public void clearRequestCycle() {
            ThreadContext.setRequestCycle(null);
        }

    }

    // --
}
