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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.entity;


import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoMetamodelUtil;

import lombok.val;

/**
 * Implements {@link ObjectSpecIdFacetFactory} only because 
 * is a prereq of {@link DomainObjectAnnotationFacetFactory}.
 */
public class JdoEntityFacetFactory
extends FacetFactoryAbstract
implements ObjectSpecIdFacetFactory {

    public JdoEntityFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ObjectSpecIdFacetFactory.ProcessObjectSpecIdContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

        final PersistenceCapable annotation = Annotations.getAnnotation(cls, PersistenceCapable.class);
        if (annotation == null) {
            return;
        }
        String annotationSchemaAttribute = annotation.schema();
        if(_Strings.isNullOrEmpty(annotationSchemaAttribute)) {
            annotationSchemaAttribute = null;
        }
        String annotationTableAttribute = annotation.table();
        if (_Strings.isNullOrEmpty(annotationTableAttribute)) {
            annotationTableAttribute = cls.getSimpleName();
        }
        
        val facetHolder = processClassContext.getFacetHolder();
        
        val embeddedOnlyAttribute = annotation.embeddedOnly();
        // Whether objects of this type can only be embedded, 
        // hence have no ID that binds them to the persistence layer
        final boolean embeddedOnly = Boolean.valueOf(embeddedOnlyAttribute)
                || Annotations.getAnnotation(cls, EmbeddedOnly.class)!=null; 
        
        if(embeddedOnly) {
            // suppress
        } else {
            val jdoPersistenceCapableFacet = new JdoEntityFacet(
                    facetHolder, 
                    isisInteractionTrackerLazy);
            FacetUtil.addFacet(jdoPersistenceCapableFacet);
        }

        return;
    }
    
    // -- INTERACTION TRACKER LOOKUP
    
    private final _Lazy<InteractionTracker> isisInteractionTrackerLazy = _Lazy.threadSafe(this::lookupIsisInteractionTracker); 
    private InteractionTracker lookupIsisInteractionTracker() {
        return super.getMetaModelContext().getServiceRegistry().lookupServiceElseFail(InteractionTracker.class);        
    }
    
    
    
}
