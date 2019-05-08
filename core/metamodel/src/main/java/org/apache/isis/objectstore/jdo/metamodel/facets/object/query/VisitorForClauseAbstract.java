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
package org.apache.isis.objectstore.jdo.metamodel.facets.object.query;

import java.util.List;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

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
            final ValidationFailures validationFailures) {
        validate(objectSpec, validationFailures);
        return true;
    }

    private void validate(
            final ObjectSpecification objectSpec,
            final ValidationFailures validationFailures) {
        final JdoQueryFacet facet = objectSpec.getFacet(JdoQueryFacet.class);
        if(facet == null) {
            return;
        }
        final List<JdoNamedQuery> namedQueries = facet.getNamedQueries();
        for (final JdoNamedQuery namedQuery : namedQueries) {
            if(namedQuery.getLanguage().equals("JDOQL")) {
                final String query = namedQuery.getQuery();
                final String fromClassName = deriveClause(query);
                interpretJdoql(fromClassName, objectSpec, query, validationFailures);
            }
        }
    }

    private void interpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final ValidationFailures validationFailures) {

        if (classNameFromClause == null) {
            return;
        }

        final String className = objectSpec.getCorrespondingClass().getName();
        if (getSpecificationLoader().loadSpecification(classNameFromClause)==null) {
            validationFailures.add(
                    "%s: error in JDOQL query, class name for '%s' clause not recognized (JDOQL : %s)",
                    className, clause, query);
            return;
        }

        postInterpretJdoql(classNameFromClause, objectSpec, query, validationFailures);
    }

    abstract String deriveClause(final String query);

    abstract void postInterpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final ValidationFailures validationFailures);


    SpecificationLoader getSpecificationLoader() {
        return facetFactory.getSpecificationLoader();
    }

}
