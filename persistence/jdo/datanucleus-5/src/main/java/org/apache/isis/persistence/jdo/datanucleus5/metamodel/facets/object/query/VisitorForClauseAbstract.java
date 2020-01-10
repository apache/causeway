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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.query;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.val;

abstract class VisitorForClauseAbstract implements MetaModelValidatorVisiting.Visitor {

    private final JdoQueryAnnotationFacetFactory facetFactory;
    final String clause;

    VisitorForClauseAbstract(
            final JdoQueryAnnotationFacetFactory facetFactory,
            final String clause) {
        
        this.facetFactory = facetFactory;
        this.clause = clause;
    }

    @Override
    public boolean visit(
            final ObjectSpecification objectSpec,
            final MetaModelValidator validator) {
        
        validate(objectSpec, validator);
        return true;
    }

    private void validate(
            final ObjectSpecification objectSpec,
            final MetaModelValidator validator) {
        
        final JdoQueryFacet facet = objectSpec.getFacet(JdoQueryFacet.class);
        if(facet == null) {
            return;
        }
        final List<JdoNamedQuery> namedQueries = facet.getNamedQueries();
        for (final JdoNamedQuery namedQuery : namedQueries) {
            if(namedQuery.getLanguage().equals("JDOQL")) {
                final String query = namedQuery.getQuery();
                final String fromClassName = deriveClause(query);
                interpretJdoql(fromClassName, objectSpec, query, validator);
            }
        }
    }

    private void interpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final MetaModelValidator validator) {

        if (_Strings.isNullOrEmpty(classNameFromClause)) {
            return;
        }

        val className = objectSpec.getCorrespondingClass().getName();
        if (getSpecificationLoader().loadSpecification(ObjectSpecId.of(classNameFromClause))==null) {
            validator.onFailure(
                    objectSpec,
                    Identifier.classIdentifier(className),
                    "%s: error in JDOQL query, class name for '%s' clause not recognized (JDOQL : %s)",
                    className, clause, query);
            return;
        }

        postInterpretJdoql(classNameFromClause, objectSpec, query, validator);
    }

    abstract String deriveClause(final String query);

    abstract void postInterpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final MetaModelValidator validator);


    SpecificationLoader getSpecificationLoader() {
        return facetFactory.getSpecificationLoader();
    }

}
