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
package org.apache.isis.persistence.jdo.metamodel.facets.object.query;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

class VisitorForVariablesClause extends VisitorForClauseAbstract {

    VisitorForVariablesClause(final JdoQueryAnnotationFacetFactory facetFactory) {
        super(facetFactory, "VARIABLES");
    }

    @Override
    String deriveClause(final String query) {
        return JdoQueryAnnotationFacetFactory.variables(query);
    }

    @Override
    void postInterpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final MetaModelValidator validator) {


        val cls = objectSpec.getCorrespondingClass();
        val objectSpecification = getSpecificationLoader()
                .loadSpecification(
                        classNameFromClause, 
                        IntrospectionState.TYPE_INTROSPECTED);
        
        JdoPersistenceCapableFacet persistenceCapableFacet =
                objectSpecification.getFacet(JdoPersistenceCapableFacet.class);

        if(persistenceCapableFacet == null) {
            validator.onFailure(
                    objectSpec,
                    Identifier.classIdentifier(LogicalType.fqcn(cls)),
                    "%s: error in JDOQL query, class name for '%s' clause is not annotated as @PersistenceCapable (JDOQL : %s)",
                    cls.getName(), clause, query);
            return;
        }
    }

}
