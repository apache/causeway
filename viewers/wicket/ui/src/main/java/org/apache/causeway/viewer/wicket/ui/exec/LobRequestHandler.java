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
package org.apache.causeway.viewer.wicket.ui.exec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Duration;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

public record LobRequestHandler(
    NamedWithMimeType lob,
    /**
     * Duration for which the resource will be cached by the browser.
     * Set to Duration.ZERO to disable browser caching.
     */
    @Nullable Duration cacheDuration) implements IRequestHandler, Serializable {

    public static LobRequestHandler downloadHandler(
            final ObjectAction action,
            final Object value) {
        if(value instanceof NamedWithMimeType lob) {
            return new LobRequestHandler(lob, action.getSemantics().isIdempotentOrCachable()
                   ? null
                   : Duration.ZERO);
        }
        return null;
    }

    @Override
    public void respond(IRequestCycle requestCycle) {
        var handler = new ResourceStreamRequestHandler(
            lob instanceof Blob blob
                ? resourceStream(blob)
                : lob instanceof Clob clob
                    ? resourceStream(clob)
                    : resourceStreamUnmatched(),
            lob.name());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        handler.setCacheDuration(cacheDuration);
        handler.respond(requestCycle);
    }

    // -- HELPER

    private IResourceStream resourceStream(Blob blob) {
        final IResourceStream resourceStream = new AbstractResourceStream() {
            private static final long serialVersionUID = 1L;
            @Override public InputStream getInputStream() throws ResourceStreamNotFoundException {
                return new ByteArrayInputStream(blob.bytes());
            }
            @Override public String getContentType() {
                return blob.mimeType().toString();
            }
            @Override public void close() throws IOException {
            }
        };
        return resourceStream;
    }

    private IResourceStream resourceStream(Clob clob) {
        return new StringResourceStream(clob.chars(), clob.mimeType().toString());
    }

    private IResourceStream resourceStreamUnmatched() {
        throw _Exceptions.unmatchedCase(lob);
    }

}
