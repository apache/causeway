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
package org.apache.causeway.persistence.jdo.metamodel.facets.object.query;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.query.JdoQueryFacet;

import lombok.val;

abstract class MetaModelVisitingValidatorForClauseAbstract
extends MetaModelVisitingValidatorAbstract {

    final String clause;

    protected MetaModelVisitingValidatorForClauseAbstract(
            final MetaModelContext mmc,
            final String clause) {
        super(mmc);
        this.clause = clause;
    }

    @Override
    public void validate(final ObjectSpecification objectSpec) {

        if(objectSpec.isInjectable()) {
            return;
        }

        val jdoQueryFacet = objectSpec.getFacet(JdoQueryFacet.class);
        if(jdoQueryFacet == null) {
            return;
        }
        for (val namedQuery : jdoQueryFacet.getNamedQueries()) {
            if(namedQuery.getLanguage().equals("JDOQL")) {
                final String query = namedQuery.getQuery();
                final String fromClassName = deriveClause(query);
                interpretJdoql(fromClassName, objectSpec, query);
            }
        }
    }

    private void interpretJdoql(
            final String typeNameFromClause,
            final ObjectSpecification objectSpec,
            final String query) {

        if (_Strings.isNullOrEmpty(typeNameFromClause)) {
            return;
        }

        val cls = objectSpec.getCorrespondingClass();

        val fromSpecResult = Try.call(()->getSpecificationLoader()
                .specForType(_Context.loadClass(typeNameFromClause))
                .orElse(null));

        if(!fromSpecResult.getValue().isPresent()) {
            ValidationFailure.raise(
                    objectSpec.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(cls)),
                    String.format(
                            "%s: error in JDOQL query, class name for '%s' clause not recognized (JDOQL : %s)",
                            cls.getName(),
                            clause,
                            query)
                    );
            return;
        }

        postInterpretJdoql(typeNameFromClause, objectSpec, query);
    }

    abstract String deriveClause(final String query);

    abstract void postInterpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query);

}
