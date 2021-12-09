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

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

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

import static org.junit.Assert.assertEquals;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.dto.IBook;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;
import org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketAjaxRequestListenerUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    public static class EntityPageTester extends WicketTester {

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
                + ":memberGroup:properties:1"
                + ":property:scalarTypeContainer:scalarIfRegular";

        public static final String FAVORITE_BOOK_SCALAR_NAME = FAVORITE_BOOK_SCALAR
                + ":scalarName";

        public static final String FAVORITE_BOOK_ENTITY_LINK = FAVORITE_BOOK_SCALAR
                + ":entityIconAndTitle:entityLinkWrapper:entityLink";

        public static final String FAVORITE_BOOK_ENTITY_LINK_TITLE = FAVORITE_BOOK_ENTITY_LINK
                + ":entityTitle";

        //property:scalarTypeContainer:scalarIfRegular:scalarValueContainer]: [Fragment [Component id = scalarValueContainer]] ->
        //property:scalarTypeContainer:scalarIfRegular:scalarValueContainer:scalarValue]: [TextField [Component id = scalarValue]] -> Bookstore
        //property:scalarTypeContainer:scalarIfRegular:scalarName]: [Component id = scalarName] -> Name
        //property:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink]: [WebMarkupContainer [Component id = scalarValueInlinePromptLink]] ->
        //property:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink:scalarValueInlinePromptLabel]: [Fragment [Component id = scalarValueInlinePromptLabel]] ->
        //property:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink:scalarValueInlinePromptLabel:scalarValue]: [Component id = scalarValue] -> Bookstore
        //property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueContainer:scalarValue

        public static final String INVENTORY_NAME_PROPERTY = "theme:entityPageContainer:entity:rows:2"
                + ":rowContents:1"
                + ":col:fieldSets:1"
                + ":memberGroup:properties:2"
                + ":property";

        public static final String INVENTORY_NAME_PROPERTY_EDIT_LINK = INVENTORY_NAME_PROPERTY
                + ":scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink";

        public static final String INVENTORY_NAME_PROPERTY_EDIT_INLINE_FORM = INVENTORY_NAME_PROPERTY
                + ":scalarTypeContainer:scalarIfRegularInlinePromptForm:inputForm";

        public static final String INVENTORY_NAME_PROPERTY_EDIT_INLINE_FORM_TEXTFIELD = INVENTORY_NAME_PROPERTY
                + ":property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueContainer:scalarValue";

        public static final String INVENTORY_NAME_PROPERTY_EDIT_INLINE_PROMPT_FORM = INVENTORY_NAME_PROPERTY
                + ":scalarTypeContainer:scalarIfRegularInlinePromptForm:inputForm";

        public static final String INLINE_PROMPT_FORM_FIELD = ""
                + "property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueContainer:scalarValue";

        public static final String INLINE_PROMPT_FORM_OK = INVENTORY_NAME_PROPERTY_EDIT_INLINE_PROMPT_FORM
                + ":okButton";


        //inputForm:property:scalarNameAndValue]: [StringPanel [Component id = scalarNameAndValue]] -> StringPanel: ManagedObject.SimpleManagedObject(specification=ObjectSpecificationDefault@44e816ad[class=java.lang.String,type=VALUE,superclass=Object], pojo=Bookstore)
        //inputForm:property:scalarNameAndValue:scalarTypeContainer]: [WebMarkupContainer [Component id = scalarTypeContainer]] -> WebMarkupContainer:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfCompact]: [Component id = scalarIfCompact] -> Label: Bookstore
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular]: [FormGroup [Component id = scalarIfRegular]] -> FormGroup:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueContainer]: [Fragment [Component id = scalarValueContainer]] -> Fragment:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueContainer:scalarValue]: [TextField [Component id = scalarValue]] -> TextField: Bookstore
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarName]: [Component id = scalarName] -> Label: Name
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink]: [WebMarkupContainer [Component id = scalarValueInlinePromptLink]] -> WebMarkupContainer:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink:scalarValueInlinePromptLabel]: [Fragment [Component id = scalarValueInlinePromptLabel]] -> :
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:scalarValueInlinePromptLink:scalarValueInlinePromptLabel:scalarValue]: [Component id = scalarValue] -> Label: Bookstore
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:associatedActionLinksBelow]: [WebMarkupContainer [Component id = associatedActionLinksBelow]] -> WebMarkupContainer:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:associatedActionLinksRight]: [WebMarkupContainer [Component id = associatedActionLinksRight]] -> WebMarkupContainer:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:editProperty]: [WebMarkupContainer [Component id = editProperty]] -> WebMarkupContainer:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:feedback]: [NotificationPanel [Component id = feedback]] -> NotificationPanel:
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:feedback:feedbackul]: [WebMarkupContainer [Component id = feedbackul]] -> :
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegular:feedback:feedbackul:messages]: [MessageListView [Component id = messages]] -> MessageListView: []
        //inputForm:property:scalarNameAndValue:scalarTypeContainer:scalarIfRegularInlinePromptForm]: [WebMarkupContainer [Component id = scalarIfRegularInlinePromptForm]] -> WebMarkupContainer:

        // --

        @Inject private ObjectManager objectManager;

        private final IsisAppCommonContext commonContext;
        private final Function<BookDto, IBook> bookFactory;

        public EntityPageTester(
                final IsisAppCommonContext commonContext,
                final Function<BookDto, IBook> bookFactory) {
            super(newWicketApplication(commonContext));
            commonContext.injectServicesInto(this);
            this.commonContext = commonContext;
            this.bookFactory = bookFactory;
        }

        public PageParameters createPageParameters(final Object entityOrVm) {
            final ManagedObject domainObject = objectManager.adapt(entityOrVm);
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
            assertLabel(FAVORITE_BOOK_SCALAR_NAME, "Favorite Book");
            assertComponent(FAVORITE_BOOK_ENTITY_LINK, BookmarkablePageLink.class);

            val expectedLinkTitle = bookFactory.apply(bookDto).title();
            assertLabel(FAVORITE_BOOK_ENTITY_LINK_TITLE, expectedLinkTitle);
        }

        public void assertInventoryNameIs(final String expectedName) {
            assertPropertyValue(INVENTORY_NAME_PROPERTY, adapter->{
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
            val entityPage = EntityPage.ofPageParameters(commonContext, pageParameters);
            val startedPage = startPage(entityPage);
            assertRenderedPage(EntityPage.class);
            return startedPage;
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WicketTesterFactory {

        private final IsisAppCommonContext commonContext;

        public EntityPageTester createTester(final Function<BookDto, IBook> bookFactory) {
            return new EntityPageTester(commonContext, bookFactory);
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
    private static class PageFactory_forTesting implements IPageFactory {

        private final WicketApplication_forTesting holder;
        private final IPageFactory delegate;

        @Override
        public <C extends IRequestablePage> C newPage(final Class<C> pageClass, final PageParameters parameters) {
            if(EntityPage.class.equals(pageClass)) {
                return _Casts.uncheckedCast(EntityPage.ofPageParameters(holder.getCommonContext(), parameters));
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

        @Override
        protected IPageFactory newPageFactory() {
            return new PageFactory_forTesting(this, super.newPageFactory());
        }

        @Override
        protected void internalInit() {
            super.internalInit();
            // intercept AJAX requests and reloads JAXB viewmodels so any detached entities are re-fetched
            IsisWicketAjaxRequestListenerUtil.setRootRequestMapper(this, commonContext);
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
