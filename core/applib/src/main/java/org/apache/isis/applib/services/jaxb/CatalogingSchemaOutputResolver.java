/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

/**
 * An implementation of {@link SchemaOutputResolver} that keeps track of all the schemas for which it has
 * {@link #createOutput(String, String) created} an output {@link StreamResult} containing the content of the schema.
 */
class CatalogingSchemaOutputResolver extends SchemaOutputResolver {

    private static final String SCHEMA_LOCATION_INCORRECT = "http://isis.apache.org/schema/common";
    private static final String SCHEMA_LOCATION_CORRECT = "http://isis.apache.org/schema/common/common.xsd";

    private final JaxbService.IsisSchemas isisSchemas;
    private final List<String> namespaceUris = _Lists.newArrayList();

    public CatalogingSchemaOutputResolver(final JaxbService.IsisSchemas isisSchemas) {
        this.isisSchemas = isisSchemas;
    }

    public List<String> getNamespaceUris() {
        return namespaceUris;
    }

    private Map<String, StreamResultWithWriter> schemaResultByNamespaceUri = _Maps.newLinkedHashMap();

    public String getSchemaTextFor(final String namespaceUri) {
        final StreamResultWithWriter streamResult = schemaResultByNamespaceUri.get(namespaceUri);
        if (streamResult == null) {
            return null;
        }
        String xsd = streamResult.asString();

        try {
            final DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder parser = docBuildFactory.newDocumentBuilder();
            final Document document = parser.parse(new InputSource(new StringReader(xsd)));

            final Element el = document.getDocumentElement();
            replaceCommonSchemaLocationIfAny(el);

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            xsd = writer.toString();

        } catch(Exception ex) {
            // ignore
        }

        return xsd;
    }

    // replace <xs:import namespace="..." schemaLocation="http://isis.apache.org/schema/common"/>
    // with    <xs:import namespace="..." schemaLocation="http://isis.apache.org/schema/common/common.xsd"/>
    private static void replaceCommonSchemaLocationIfAny(final Node node) {
        if(schemaLocationReplacedIn(node)) {
            return;
        }
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                replaceCommonSchemaLocationIfAny(currentNode);
            }
        }
    }

    private static boolean schemaLocationReplacedIn(final Node node) {
        if(node instanceof Element) {
            final Element importEl = (Element) node;
            final Attr schemaLocationAttr = importEl.getAttributeNode("schemaLocation");
            if(schemaLocationAttr != null) {
                final String value = schemaLocationAttr.getValue();
                if(SCHEMA_LOCATION_INCORRECT.endsWith(value)) {
                    schemaLocationAttr.setValue(SCHEMA_LOCATION_CORRECT);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Result createOutput(
            final String namespaceUri, final String suggestedFileName) throws IOException {

        final StreamResultWithWriter result = new StreamResultWithWriter();

        result.setSystemId(namespaceUri);

        if (isisSchemas.shouldIgnore(namespaceUri)) {
            // skip
        } else {
            namespaceUris.add(namespaceUri);
            schemaResultByNamespaceUri.put(namespaceUri, result);
        }

        return result;
    }

    public Map<String, String> asMap() {
        final Map<String,String> map = _Maps.newLinkedHashMap();
        final List<String> namespaceUris = getNamespaceUris();

        for (String namespaceUri : namespaceUris) {
            map.put(namespaceUri, getSchemaTextFor(namespaceUri));
        }

        return Collections.unmodifiableMap(map);
    }
}
