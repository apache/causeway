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

package org.apache.isis.alternatives.remoting.transport.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.isis.alternatives.remoting.transport.TransportAbstract;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.io.LazyInputStream;

public class HttpClientTransport extends TransportAbstract {

    private HttpClient httpClient;
    private String url;

    private ByteArrayOutputStream outputStream;
    private InputStream inputStream;

    public HttpClientTransport(IsisConfiguration configuration) {
        super(configuration);
    }

    // ///////////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////////

    public void init() {
        httpClient = new HttpClient();
        url = getConfiguration().getString(HttpRemotingConstants.URL_KEY, HttpRemotingConstants.URL_DEFAULT);
    }

    public void shutdown() {
        httpClient = null;
    }

    // ///////////////////////////////////////////////////////////
    // connect, disconnect
    // ///////////////////////////////////////////////////////////

    public void connect() throws IOException {
        outputStream = new ByteArrayOutputStream();
    }

    public void disconnect() {
        inputStream = null;
        outputStream = null;
    }

    // ///////////////////////////////////////////////////////////
    // streams
    // ///////////////////////////////////////////////////////////

    /**
     * Returns an {@link OutputStream} that writes into the request body of an HTTP POST, and will send on
     * {@link OutputStream#flush() flush}.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * When first called, executes the HTTP POST, then returns the response body.
     * 
     * <p>
     * Subsequent calls return the same input stream, at whatever position they have been processed.
     */
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new LazyInputStream(new LazyInputStream.InputStreamProvider() {
                @Override
                public InputStream getInputStream() throws IOException {
                    PostMethod postMethod = new PostMethod(url);

                    // copy over
                    InputStreamRequestEntity requestEntity =
                        new InputStreamRequestEntity(new ByteArrayInputStream(outputStream.toByteArray()));
                    postMethod.setRequestEntity(requestEntity);

                    // execute
                    httpClient.executeMethod(postMethod);

                    // clear for next time
                    outputStream.reset();

                    // return response
                    return postMethod.getResponseBodyAsStream();
                }
            });
        }
        return inputStream;
    }

}
