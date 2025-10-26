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

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jsoup.nodes.Element;
import org.jsoup.select.CombiningEvaluator;
import org.jsoup.select.Evaluator;

import org.springframework.util.StringUtils;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

/**
 * Jsoup API extensions.
 *
 * <h1>- internal use only -</h1>
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @apiNote use via <pre>@ExtensionMethod({_JsoupExt.class})</pre>
 * @since 4.0
 */
@UtilityClass
public class _JsoupExt {

    @FunctionalInterface
    public interface HtmlAppender<T> {
        Element appendTo(Element container, T model);
    }
    @FunctionalInterface
    public interface HtmlAppender2<T1, T2> {
        Element appendTo(Element container, T1 model1, T2 model2);
    }
    @FunctionalInterface
    public interface HtmlAppender3<T1, T2, T3> {
        Element appendTo(Element container, T1 model1, T2 model2, T3 model3);
    }

    // -- EVALUATOR LOGIC

    public Evaluator not(final Evaluator evaluator) {
        return new Evaluator() {
            @Override
            public boolean matches(final Element root, final Element element) {
                return !evaluator.matches(root, element);
            }
        };
    }

    public Evaluator and(final Evaluator a, final Evaluator b) {
        return new CombiningEvaluator.And(List.of(a, b));
    }

    // -- ATTRIB

    public Element alt(final Element container, final String alt) {
        return container.attr("alt", alt);
    }

    /**
     * Directly replaces the current "class" attribute of the container.
     */
    public Element classes(final Element container, final String cssClasses) {
        return container.attr("class", cssClasses);
    }

    public Element addClasses(final Element container, final String cssClass) {
        if(StringUtils.hasText(cssClass)) {
            Stream.of(StringUtils.delimitedListToStringArray(cssClass, " "))
                .filter(s->StringUtils.hasLength(s))
                .forEach(container::addClass);
        }
        return container;
    }

    public Element addClassNormalized(final Element container, final String cssClass) {
        return container.addClass(cssNormalize(cssClass));
    }

    public Element href(final Element container, final String href) {
        return container.attr("href", href);
    }

    public Element onClick(final Element container, final String onClick) {
        return container.attr("onClick", onClick);
    }

    public Element role(final Element container, final String role) {
        return container.attr("role", role);
    }

    public Element src(final Element container, final String src) {
        return container.attr("src", src);
    }

    public Element tabindex(final Element container, final String tabindex) {
        return container.attr("tabindex", tabindex);
    }

    public Element type(final Element container, final String type) {
        return container.attr("type", type);
    }

    // -- APPENDERS

    public Element appendDiv(final Element container) {
        return container.appendElement("div");
    }
    public Element appendDiv(final Element container, final String cssClass) {
        return container.appendElement("div")
            .attr("class", cssClass);
    }

    public Element appendOl(final Element container) {
        return container.appendElement("ol");
    }
    public Element appendOl(final Element container, final String cssClass) {
        return container.appendElement("ol")
            .attr("class", cssClass);
    }

    public Element appendUl(final Element container) {
        return container.appendElement("ul");
    }
    public Element appendUl(final Element container, final String cssClass) {
        return container.appendElement("ul")
            .attr("class", cssClass);
    }

    public Element appendLi(final Element container) {
        return container.appendElement("li");
    }
    public Element appendLi(final Element container, final String cssClass) {
        return container.appendElement("li")
            .attr("class", cssClass);
    }

    public Element appendSpan(final Element container) {
        return container.appendElement("span");
    }
    public Element appendSpan(final Element container, final String cssClass) {
        return container.appendElement("span")
            .attr("class", cssClass);
    }

    // -- UTIL

    //TODO must be disable for the coll endpoint, otherwise invalid ajax payload
    public Element comment(final Element container, final String comment) {
        //return container.append("<!-- " + comment + " -->");
        return container;
    }

    /**
     * Alias for {@link Element#appendText(String)}
     */
    public Element text(final Element container, final String text) {
        return container.appendText(text);
    }

    public Element italic(final Element container, final String text) {
        container.appendElement("i").appendText(text);
        return container;
    }

    public Element bold(final Element container, final String text) {
        container.appendElement("b").appendText(text);
        return container;
    }

    public <T> Element model(final Element container, final HtmlAppender<T> appender, final T model) {
        comment(container, appender.getClass().getName());
        appender.appendTo(container, model);
        return container;
    }

    public <T1, T2> Element model(final Element container, final HtmlAppender2<T1, T2> appender,
            final T1 model1, final T2 model2) {
        comment(container, appender.getClass().getName());
        appender.appendTo(container, model1, model2);
        return container;
    }

    public <T1, T2, T3> Element model(final Element container, final HtmlAppender3<T1, T2, T3> appender,
        final T1 model1, final T2 model2, final T3 model3) {
        comment(container, appender.getClass().getName());
        appender.appendTo(container, model1, model2, model3);
        return container;
}

    /**
     * Unconditional branching.
     */
    public Element branch(final Element container, final Consumer<Element> branchAppender) {
        branchAppender.accept(container);
        return container;
    }

    /**
     * Conditional branching, based on condition.
     */
    public Element branch(final Element container, final boolean condition,
        final Consumer<Element> branchAppender) {
        if(condition) branchAppender.accept(container);
        return container;
    }
    /**
     * Conditional branching with either/or semantics, based on condition.
     */
    public Element branch(final Element container, final boolean condition,
        final Consumer<Element> branchAppender, final Consumer<Element> orElseAppender) {
        if(condition) {
            branchAppender.accept(container);
        } else {
            orElseAppender.accept(container);
        }
        return container;
    }

    /**
     * Conditional branching, based on presence of given modelOpt.
     */
    public <T> Element branch(final Element container, final Optional<T> modelOpt,
        final BiConsumer<Element, T> branchAppender) {
        modelOpt.ifPresent(t->branchAppender.accept(container, t));
        return container;
    }

    /**
     * Conditional branching, based on presence of given modelOpt.
     */
    public <T> Element branch(final Element container, final Optional<T> modelOpt,
        final BiConsumer<Element, T> branchAppender, final Consumer<Element> fallbackAppender) {
        modelOpt
            .ifPresentOrElse(
                t->branchAppender.accept(container, t),
                ()->fallbackAppender.accept(container));
        return container;
    }

    /**
     * Conditional mapping, based on presence of given modelOpt.
     */
    public <T, R> R map(final Element element, final Optional<T> modelOpt,
        final BiFunction<Element, T, R> mapper, final Function<Element, R> fallbackMapper) {
        return modelOpt
            .map(t->mapper.apply(element, t))
            .orElseGet(()->fallbackMapper.apply(element));
    }

    // -- HELPER

    private String cssNormalize(final String cssClass) {
        var trimmed = _Strings.blankToNullOrTrim(cssClass);
        return _Strings.isNullOrEmpty(trimmed)
                ? null
                : cssClass.replaceAll("\\.", "-").replaceAll("[^A-Za-z0-9- ]", "").replaceAll("\\s+", "-");
    }

}
