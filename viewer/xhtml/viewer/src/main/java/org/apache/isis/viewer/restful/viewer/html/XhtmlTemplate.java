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
package org.apache.isis.viewer.restful.viewer.html;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.isis.viewer.restful.viewer.tree.Attribute;
import org.apache.isis.viewer.restful.viewer.tree.Element;
import org.jdom.output.XMLOutputter;


/**
 * TODO: add in support for base URI in generated XML docs?
 */
public class XhtmlTemplate {

    private final Element html;
    private Element head;
    private Element body;

    public XhtmlTemplate(final String titleStr, final HttpServletRequest servletRequest,
        final String... javaScriptFiles) {
        this(titleStr, servletRequest.getSession().getServletContext(), javaScriptFiles);
    }

    private XhtmlTemplate(final String titleStr, final ServletContext servletContext, final String... javaScriptFiles) {
        this.html = new Element("html");
        addHeadAndTitle(titleStr);
        for (final String javaScriptFile : javaScriptFiles) {
            final Element script = new Element("script");
            script.addAttribute(new Attribute("type", "text/javascript"));
            script.addAttribute(new Attribute("src", "/" + javaScriptFile));
            script.appendChild(""); // force the </script> to be separate.
            head.appendChild(script);
        }
        addBody();
    }

    private void addHeadAndTitle(final String titleStr) {
        head = new Element("head");
        html.appendChild(head);
        final Element title = new Element("title");
        title.appendChild(titleStr);
        head.appendChild(title);
    }

    /**
     * Adds a &lt;body id=&quot;body&quot;> element.
     * 
     * <p>
     * The <tt>id</tt> attribute is so that Javascript can use <tt>document.getElementById(&quot;body&quot;);</tt>
     * 
     */
    private void addBody() {
        body = new Element("body");
        body.addAttribute(new Attribute("id", "body"));
        html.appendChild(body);
    }

    public XhtmlTemplate appendToBody(final Element... elements) {
        for (final Element element : elements) {
            body.appendChild(element);
        }
        return this;
    }

    public void appendToDiv(final Element div, final Element... elements) {
        for (final Element element : elements) {
            div.appendChild(element);
        }
    }

    public String toXML() {
        return xmlUsingToXmlMethod();
    }

    private String xmlUsingToXmlMethod() {
        org.jdom.Document document = new org.jdom.Document(html.xmlElement);
        StringWriter sw = new StringWriter();
        XMLOutputter xmlOutputter = new XMLOutputter();
        try {
            xmlOutputter.output(document, sw);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
