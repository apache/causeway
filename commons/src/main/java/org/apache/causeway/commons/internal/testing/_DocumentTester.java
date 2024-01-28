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
package org.apache.causeway.commons.internal.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.codec._DocumentFactories;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class _DocumentTester {

    @SneakyThrows
    public void assertXmlEqualsIgnoreOrder(final @NonNull String xml1, final @NonNull String xml2) {
        var doc1 = _DocumentFactories.parseDocument(xml1);
        var doc2 = _DocumentFactories.parseDocument(xml2);
        doc1.normalizeDocument();
        doc2.normalizeDocument();
        _Assert.assertTrue(doc1.isEqualNode(doc2));
    }

    /**
     * @see "https://www.baeldung.com/jackson-compare-two-json-objects"
     */
    @SneakyThrows
    public void assertJsonEqualsIgnoreOrder(final @NonNull String json1, final @NonNull String json2) {
        var mapper = new ObjectMapper();
        _Assert.assertEquals(mapper.readTree(json1), mapper.readTree(json2));
    }

    @SneakyThrows
    public void assertYamlEqualsIgnoreOrder(final @NonNull String yaml1, final @NonNull String yaml2) {
        var mapper = new ObjectMapper(new YAMLFactory());
        _Assert.assertEquals(mapper.readTree(yaml1), mapper.readTree(yaml2));
    }

}
