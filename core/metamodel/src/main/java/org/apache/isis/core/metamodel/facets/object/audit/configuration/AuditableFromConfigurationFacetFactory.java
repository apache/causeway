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
package org.apache.isis.core.metamodel.facets.object.audit.configuration;


import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;


public class AuditableFromConfigurationFacetFactory extends FacetFactoryAbstract implements IsisConfigurationAware {

    private static final String AUDIT_OBJECTS_KEY = "isis.services.audit.objects";

    private IsisConfiguration configuration;

    private static enum ObjectCategorization {
        ALL,
        NONE;
        public static ObjectCategorization parse(final String value) {
            if ("all".equalsIgnoreCase(value)) {
                return ALL;
            } else {
                return NONE;
            }
        }
    }
    

    public AuditableFromConfigurationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final String configuredValue = configuration.getString(AUDIT_OBJECTS_KEY);
        final ObjectCategorization categorization = ObjectCategorization.parse(configuredValue);
        if(categorization == ObjectCategorization.NONE) {
            return;
        }
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        
        if(facetHolder.containsDoOpFacet(AuditableFacet.class)) {
            // do not replace
            return;
        }
        if(HasTransactionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return; 
        }
        
        FacetUtil.addFacet(new AuditableFacetFromConfiguration(facetHolder));
        return;
    }

    // //////////////////////////////////////

    
    @Override
    public void setConfiguration(IsisConfiguration configuration) {
        this.configuration = configuration;
    }


}
