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

package org.apache.isis.core.runtime.snapshot;

import java.io.CharArrayWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

public class DomSerializerJaxp implements DomSerializer {

    private final TransformerFactory transformerFactory;
    private final Transformer newTransformer;

    public DomSerializerJaxp() throws TransformerConfigurationException {
        this.transformerFactory = TransformerFactory.newInstance();
        this.newTransformer = transformerFactory.newTransformer();
    }

    @Override
    public String serialize(final Element domElement) {
        final CharArrayWriter caw = new CharArrayWriter();
        try {
            serializeTo(domElement, caw);
            return caw.toString();
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public void serializeTo(final Element domElement, final OutputStream os) throws Exception {
        final OutputStreamWriter osw = new OutputStreamWriter(os);
        serializeTo(domElement, osw);
    }

    @Override
    public void serializeTo(final Element domElement, final Writer writer) throws Exception {
        final Source source = new DOMSource(domElement);
        final Result result = new StreamResult(writer);
        newTransformer.transform(source, result);
    }

}
