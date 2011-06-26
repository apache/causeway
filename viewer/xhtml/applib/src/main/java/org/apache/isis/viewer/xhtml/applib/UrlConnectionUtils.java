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
package org.apache.isis.viewer.xhtml.applib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Not API, so intentionally not visible outside this package.
 */
final class UrlConnectionUtils {

    static final String MIME_TYPE = "application/xhtml+xml";

    private UrlConnectionUtils() {
    }

    static void writeMapToConnectionOutputStream(final Map<String, String> formArgumentsByParameter,
        final HttpURLConnection connection) throws IOException {
        final OutputStream os = connection.getOutputStream();
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        StringUtils.writeMap(formArgumentsByParameter, writer);
    }

    static Document readDocFromConnectionInputStream(final HttpURLConnection connection) throws JDOMException, IOException {
        final InputStream stream = connection.getInputStream();
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(stream);
    }

    static HttpURLConnection createConnection(final String uri) throws MalformedURLException, IOException {
        final URL url = new URL(uri);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Content-Type", MIME_TYPE);
        return connection;
    }

    static HttpURLConnection createPostConnection(final String uri) throws IOException, ProtocolException {
        final HttpURLConnection connection = createConnection(uri);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        return connection;
    }

    static HttpURLConnection createGetConnection(final String uri) throws IOException, ProtocolException {
        final HttpURLConnection connection = createConnection(uri);
        connection.setRequestMethod("GET");
        return connection;
    }

}
