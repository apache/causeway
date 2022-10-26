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
package org.apache.causeway.testdomain.conf;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.CausewayWicketAjaxRequestListenerUtil;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;

@Configuration
@Import({
    CausewayModuleViewerWicketViewer.class,
    Configuration_usingWicket.WicketViewerOutputMarkupContainerClassNameEnable.class
})
public class Configuration_usingWicket {

    @Configuration
    public class WicketViewerOutputMarkupContainerClassNameEnable
    implements WicketApplicationInitializer {

        @Override
        public void init(final WebApplication webApplication) {
            webApplication.getDebugSettings()
                .setComponentPathAttributeName("wicket-tester-path")
                .setOutputMarkupContainerClassName(true);
        }
    }

    public static class EntityPageTester
    extends WicketTester
    implements HasMetaModelContext {

        // -- HOMEPAGE (TEST APP)

        public static final String OPEN_SAMPLE_ACTION = "theme:entityPageContainer:entity:rows:1"
                + ":rowContents:1"
                + ":col:entityHeaderPanel:entityActions:additionalLinkList:additionalLinkItem:1"
                + ":additionalLink";

        public static final String OPEN_SAMPLE_ACTION_TITLE = OPEN_SAMPLE_ACTION
                + ":additionalLinkTitle";


        // -- INVENTORY JAXB VM

        public static final String FAVORITE_BOOK_SCALAR = "theme:entityPageContainer:entity:rows:2"
                + ":rowContents:1"
                + ":col:fieldSets:1"
                + ":memberGroup:properties:2"
                + ":property:scalarTypeContainer:scalarIfRegular";

        public static final String FAVORITE_BOOK_SCALAR_NAME = FAVORITE_BOOK_SCALAR
                + ":scalarName";

        public static final String FAVORITE_BOOK_ENTITY_LINK = FAVORITE_BOOK_SCALAR
                + ":container-fieldFrame:scalarValueInlinePromptLink:container-scalarValue:entityLink"
                + ":entityIconAndTitle:entityLinkWrapper:entityLink";

        public static final String FAVORITE_BOOK_ENTITY_LINK_TITLE = FAVORITE_BOOK_ENTITY_LINK
                + ":entityTitle";

        @RequiredArgsConstructor
        public static enum SimulatedProperties implements SimulatedProperty {
            INVENTORY_NAME("theme:entityPageContainer:entity:rows:2"
                    + ":rowContents:1"
                    + ":col:fieldSets:1"
                    + ":memberGroup:properties:1"
                    + ":property"),
            JDO_BOOK_ISBN("theme:entityPageContainer:entity:rows:2"
                    + ":rowContents:1:col:rows:1:rowContents:1"
                    + ":col:tabGroups:1:panel:tabPanel:rows:1:rowContents:1:col"
                    + ":fieldSets:1"
                    + ":memberGroup:properties:3"
                    + ":property"),
            JPA_BOOK_ISBN("theme:entityPageContainer:entity:rows:2"
                    + ":rowContents:1:col:rows:1:rowContents:1"
                    + ":col:tabGroups:1:1"
                    + ":rowContents:1:col"
                    + ":fieldSets:1"
                    + ":memberGroup:properties:4"
                    + ":property"
                    );
            @Getter @Accessors(fluent=true) final String id;
        }

        public static interface SimulatedProperty {
            String id();
            default String editLink() {
                return id() + ":scalarTypeContainer:scalarIfRegular:container-fieldFrame"
                        + ":scalarValueInlinePromptLink";
            }
            default String editInlineForm() {
                return id() + ":scalarTypeContainer:scalarIfRegularInlinePromptForm:inputForm";
            }
            default String scalarField() {
                return "property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:"
                    + RegularFrame.FIELD.getContainerId() + ":"
                    + FieldFrame.SCALAR_VALUE_CONTAINER.getContainerId() + ":scalarValue";
            }
            default String editInlineFormTextField() {
                return id() + ":" + scalarField();
            }
            default String editInlinePromptForm() {
                return editInlineForm();
                //return id() + ":scalarTypeContainer:scalarIfRegularInlinePromptForm:inputForm";
            }
            default String editInlinePromptFormOk() {
                return editInlinePromptForm() + ":okButton";
            }
        }

        // --

        @Getter
        private final MetaModelContext metaModelContext;
        private final Function<BookDto, IBook> bookFactory;

        public EntityPageTester(
                final MetaModelContext metaModelContext,
                final Function<BookDto, IBook> bookFactory) {
            super(newWicketApplication(metaModelContext));
            this.metaModelContext = metaModelContext;
            metaModelContext.injectServicesInto(this);
            this.bookFactory = bookFactory;

        }

        public PageParameters createPageParameters(final Object entityOrVm) {
            final ManagedObject domainObject = getObjectManager().adapt(entityOrVm);
            return PageParameterUtils.createPageParametersForObject(domainObject);
        }

        public void assertPageTitle(final String expectedLabel) {
            assertLabel("pageTitle", expectedLabel);
        }

        public void assertHeaderBrandText(final String expectedLabel) {
            assertLabel("header:applicationName:brandText", expectedLabel);
        }

        @SneakyThrows
        public void assertPropertyValue(final String path, final ThrowingConsumer<ManagedObject> checker) {
            Component component = getComponentFromLastRenderedPage(path);
            ManagedObject adapter = (ManagedObject)component.getDefaultModelObject();
            checker.accept(adapter);
        }

        public void assertFavoriteBookIs(final BookDto bookDto) {
            assertLabel(FAVORITE_BOOK_SCALAR_NAME, "Favorite Book:");
            assertComponent(FAVORITE_BOOK_ENTITY_LINK, BookmarkablePageLink.class);

            val expectedLinkTitle = bookFactory.apply(bookDto).title();
            assertLabel(FAVORITE_BOOK_ENTITY_LINK_TITLE, expectedLinkTitle);
        }

        public void assertInventoryNameIs(final String expectedName) {
            assertPropertyValue(SimulatedProperties.INVENTORY_NAME.id(), adapter->{
                assertEquals(expectedName, adapter.getPojo());
            });
        }

        public void dumpComponentTree(final Predicate<Component> filter) {
            getLastRenderedPage().visitChildren(new IVisitor<Component, Object>() {
                @Override
                public void component(final Component component, final IVisit<Object> visit) {
                    if(filter.test(component)) {

                        val inversePath = new ArrayList<String>();
                        var comp = component;

                        while(comp!=null) {
                            inversePath.add(comp.getId());
                            comp = comp.getParent();
                        }

                        val path = Can.ofCollection(inversePath)
                        .reverse()
                        .stream()
                        .skip(1L)
                        .collect(Collectors.joining(":"));

                        System.err.printf("comp[%s]: %s -> %s: %s%n",
                                path, component, component.getClass().getSimpleName(),
                                component.getDefaultModelObjectAsString());
                    }
                }
            });
        }

        /**
         * Renders the {@link EntityPage}.
         * @see #startPage(IPageProvider)
         */
        public EntityPage startEntityPage(final PageParameters pageParameters) {
            val entityPage = EntityPage.forPageParameters(getMetaModelContext(), pageParameters);
            val startedPage = startPage(entityPage);
            assertRenderedPage(EntityPage.class);
            return startedPage;
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WicketTesterFactory {

        private final MetaModelContext commonContext;

        public EntityPageTester createTester(final Function<BookDto, IBook> bookFactory) {
            return new EntityPageTester(commonContext, bookFactory);
        }
    }

    @Bean @Inject
    public WicketTesterFactory wicketTesterFactory(final MetaModelContext commonContext) {
        return new WicketTesterFactory(commonContext);
    }

    // -- HELPER -- APPLICATION (WICKET)

    private static WebApplication newWicketApplication(final MetaModelContext commonContext) {
        val wicketApplication = new WicketApplication_forTesting(commonContext);
        ThreadContext.setApplication(wicketApplication);
        return wicketApplication;
    }

    @RequiredArgsConstructor
    private static class PageFactory_forTesting implements IPageFactory {

        private final WicketApplication_forTesting holder;
        private final IPageFactory delegate;

        @Override
        public <C extends IRequestablePage> C newPage(final Class<C> pageClass, final PageParameters parameters) {
            if(EntityPage.class.equals(pageClass)) {
                return _Casts.uncheckedCast(EntityPage.forPageParameters(holder.getMetaModelContext(), parameters));
            }
            return delegate.newPage(pageClass, parameters);
        }

        @Override
        public <C extends IRequestablePage> C newPage(final Class<C> pageClass) {
            if(EntityPage.class.equals(pageClass)) {
                throw _Exceptions.illegalArgument("cannot instantiate EntityPage without PageParameters");
            }
            return delegate.newPage(pageClass);
        }

        @Override
        public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass) {
            if(EntityPage.class.equals(pageClass)) {
                return true;
            }
            return delegate.isBookmarkable(pageClass);
        }
    }

    @RequiredArgsConstructor
    static class WicketApplication_forTesting
    extends WebApplication
    implements
        HasComponentFactoryRegistry,
        HasMetaModelContext {
        private static final long serialVersionUID = 1L;

        @Override
        protected void init() {
            super.init();
            getCspSettings().blocking().disabled(); // since Wicket 9, CSP is enabled by default [https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP]
            final IBootstrapSettings settings = new BootstrapSettings();
            settings.setDeferJavascript(false);
            Bootstrap.install(this, settings);
            getHeaderResponseDecorators().add(response ->
                new ResourceAggregator(new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS")));
            //XXX set to false for less strict testing
            getDebugSettings().setComponentUseCheck(false);
        }

        @Getter
        private final MetaModelContext metaModelContext;

        @Getter(lazy=true)
        private final ComponentFactoryRegistry componentFactoryRegistry =
                lookupServiceElseFail(ComponentFactoryRegistry.class);

        @Getter(lazy=true)
        private final PageClassRegistry pageClassRegistry =
                lookupServiceElseFail(PageClassRegistry.class);

        @Override
        public Class<? extends Page> getHomePage() {
            return getPageClassRegistry().getPageClass(PageType.HOME);
        }

        @Override
        protected IPageFactory newPageFactory() {
            return new PageFactory_forTesting(this, super.newPageFactory());
        }

        @Override
        protected void internalInit() {
            super.internalInit();
            // intercept AJAX requests and reload view-models so any detached entities are re-fetched
            CausewayWicketAjaxRequestListenerUtil.setRootRequestMapper(this, metaModelContext);
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
//        private MetaModelContext commonContext;
//
//        public WicketApplication_forTesting() {
//            setRootRequestMapper(new SystemMapper(this));
//        }
//
//        @Override
//        public MetaModelContext getCommonContext() {
//            if(commonContext==null) {
//                commonContext = MetaModelContext.of(mmc);
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
