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

import jakarta.inject.Inject;

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
import org.apache.wicket.settings.DebugSettings.ClassOutputStrategy;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.function.ThrowingConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.RegularFrame;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.CausewayWicketAjaxRequestListenerUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;

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
                .setOutputMarkupContainerClassNameStrategy(ClassOutputStrategy.HTML_COMMENT);
        }
    }

    public static class DomainObjectPageTester
    extends WicketTester
    implements HasMetaModelContext {

        // -- HOMEPAGE (TEST APP)

        public static final String OPEN_SAMPLE_ACTION = "theme:domainObjectContainer:domainObject:rows:1"
                + ":rowContents:1"
                + ":col:objectHeaderPanel:objectActions:additionalLinkList:additionalLinkItem:1"
                + ":actionLink";

        public static final String OPEN_SAMPLE_ACTION_TITLE = OPEN_SAMPLE_ACTION
                + ":additionalLinkTitle";

        // -- BOOK PAGE

        public static final String BOOK_DELETE_ACTION_JPA = "theme:domainObjectContainer:domainObject:rows:2"
                + ":rowContents:1"
                + ":col:rows:1:rowContents:1:col:rows:1"
                + ":rowContents:1"
                + ":col:fieldSets:1:memberGroup"
                + ":panelHeading:associatedActionLinksPanel"
                + ":additionalLinkList:additionalLinkItem:0:actionLink";

        // -- GENERIC STANDALONE COLLECTION

        public static final String STANDALONE_COLLECTION = "theme:standaloneCollection:standaloneCollection";
        public static final String STANDALONE_COLLECTION_LABEL = STANDALONE_COLLECTION
                + ":actionName";

        // -- INVENTORY JAXB VM

        public static final String FAVORITE_BOOK_SCALAR = "theme:domainObjectContainer:domainObject:rows:2"
                + ":rowContents:1"
                + ":col:fieldSets:1"
                + ":memberGroup:properties:2"
                + ":property:scalarTypeContainer:scalarIfRegular";

        public static final String FAVORITE_BOOK_SCALAR_NAME = FAVORITE_BOOK_SCALAR
                + ":scalarNameBeforeValue";

        public static final String FAVORITE_BOOK_ENTITY_LINK = FAVORITE_BOOK_SCALAR
                + ":container-fieldFrame:scalarValueInlinePromptLink:container-scalarValue:objectLink"
                + ":objectIconAndTitle:objectLinkWrapper:objectLink";

        public static final String FAVORITE_BOOK_ENTITY_LINK_TITLE = FAVORITE_BOOK_ENTITY_LINK
                + ":objectTitle";

        @RequiredArgsConstructor
        public static enum SimulatedProperties implements SimulatedProperty {
            INVENTORY_NAME("theme:domainObjectContainer:domainObject:rows:2"
                    + ":rowContents:1"
                    + ":col:fieldSets:1"
                    + ":memberGroup:properties:1"
                    + ":property"),
            JPA_BOOK_ISBN("theme:domainObjectContainer:domainObject:rows:2"
                    + ":rowContents:1:col:rows:1:rowContents:1"
                    + ":col:rows:1"
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
                return "property:attributeNameAndValue:scalarTypeContainer:scalarIfRegular:"
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

        public DomainObjectPageTester(
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
            assertLabel(FAVORITE_BOOK_SCALAR_NAME, "Favorite Book");
            assertComponent(FAVORITE_BOOK_ENTITY_LINK, BookmarkablePageLink.class);

            var expectedLinkTitle = bookFactory.apply(bookDto).title();
            assertLabel(FAVORITE_BOOK_ENTITY_LINK_TITLE, expectedLinkTitle);
        }

        public void assertInventoryNameIs(final String expectedName) {
            assertPropertyValue(SimulatedProperties.INVENTORY_NAME.id(), adapter->{
                assertEquals(expectedName, adapter.getPojo());
            });
        }

        public void dumpComponentTree(final Predicate<Component> filter) {
            getLastRenderedPage().visitChildren((component, visit) -> {
                if(filter.test(component)) {

                    var inversePath = new ArrayList<String>();
                    var comp = component;

                    while(comp!=null) {
                        inversePath.add(comp.getId());
                        comp = comp.getParent();
                    }

                    var path = Can.ofCollection(inversePath)
                    .reverse()
                    .stream()
                    .skip(1L)
                    .collect(Collectors.joining(":"));

                    System.err.printf("comp[%s]: %s -> %s: %s%n",
                            path, component, component.getClass().getSimpleName(),
                            component.getDefaultModelObjectAsString());
                }
            });
        }

        /**
         * Renders the {@link DomainObjectPage}.
         * @see #startPage(IPageProvider)
         */
        public DomainObjectPage startDomainObjectPage(final PageParameters pageParameters) {
            var domainObjectPage = DomainObjectPage.forPageParameters(pageParameters);
            var startedPage = startPage(domainObjectPage);
            assertRenderedPage(DomainObjectPage.class);
            return startedPage;
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class WicketTesterFactory {
        private final MetaModelContext commonContext;
        public DomainObjectPageTester createTester(final Function<BookDto, IBook> bookFactory) {
            return new DomainObjectPageTester(commonContext, bookFactory);
        }
    }

    @Bean @Inject
    public WicketTesterFactory wicketTesterFactory(final MetaModelContext commonContext) {
        return new WicketTesterFactory(commonContext);
    }

    // -- HELPER -- APPLICATION (WICKET)

    private static WebApplication newWicketApplication(final MetaModelContext commonContext) {
        var wicketApplication = new WicketApplication_forTesting(commonContext);
        ThreadContext.setApplication(wicketApplication);
        return wicketApplication;
    }

    @RequiredArgsConstructor
    private static class PageFactory_forTesting implements IPageFactory {

        private final IPageFactory delegate;

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
                throw _Exceptions.illegalArgument("cannot instantiate DomainObjectPage without PageParameters");
            }
            return delegate.newPage(pageClass);
        }

        @Override
        public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass) {
            if(DomainObjectPage.class.equals(pageClass)) {
                return true;
            }
            return delegate.isBookmarkable(pageClass);
        }
    }

    @RequiredArgsConstructor
    static class WicketApplication_forTesting
    extends WebApplication
    implements
        HasComponentFactoryRegistry{
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
                metaModelContext.lookupServiceElseFail(ComponentFactoryRegistry.class);

        @Getter(lazy=true)
        private final PageClassRegistry pageClassRegistry =
                metaModelContext.lookupServiceElseFail(PageClassRegistry.class);

        @Override
        public Class<? extends Page> getHomePage() {
            return getPageClassRegistry().getPageClass(PageType.HOME);
        }

        @Override
        protected IPageFactory newPageFactory() {
            return new PageFactory_forTesting(super.newPageFactory());
        }

        @Override
        protected void internalInit() {
            super.internalInit();
            // intercept AJAX requests and reload view-models so any detached entities are re-fetched
            CausewayWicketAjaxRequestListenerUtil.setRootRequestMapper(this, metaModelContext);
        }

    }

}
