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
package org.apache.isis.core.metamodel.services.layout;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.ObjectLayoutMetadataService;
import org.apache.isis.core.metamodel.facets.object.layoutmetadata.ObjectLayoutMetadataFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(nature = NatureOfService.DOMAIN)
public class ObjectLayoutMetadataServiceDefault
        implements ObjectLayoutMetadataService, SpecificationLoaderAware {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLayoutMetadataServiceDefault.class);

    @Programmatic
    public ObjectLayoutMetadata fromXml(Class<?> domainClass) {

        final String xml;
        final String resourceName = domainClass.getSimpleName() + ".layout.xml";
        try {
            xml = resourceContentOf(domainClass, resourceName);
        } catch (IOException | IllegalArgumentException ex) {

            final String message = String .format(
                    "Failed to locate file %s (relative to %s.class); ex: %s)",
                    resourceName, domainClass.getName(), ex.getMessage());

            LOG.debug(message);
            return null;
        }

        try {
            final ObjectLayoutMetadata metadata = jaxbService.fromXml(ObjectLayoutMetadata.class, xml);
            return metadata;
        } catch(Exception ex) {

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            LOG.warn(message);

            return null;
        }
    }

    private static String resourceContentOf(final Class<?> cls, final String resourceName) throws IOException {
        final URL url = Resources.getResource(cls, resourceName);
        return Resources.toString(url, Charset.defaultCharset());
    }



    @Override
    public ObjectLayoutMetadata toMetadata(final Object domainObject) {
        return toMetadata(domainObject.getClass());
    }

    @Override
    public ObjectLayoutMetadata toMetadata(final Class<?> domainClass) {
        final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);
        final ObjectLayoutMetadataFacet facet = objectSpec.getFacet(ObjectLayoutMetadataFacet.class);
        return facet != null? facet.getMetadata(): null;
    }

    ////////////////////////////////////////////////////////

    private boolean dynamicReloading;

    @Override
    public void toggleDynamicReloading() {
        this.dynamicReloading = !this.dynamicReloading;
    }

    @Override
    public boolean isDynamicReloading() {
        return this.dynamicReloading;
    }

    //region > injected dependencies

    private SpecificationLoader specificationLookup;

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    @Inject
    JaxbService jaxbService;


    //endregion

}
