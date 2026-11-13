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
package org.apache.causeway.commons.internal.html;

import java.util.Optional;
import java.util.function.Consumer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

/**
 * Bootstrap API on top of Jsoup.
 *
 * <h1>- internal use only -</h1>
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @apiNote use via <pre>@ExtensionMethod({_Bootstrap.Extensions.class})</pre>
 * @since 4.0
 */
@UtilityClass
@ExtensionMethod({_JsoupExt.class})
public class _Bootstrap {

    public record BootstrapSettings(
        String bootstrapVersion,
        /** MINIMIZED is recommended for production */
        ResourceVariant resourceVariant,
        /**
         * jQuery is not required by Bootstrap, but e.g. by datables.net
         * <p> if provided gets prepended before bootstrap resources
         */
        Optional<String> jqueryVersionOpt) {

        public BootstrapSettings {
            resourceVariant = resourceVariant!=null
                    ? resourceVariant
                    : ResourceVariant.MINIMIZED;
            jqueryVersionOpt = jqueryVersionOpt!=null
                    ? jqueryVersionOpt.filter(StringUtils::hasText)
                    : Optional.empty();
        }
    }

    /**
     * Used for JS and CSS resource referencing.
     */
    public enum ResourceVariant {
        /** optionally for prototyping, useful for resource debugging */
        PLAIN,
        /** recommended for production */
        MINIMIZED
    }

    public enum ButtonVariant {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO,
        LIGHT,
        DARK,
        LINK;
        public String cssClass(final boolean outlined) {
            return outlined
                ? "btn-outline-" + name().toLowerCase()
                : "btn-" + name().toLowerCase();
        }
    }

    @RequiredArgsConstructor
    public enum ButtonSizeModifier {
        SMALL("btn-sm"),
        LARGE("btn-lg");
        public final String cssClass;
    }

    /// @see <a href="https://getbootstrap.com/docs/5.3/layout/containers/">bootstrap</a>
    public enum ContainerBreakPoint {
        DEFAULT,
        XS,
        SM,
        MD,
        LG,
        XL,
        XXL,
        FLUID;
        public String cssClass() {
            if(this == DEFAULT) return "container";
            return "container-" + name().toLowerCase();
        }
    }

    public enum GridOption {
        DEFAULT,
        SM,
        MD,
        LG,
        XL,
        XXL;
        public String colCssClass(final int size) {
            if(this == DEFAULT) return "col-" + size;
            return "col-" + name().toLowerCase() + "-" + size;
        }
    }

    public enum TooltipPlacement {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT;
        public String id() {
            return name().toLowerCase();
        }
    }

    public record Page(Document doc, Element content, Element scriptsContainer) {
        public Page setTitle(final String title) {
            head().getElementsByTag("title").forEach(Element::remove);
            head().appendElement("title").appendText(title);
            return this;
        }
        public Page addStyle(final String style) {
            head().appendElement("style").append(style);
            return this;
        }
        public Page addCssLink(final @Nullable String href) {
            return addCssLink(Optional.ofNullable(href));
        }
        public Page addCssLink(final @Nullable Optional<String> hrefOpt) {
            (hrefOpt!=null
                    ? hrefOpt.filter(StringUtils::hasText)
                    : Optional.<String>empty())
                .ifPresent(href->Extensions.appendCssLink(head(), href));
            return this;
        }
        public Page addScriptLink(final @Nullable String href) {
            return addScriptLink(Optional.ofNullable(href));
        }
        public Page addScriptLink(final @Nullable Optional<String> hrefOpt) {
            (hrefOpt!=null
                    ? hrefOpt.filter(StringUtils::hasText)
                    : Optional.<String>empty())
                .ifPresent(href->scriptsContainer.appendElement("script")
                        .src(href));
            return this;
        }
        public Element head() {
            return doc.head();
        }
        public String toHtml() {
            return doc.outerHtml();
        }
        public Page addMeta(final String name, final String content) {
            doc.head().appendElement("meta")
                .attr("name", name)
                .attr("content", content);
            return this;
        }
    }

    public record Modal(
        Element element,
        Element header,
        Element body,
        Element footer) {
        public static Modal create(final Element container) {
            var modal = container.appendDiv("modal")
                .attr("tabindex", "-1");
            var dialog = modal.appendDiv("modal-dialog");
            var content = dialog.appendDiv("modal-content");
            var header = content.appendDiv("modal-header");
            var body = content.appendDiv("modal-body");
            var footer = content.appendDiv("modal-footer");
            return new Modal(modal, header, body, footer);
        }
    }

    public record CardHeaderless(Element element, Element body) {
        public static CardHeaderless create(final Element container) {
            var card = container.appendDiv("card");
            var body = card.appendDiv("card-body");
            return new CardHeaderless(card, body);
        }
    }
    public record Card(Element element, Element header, Element titleSpan, Element body) {
        public static Card create(final Element container) {
            var card = container.appendDiv("card");
            var header = card.appendDiv("card-header");
            var titleSpan = header.appendSpan("card-title");
            var body = card.appendDiv("card-body");
            return new Card(card, header, titleSpan, body);
        }
        public Card title(final String text) {
            titleSpan.empty();
            titleSpan.appendText(text);
            return this;
        }
    }

    public record Table(Element element, String id, Element headRow, Element body, Element footRow) {
        public static Table create(final Element container, final String id) {
            var table = container.appendElement("table")
                //.addClasses("table table-sm table-striped table-hover table-bordered")
                .addClasses("table table-sm table-striped")
                .attr("cellspacing", "0");

            table.comment("head row");
            var headRow = table.appendElement("thead")
                .appendElement("tr");

            table.comment("content rows");
            var tbody = table.appendElement("tbody");

            table.comment("foot row");
            var footRow = table.appendElement("tfoot")
                .appendElement("tr");

            return new Table(table, id, headRow, tbody, footRow);
        }

        public Element appendRow() {
            return body.appendElement("tr");
        }

        public Element appendHeaderCol() {
            return headRow().appendElement("th")
                .attr("scope", "col");
        }

        public int colCount() {
            return headRow().childrenSize();
        }

        public Element appendFooterColSpanningAll() {
            return footRow.appendElement("td")
                .attr("colspan", "" + colCount());
        }

    }

    public record TabGroup(Element element, String id, Element ul, Element content) {
        public static TabGroup create(final Element container, final String id) {
            var tabs = container.appendDiv("tabGroups");

            tabs.comment("nav tabs");
            var ul =  tabs.appendUl("nav nav-tabs")
                .role("tablist");

            tabs.comment("tab panes");
            var content = tabs.appendDiv("tab-content");

            return new TabGroup(tabs, id, ul, content);
        }
        public TabGroup addActiveTab(final String text) {
            return addTab(text, true);
        }
        public TabGroup addTab(final String text) {
            return addTab(text, false);
        }
        public TabGroup addTab(final String text, final boolean active) {
            var tabPanelId = id() + "-" + tabCount();

            ul.appendLi("nav-item")
                .role("presentation")

                .appendElement("button")
                .addClass("nav-link")
                .branch(active, button->button.addClass("active"))
                .role("tab")
                .type("button")
                .attr("data-bs-toggle", "tab")
                .attr("data-bs-target", "#" + tabPanelId)

                .appendElement("span")
                .appendText(text);
            return this;
        }
        public Element appendContentPane(final boolean active) {
            var tabPanelId = id() + "-" + content().childrenSize();
            return content.appendDiv("tab-pane fade")
                .branch(active, div->div.addClass("show active"))
                .id(tabPanelId)
                .role("tabpanel")
                .tabindex("0");
        }
        public int tabCount() {
            return ul().childrenSize();
        }

    }

    public record Breadcrumbs(Element element, Element ol) {
        public static Breadcrumbs create(final Element container) {
            var breadcrumbs = container.appendElement("nav");
            var ol = breadcrumbs
                .appendOl("breadcrumb");

//TODO missing tooltip handler
//TODO object icon stuff
//                <span class="objectIconAndTitlePanel" id="id3cd"> <a
//                        href="./dita.globodiet.params.food_list.Food.Manager:%7C%7C"
//                        class="objectUrlSource wkt-component-with-tooltip" id="id38b"
//                        rel="popover"
//                        data-bs-content="Manage Food&lt;br/&gt;-&lt;br/&gt;Food, Product, On-the-fly Recipe or Alias"
//                        data-bs-original-title="Manager"> <span
//                            class="objectIconFa objectIconFa-table_row"><span><i
//                                    class="fa fa-fw fa-solid fa-utensils food-color"></i></span></span> <span
//                            class="objectTitle">Manage Food</span>
//                    </a>
//                </span>

            return new Breadcrumbs(breadcrumbs, ol);
        }
        public Breadcrumbs item(final Consumer<Element> appender) {
            appender.accept(ol.appendLi("breadcrumb-item")
                .appendSpan());
            return this;
        }
        public Breadcrumbs item(final String text) {
            return item(span->span.appendText(text));
        }
    }

    public Page page(final BootstrapSettings settings) {
        var doc = new Document("");
        doc.attr("lang", "en");
        doc.appendChild(new DocumentType("html", "", ""));
        doc.head().appendElement("meta")
            .attr("charset", "utf-8");
        doc.head().appendElement("meta")
            .attr("name", "viewport")
            .attr("content", "width=device-width, initial-scale=1");

        var content = doc.body().appendElement("div")
            .addClass(ContainerBreakPoint.FLUID.cssClass())
            .comment("content here");

        var scriptsContainer = doc.body()
            .appendDiv()
            .comment("scripts last");

        var suffix = settings.resourceVariant() == ResourceVariant.MINIMIZED ? ".min" : "";

        return new Page(doc, content, scriptsContainer)
            // jQuery -  not required by bootstrap, but e.g. by datables.net - jQuery should be initialized before bootstrap
            .addScriptLink(settings.jqueryVersionOpt()
                    .map(jqueryVersion->"/webjars/jquery/%s/jquery%s.js".formatted(jqueryVersion, suffix)))
            // bootstrap (bundles popper.js)
            .addCssLink("/webjars/bootstrap/%s/css/bootstrap%s.css".formatted(settings.bootstrapVersion(), suffix))
            .addScriptLink("/webjars/bootstrap/%s/js/bootstrap.bundle%s.js".formatted(settings.bootstrapVersion(), suffix));
    }

    // -- API EXTENSION

    @UtilityClass
    public static class Extensions {

        public Element appendButton(final Element container) {
            return container.appendElement("button")
                .attr("type", "button");
        }

        public Element appendButton(final Element container, final ButtonVariant buttonVariant) {
            return appendButton(container)
                .addClass("btn")
                .addClass(buttonVariant.cssClass(false));
        }
        public Element appendButton(final Element container, final ButtonVariant buttonVariant, final ButtonSizeModifier size) {
            return appendButton(container, buttonVariant)
                .addClass(size.cssClass);
        }

        public Element appendButtonOutlined(final Element container, final ButtonVariant buttonVariant) {
            return appendButton(container)
                .addClass("btn")
                .addClass(buttonVariant.cssClass(true));
        }
        public Element appendButtonOutlined(final Element container, final ButtonVariant buttonVariant, final ButtonSizeModifier size) {
            return appendButtonOutlined(container, buttonVariant)
                .addClass(size.cssClass);
        }

        public Element appendCssLink(final Element container, final String href) {
            return container.appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", href);
        }

        public Element appendJsLink(final Element container, final String href) {
            return container.appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", href);
        }

        // -- BOOTSTRAP INLINED

        public Element caret(final Element container) {
            container.appendSpan("caret");
            return container;
        }

        public Element clearfix(final Element container) {
            container.appendDiv("clearfix");
            return container;
        }

        public Element faIcon(final Element container, final String cssClasses) {
            container.appendElement("i")
                .attr("class", cssClasses);
            return container;
        }

        public Element tooltip(final Element container, final TooltipPlacement placement, final Consumer<Element> tooltipContentCallback) {
            var doc = new Document("");
            tooltipContentCallback.accept(doc.body());
            container.addClass("birdseye-tooltip-trigger")
                .attr("data-bs-toggle", "tooltip")
                .attr("data-bs-custom-class", "birdseye-tooltip")
                .attr("data-bs-placement", placement.id())
                .attr("data-bs-html", "true")
                .attr("data-bs-title", doc.body().html());
            return container;
        }

        // -- BOOTSTRAP SIMPLE

        public Element appendRow(final Element container) {
            return container.appendElement("div")
                .addClass("row");
        }

        public Element appendCol(final Element container, final GridOption gridOption, final int size) {
            return container.appendElement("div")
                .addClass(gridOption.colCssClass(size));
        }

        public Element appendTooltip(final Element container, final GridOption gridOption, final int size) {
            return container.appendElement("div")
                .addClass(gridOption.colCssClass(size));
        }

        // -- BOOTSTRAP CONTAINER

        public Breadcrumbs appendBreadcrumbs(final Element container) {
            return Breadcrumbs.create(container);
        }

        public Card appendCard(final Element container) {
            var card = Card.create(container);
            return card;
        }

        public CardHeaderless appendCardHeaderless(final Element container) {
            var card = CardHeaderless.create(container);
            return card;
        }

        public Modal appendModal(final Element container, final String title) {
            var modal = Modal.create(container);
            modal.header().appendElement("h3").addClass("modal-title").appendText(title);
            appendButton(modal.header())
                .attr("data-bs-dismiss", "modal")
                .attr("aria-label", "Close")
                .addClass("btn-close");
            appendButton(modal.footer(), ButtonVariant.SECONDARY)
                .attr("data-bs-dismiss", "modal")
                .appendText("Close");
            appendButton(modal.footer(), ButtonVariant.PRIMARY)
                .appendText("Save changes");
            return modal;
        }

        public Table appendTable(final Element container, final String id) {
            return Table.create(container, id);
        }

        public TabGroup appendTabGroup(final Element container, final String id) {
            return TabGroup.create(container, id);
        }

    }

}
