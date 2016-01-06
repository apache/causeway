/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.core.metamodel.facets.object.layoutxml;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.applib.layout.v1_0.DomainObject;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class LayoutXmlFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutXmlFacetFactory.class);

    public LayoutXmlFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(LayoutXmlFacetDefault.create(facetHolder, readMetadata(cls)));
    }

    private final Set<Class<?>> blacklisted = Sets.newConcurrentHashSet();

    private DomainObject readMetadata(final Class<?> domainClass) {

        if(blacklisted.contains(domainClass)) {
            return null;
        }
        final String xml;

        final String resourceName = domainClass.getSimpleName() + ".layout.xml";
        try {
            xml = ClassExtensions.resourceContentOf(domainClass, resourceName);
        } catch (IOException | IllegalArgumentException ex) {

            blacklisted.add(domainClass);
            final String message = String .format(
                    "Failed to locate file %s (relative to %s.class); ex: %s)",
                    resourceName, domainClass.getName(), ex.getMessage());

            LOG.debug(message);
            return null;
        }

        try {
            final JaxbService jaxbService = servicesInjector.lookupService(JaxbService.class);
            final DomainObject metadata = jaxbService.fromXml(DomainObject.class, xml);
            return metadata;
        } catch(Exception ex) {

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            LOG.warn(message);

            return null;
        }
    }

    private ServicesInjector servicesInjector;

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
