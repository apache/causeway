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
package org.apache.isis.applib.services.xmlsnapshot;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class XmlSnapshotServiceAbstract implements XmlSnapshotService {

    @Override
    public Document asDocument(String xmlStr) {
        try {
            final StringReader reader = new StringReader(xmlStr);
            final StreamSource streamSource = new StreamSource(reader);
            final DOMResult result = new DOMResult();
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(streamSource, result);

            final Node node = result.getNode();
            return (Document) node;
        } catch (TransformerException e) {
            throw new XmlSnapshotService.Exception(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getChildElementValue(final Element el, final String tagname, final Class<T> expectedCls) {
        final Element chldEl = getChildElement(el, tagname);
        final String dataType = chldEl.getAttribute("isis:datatype");
        if(dataType == null) {
            throw new IllegalArgumentException("unable to locate " + tagname + "/@datatype attribute");
        }
        if("isis:String".equals(dataType)) {
            return (T)getChildTextValue(chldEl);
        }
        if("isis:LocalDate".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            final DateTimeFormatter forPattern = DateTimeFormat.forPattern("dd-MMM-yyyy").withLocale(Locale.ENGLISH);
            return (T)forPattern.parseLocalDate(str);
        }
        if("isis:Byte".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Byte(str);
        }
        if("isis:Short".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Short(str);
        }
        if("isis:Integer".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Integer(str);
        }
        if("isis:Long".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Long(str);
        }
        if("isis:Float".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Float(str);
        }
        if("isis:Double".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T)new Double(str);
        }
        if("isis:BigDecimal".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T) new BigDecimal(str);
        }
        if("isis:BigInteger".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T) new BigInteger(str);
        }
        if("isis:Boolean".equals(dataType)) {
            final String str = getChildTextValue(chldEl);
            return (T) new Boolean(str);
        }
        throw new IllegalArgumentException(
                "Datatype of '" + dataType + "' for element '" + tagname + "' not recognized");
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
            throw new IllegalArgumentException("unable to locate app:reference/text() node");
        }
        final Text referenceText = (Text) childNodes.item(0);
        return referenceText.getData();
    }

}
