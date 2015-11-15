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
package org.apache.isis.schema.services.jaxb;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.services.jaxb.JaxbService;

/**
 * An implementation of {@link SchemaOutputResolver} that keeps track of all the schemas for which it has
 * {@link #createOutput(String, String) created} an output {@link StreamResult} containing the content of the schema.
 */
class CatalogingSchemaOutputResolver extends SchemaOutputResolver
{
    private final JaxbService.IsisSchemas isisSchemas;
    private List<String> namespaceUris = Lists.newArrayList();

    public CatalogingSchemaOutputResolver(final JaxbService.IsisSchemas isisSchemas) {
        this.isisSchemas = isisSchemas;
    }

    public List<String> getNamespaceUris() {
        return namespaceUris;
    }

    private Map<String, StreamResultWithWriter> schemaResultByNamespaceUri = Maps.newLinkedHashMap();

    public String getSchemaTextFor(final String namespaceUri) {
        final StreamResultWithWriter streamResult = schemaResultByNamespaceUri.get(namespaceUri);
        return streamResult != null? streamResult.asString(): null;
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
        final Map<String,String> map = Maps.newLinkedHashMap();
        final List<String> namespaceUris = getNamespaceUris();

        for (String namespaceUri : namespaceUris) {
            map.put(namespaceUri, getSchemaTextFor(namespaceUri));
        }

        return Collections.unmodifiableMap(map);
    }
}
