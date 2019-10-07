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
package org.apache.isis.jdo.metamodel;

import org.springframework.stereotype.Component;

import org.apache.isis.jdo.metamodel.specloader.validator.JdoMetaModelValidator;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.FacetProcessingOrder;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.val;

@Component
public class JdoProgrammingModelPlugin implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(ProgrammingModel pm) {

        // come what may, we have to ignore the PersistenceCapable supertype.
        pm.addFactory(FacetProcessingOrder.C2_AFTER_METHOD_REMOVING, RemoveJdoEnhancementTypesFacetFactory.class, Marker.JDO);
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        pm.addFactory(FacetProcessingOrder.C2_AFTER_METHOD_REMOVING, RemoveJdoPrefixedMethodsFacetFactory.class, Marker.JDO);
        // DN 4.x
        pm.addFactory(FacetProcessingOrder.C2_AFTER_METHOD_REMOVING, RemoveDatanucleusPersistableTypesFacetFactory.class, Marker.JDO);
        pm.addFactory(FacetProcessingOrder.C2_AFTER_METHOD_REMOVING, RemoveDnPrefixedMethodsFacetFactory.class, Marker.JDO);

        val config = IsisContext.getConfiguration();
        
        // these validators add themselves to the programming model
        new JdoMetaModelValidator(config, pm);
    }

}
