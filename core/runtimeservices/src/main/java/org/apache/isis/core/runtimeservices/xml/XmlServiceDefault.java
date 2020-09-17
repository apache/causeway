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
package org.apache.isis.core.runtimeservices.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Named;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.xml.XmlService;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.codec._DocumentFactories;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisRuntimeServices.XmlService")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@Log4j2
public class XmlServiceDefault implements XmlService {

    @Override
    public Document asDocument(String xmlStr) {
        try {
            final StringReader reader = new StringReader(xmlStr);
            final StreamSource streamSource = new StreamSource(reader);
            final DOMResult result = new DOMResult();

            final Transformer transformer = _DocumentFactories.transformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(streamSource, result);

            final Node node = result.getNode();
            return (Document) node;
        } catch (TransformerException e) {
            throw new Exception(e);
        }
    }

    @Override
    public String asString(final Document doc) {
        try {
            final DOMSource domSource = new DOMSource(doc);
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);

            final Transformer transformer = _DocumentFactories.transformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerConfigurationException e) {
            throw new IsisException(e);
        } catch (TransformerException e) {
            throw new IsisException(e);
        }
    }

    @Override
    public Element getChildElement(final Element el, final String tagname) {
        NodeList elementsByTagName = el.getElementsByTagName(tagname);
        final int length = elementsByTagName.getLength();
        if(length != 1 || !(elementsByTagName.item(0) instanceof Element)) {
            throw new IllegalArgumentException("unable to locate " + tagname + " element");
        }
        final Element item = (Element) elementsByTagName.item(0);
        return item;
    }

    @Override
    public String getChildTextValue(final Element el) {
        final NodeList childNodes = el.getChildNodes();
        if(childNodes.getLength() !=1 || !(childNodes.item(0) instanceof Text)) {
            throw new IllegalArgumentException("unable to locate child Text node");
        }
        final Text referenceText = (Text) childNodes.item(0);
        return referenceText.getData();
    }

}
