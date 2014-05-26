/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import java.net.URLConnection;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Time;

/**
 * Adapted from {@link org.apache.wicket.request.resource.ByteArrayResource}
 */
public class CharSequenceResource extends AbstractResource {
    private static final long serialVersionUID = 1L;

    /** the content type. */
    private final String contentType;

    private CharSequence chars;

    /** the time that this resource was last modified; same as construction time. */
    private final Time lastModified = Time.now();

    private final String filename;

    public CharSequenceResource(final String contentType, final CharSequence chars, final String filename)
    {
        this.contentType = contentType;
        this.chars = chars;
        this.filename = filename;
    }

    protected void configureResponse(final ResourceResponse response, final Attributes attributes)
    {
    }

    /**
     * @see org.apache.wicket.request.resource.AbstractResource#newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)
     */
    @Override
    protected ResourceResponse newResourceResponse(final Attributes attributes)
    {
        final ResourceResponse response = new ResourceResponse();

        String contentType = this.contentType;

        if (contentType == null)
        {
            if (filename != null)
            {
                contentType = URLConnection.getFileNameMap().getContentTypeFor(filename);
            }

            if (contentType == null)
            {
                contentType = "application/octet-stream";
            }
        }


        response.setContentType(contentType);
        response.setLastModified(lastModified);

        final CharSequence data = getData(attributes);
        if (data == null)
        {
            response.setError(404 /* HttpServletResponse.SC_NOT_FOUND */);
        }
        else
        {
            response.setContentLength(data.length());

            if (response.dataNeedsToBeWritten(attributes))
            {
                if (filename != null)
                {
                    response.setFileName(filename);
                    response.setContentDisposition(ContentDisposition.ATTACHMENT);
                }
                else
                {
                    response.setContentDisposition(ContentDisposition.INLINE);
                }

                response.setWriteCallback(new WriteCallback()
                {
                    @Override
                    public void writeData(final Attributes attributes)
                    {
                        //attributes.getResponse().write(data);
                        attributes.getResponse().write(data);
                    }
                });

                configureResponse(response, attributes);
            }
        }

        return response;
    }

    protected CharSequence getData(final Attributes attributes)
    {
        return chars;
    }
}
