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

import org.apache.wicket.Page;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;

@Configuration
@Import({
    IsisModuleViewerWicketViewer.class,
})
public class Configuration_usingWicket {

    @Bean @Singleton @Inject
    public IsisAppCommonContext commonContext(final MetaModelContext mmc) {
        return IsisAppCommonContext.of(mmc);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WicketTesterFactory {

        private final IsisAppCommonContext commonContext;

        public WicketTester createTester() {
            return new WicketTester(newWicketApplication(commonContext));
        }
    }

    @Bean @Inject
    public WicketTesterFactory wicketTesterFactory(final IsisAppCommonContext commonContext) {
        return new WicketTesterFactory(commonContext);
    }


    // -- HELPER -- APPLICATION (WICKET)

    private static WebApplication newWicketApplication(final IsisAppCommonContext commonContext) {
        val wicketApplication = new WicketApplication_forTesting(commonContext);
        ThreadContext.setApplication(wicketApplication);
        return wicketApplication;
    }

    @RequiredArgsConstructor
    static class WicketApplication_forTesting
    extends WebApplication
    implements
        ComponentFactoryRegistryAccessor,
        WicketViewerSettingsAccessor,
        HasCommonContext {
        private static final long serialVersionUID = 1L;

        @Override
        protected void init() {
            super.init();
            final IBootstrapSettings settings = new BootstrapSettings();
            settings.setDeferJavascript(false);
            Bootstrap.install(this, settings);
            setHeaderResponseDecorator(response ->
                new ResourceAggregator(new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS")));
        }

        @Getter
        private final IsisAppCommonContext commonContext;

        @Getter(lazy=true)
        private final ComponentFactoryRegistry componentFactoryRegistry =
                getCommonContext().lookupServiceElseFail(ComponentFactoryRegistry.class);

        @Getter(lazy=true)
        private final PageClassRegistry pageClassRegistry =
                getCommonContext().lookupServiceElseFail(PageClassRegistry.class);

        @Getter(lazy=true)
        private final WicketViewerSettings settings =
                getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);

        @Override
        public Class<? extends Page> getHomePage() {
            return getPageClassRegistry().getPageClass(PageType.HOME);
        }

    }


    // --

//  @Bean
//  public RequestCycleFactory requestCycleFactory() {
//      return new RequestCycleFactory();
//  }
//
//    private static class WicketApplication_forTesting
//    extends MockApplication
//    implements HasCommonContext {
//
//        @Inject MetaModelContext mmc;
//
//        private IsisAppCommonContext commonContext;
//
//        public WicketApplication_forTesting() {
//            setRootRequestMapper(new SystemMapper(this));
//        }
//
//        @Override
//        public IsisAppCommonContext getCommonContext() {
//            if(commonContext==null) {
//                commonContext = IsisAppCommonContext.of(mmc);
//            }
//            return commonContext;
//        }
//
//    }

    // -- HELPER -- REQUEST CYCLE (WICKET)

//    @NoArgsConstructor(access = AccessLevel.PRIVATE)
//    public static class RequestCycleFactory {
//
//        public void newRequestCycle(
//                final Class<? extends IRequestablePage> pageClass,
//                final PageParameters pageParameters) {
//
//            val url = Application.get().getRootRequestMapper().mapHandler(
//                    new BookmarkablePageRequestHandler(new PageProvider(pageClass, pageParameters)));
//
//            final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
//            final ServletWebRequest servletWebRequest =
//                    new ServletWebRequest(mockHttpServletRequest, "", url);//Url.parse("/wicket"));
//            final MockWebResponse mockWebResponse = new MockWebResponse();
//
//            ThreadContext.setRequestCycle(new RequestCycle(new RequestCycleContext(
//                    servletWebRequest,
//                    mockWebResponse,
//                    Mockito.mock(IRequestMapper.class),
//                    Mockito.mock(IExceptionMapper.class))));
//        }
//
//        public void clearRequestCycle() {
//            ThreadContext.setRequestCycle(null);
//        }
//
//    }

}
