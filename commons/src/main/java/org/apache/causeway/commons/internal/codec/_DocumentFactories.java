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
package org.apache.causeway.commons.internal.codec;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.jdom2.input.SAXBuilder;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 *
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">cheatsheetseries.owasp.org</a>
 *
 * @since 2.0
 */
@UtilityClass
public class _DocumentFactories {

    public DocumentBuilderFactory documentBuilderFactory() {
        val df = DocumentBuilderFactory.newInstance();
        df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // XML parsers should not be vulnerable to XXE attacks
        df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // XML parsers should not be vulnerable to XXE attacks
        return df;
    }

    public DocumentBuilder documentBuilder() throws ParserConfigurationException {
        /*sonar-ignore-on*/
        return documentBuilderFactory().newDocumentBuilder();
        /*sonar-ignore-off*/
    }

    public TransformerFactory transformerFactory() {
        val tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // XML transformers should be secured
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // XML transformers should be secured
        return tf;
    }

    public Transformer transformer() throws TransformerConfigurationException {
        return transformerFactory().newTransformer();
    }

    public XMLInputFactory xmlInputFactory() {
        val xmlInputFactory = XMLInputFactory.newInstance();

        // disables DTDs entirely
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        // disable external entities
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        return xmlInputFactory;
    }

    public SAXBuilder saxBuilder() {
        /*sonar-ignore-on*/
        val builder = new SAXBuilder();
        /*sonar-ignore-off*/
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return builder;
    }




}
