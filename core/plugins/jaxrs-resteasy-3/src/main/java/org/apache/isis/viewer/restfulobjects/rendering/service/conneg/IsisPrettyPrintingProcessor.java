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
package org.apache.isis.viewer.restfulobjects.rendering.service.conneg;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.annotations.DecorateTypes;
import org.jboss.resteasy.spi.DecoratorProcessor;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DecorateTypes({"text/*+xml", "application/*+xml"})
public class IsisPrettyPrintingProcessor implements DecoratorProcessor<Marshaller, PrettyPrinting> {

    public static final String KEY_PRETTY_PRINT = "isis.services." + ContentNegotiationServiceXRoDomainType.class.getSimpleName() + ".prettyPrint";

    @Override
    public Marshaller decorate(final Marshaller target, final PrettyPrinting annotation, @SuppressWarnings("rawtypes") final Class type, final Annotation[] annotations, final MediaType mediaType) {
        return shouldPrettyPrint()
                ? doDecorate(target, annotation, type, annotations, mediaType)
                        : target;
    }

    protected boolean shouldPrettyPrint() {
        try {
            return getConfiguration().getBoolean(KEY_PRETTY_PRINT, _Context.isPrototyping());
        } catch (Exception e) {
            return true;
        }
    }

    protected Marshaller doDecorate(Marshaller target, PrettyPrinting annotation, @SuppressWarnings("rawtypes") Class type, Annotation[] annotations, MediaType mediaType) {
        try {
            target.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            return target;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected IsisConfiguration getConfiguration() {
        return getIsisSessionFactory().getConfiguration();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }
}
