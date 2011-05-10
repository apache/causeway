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
package org.apache.isis.viewer.restful.viewer.xom;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

public class XhtmlRendererXom {

    public Element div_p(final String paragraphText, final String htmlClassAttribute) {
        final Element div = div(htmlClassAttribute);
        div.appendChild(p(paragraphText, htmlClassAttribute));
        return div;
    }

    public Element p(final String paragraphText, final String htmlClassAttribute) {
        return builder("p").append(paragraphText).classAttr(htmlClassAttribute).build();
    }

    public Element p(final boolean condition, final String htmlClassAttribute) {
        return p(condition ? "Y" : "N", htmlClassAttribute);
    }

    public Element div(final String htmlClassAttribute) {
        return builder("div").classAttr(htmlClassAttribute).build();
    }

    public Element ul(final String htmlClassAttribute) {
        final Element ul = new Element("ul");
        addClassAttribute(ul, htmlClassAttribute);
        return ul;
    }

    public Element li_a(final String uri, final String aHrefText, final String aHrefRel, final String aHrefRev,
        final String htmlClassAttribute) {
        final Element li = new Element("li");
        li.appendChild(aHref(uri, aHrefText, aHrefRel, aHrefRev, htmlClassAttribute));
        return li;
    }

    public Element li_p(final String paragraphText, final String htmlClassAttribute) {
        final Element li = new Element("li");
        li.appendChild(p(paragraphText, htmlClassAttribute));
        return li;
    }

    public Element aHref(final String aHref, final String aHrefText, final String aHrefRel, final String aHrefRev,
        final String htmlClassAttribute) {
        final Element a = new Element("a");
        a.appendChild(aHrefText);
        a.addAttribute(new Attribute("href", aHref));
        a.addAttribute(new Attribute("rel", aHrefRel));
        a.addAttribute(new Attribute("rev", aHrefRev));
        addClassAttribute(a, htmlClassAttribute);
        return a;
    }

    public <T> Element table(final List<TableColumn<T>> columns, final List<T> rows, final String htmlClassAttribute) {
        final Element table = new Element("table");
        table.addAttribute(new Attribute("border", "1"));
        table.appendChild(appendTrTh(columns));
        appendTrTd(table, columns, rows);
        return table;
    }

    private static <T> Element appendTrTh(final List<TableColumn<T>> columns) {
        final Element tr = new Element("tr");
        for (final TableColumn<T> column : columns) {
            tr.appendChild(column.th());
        }
        return tr;
    }

    private static <T> void appendTrTd(final Element table, final List<TableColumn<T>> columns, final List<T> rows) {
        for (final T row : rows) {
            final Element tr = new Element("tr");
            for (final TableColumn<T> column : columns) {
                tr.appendChild(column.td(row));
            }
            table.appendChild(tr);
        }
    }

    public Element dl(final String htmlClassAttribute) {
        final Element dl = new Element("dl");
        addClassAttribute(dl, htmlClassAttribute);
        return dl;
    }

    public DtDd dt_dd(final String dtText, final String ddText, final String htmlClassAttribute) {
        final Element dt = new Element("dt");
        dt.appendChild(dtText);
        final Element dd = new Element("dd");
        dd.appendChild(ddText);
        return new DtDd(dt, dd);
    }

    /**
     * Creates a POST form.
     * 
     * <pre>
     *   &lt;form name=&quot;[propertyName]&quot; action=&quot;form&quot; method=&quot;POST&quot;&gt;
     *     &lt;input type=&quot;text&quot; name=&quot;value&quot; value=&quot;&quot; class=&quot;isis-property&quot;&gt;
     *     &lt;input type=&quot;submit&quot; value=&quot;Modify&quot;&gt;
     *   &lt;/form&gt;
     * </pre>
     * 
     * @param property
     * @return
     */
    public Element form(final String formName, final String htmlClassAttribute) {
        return builder("form").attr("name", formName).build();
    }

    private static void addClassAttribute(final Element el, final String htmlClassAttribute) {
        if (htmlClassAttribute == null) {
            return;
        }
        el.addAttribute(new Attribute("class", htmlClassAttribute));
    }

    private ElementBuilderXom builder(final String elementName) {
        return new ElementBuilderXom(elementName);
    }

}
