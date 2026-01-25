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

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter @Accessors(fluent = true)
public final class LobRequestHandler implements IRequestHandler, Serializable {
    private static final long serialVersionUID = 1L;
    
	private final NamedWithMimeType lob;
    /**
     * Duration for which the resource will be cached by the browser.
     * Set to Duration.ZERO to disable browser caching.
     */
    private final @org.springframework.lang.Nullable Duration cacheDuration;

    public static LobRequestHandler downloadHandler(
            final ObjectAction action,
            final Object value) {
        if(value instanceof NamedWithMimeType) {
            return new LobRequestHandler((NamedWithMimeType)value, action.getSemantics().isIdempotentOrCachable()
                   ? null
                   : Duration.ZERO);
        }
        return null;
    }

    @Override
    public void respond(IRequestCycle requestCycle) {
        var handler = new ResourceStreamRequestHandler(
            lob instanceof Blob
                ? resourceStream((Blob)lob)
                : lob instanceof Clob
                    ? resourceStream((Clob)lob)
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
        return resourceStream(clob.toBlobUtf8());
        // [CAUSEWAY-3958] has issues with CSV files
		// return new StringResourceStream(clob.chars(), clob.mimeType().toString());
    }

    private IResourceStream resourceStreamUnmatched() {
        throw _Exceptions.unmatchedCase(lob);
    }

}
