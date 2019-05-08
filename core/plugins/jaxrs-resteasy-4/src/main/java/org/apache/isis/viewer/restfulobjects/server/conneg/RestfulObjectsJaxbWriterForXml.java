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
package org.apache.isis.viewer.restfulobjects.server.conneg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlRootElementProvider;

import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

@Provider
@Produces({"application/xml", "application/*+xml", "text/*+xml"})
public class RestfulObjectsJaxbWriterForXml extends JAXBXmlRootElementProvider {

    public RestfulObjectsJaxbWriterForXml(){
        ;
    }

    @Override protected boolean isReadWritable(
            final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return super.isReadWritable(type, genericType, annotations, mediaType) && hasXRoDomainTypeParameter(mediaType);
    }

    protected boolean hasXRoDomainTypeParameter(final MediaType mediaType) {
        return Util.hasXRoDomainTypeParameter(mediaType);
    }

    @Override
    protected Marshaller getMarshaller(
            final Class<?> type, final Annotation[] annotations, final MediaType mediaType) {
        final Marshaller marshaller = super.getMarshaller(type, annotations, mediaType);
        marshaller.setAdapter(PersistentEntityAdapter.class, new PersistentEntityAdapter() {
            @Override
            protected BookmarkService getBookmarkService() {
                return getServiceRegistry().lookupServiceElseFail(BookmarkService.class);
            }
        });
        return marshaller;
    }

    ServiceRegistry getServiceRegistry() {
        return IsisContext.getServiceRegistry();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }
}
