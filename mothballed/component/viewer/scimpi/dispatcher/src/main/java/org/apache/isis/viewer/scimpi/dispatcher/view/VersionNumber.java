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
package org.apache.isis.viewer.scimpi.dispatcher.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class VersionNumber extends AbstractElementProcessor {

    private static final String MARKER = "Implementation-Build: ";
    private static final String FILE = "/META-INF/MANIFEST.MF";
    private String version;

    @Override
    public String getName() {
        return "version-number";
    }

    @Override
    public void process(final Request request) {
        if (version == null) {
            version = "0000"; // default revision number
            loadRevisonNumber(request.getContext());
        }
        request.appendHtml(version);
    }

    private void loadRevisonNumber(final RequestContext context) {
        BufferedReader reader;
        try {
            String file = FILE;

            file = context.findFile(FILE);
            reader = new BufferedReader(new InputStreamReader(context.openStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(MARKER)) {
                    version = line.substring(MARKER.length());
                    break;
                }
            }
            reader.close();
        } catch (final IOException e) {
            throw new ScimpiException(e);
        }
    }

}
