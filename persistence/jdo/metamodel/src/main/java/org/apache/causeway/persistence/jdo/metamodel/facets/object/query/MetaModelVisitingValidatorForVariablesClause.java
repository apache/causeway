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
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

class MetaModelVisitingValidatorForVariablesClause
extends MetaModelVisitingValidatorForClauseAbstract {

    MetaModelVisitingValidatorForVariablesClause(final MetaModelContext mmc) {
        super(mmc, "VARIABLES");
    }

    @Override
    String deriveClause(final String query) {
        return JdoQueryAnnotationFacetFactory.variables(query);
    }

    @Override
    void postInterpretJdoql(
            final String classNameFromClause, // actually class not logical type!
            final ObjectSpecification objectSpec,
            final String query) {

        val persistenceCapableFacetIfAny = Try.call(
                ()->getSpecificationLoader()
                    .specForType(_Context.loadClass(classNameFromClause))
                    .map(spec->spec.getFacet(JdoPersistenceCapableFacet.class))
                    .orElse(null)
                )
                .getValue();

        if(persistenceCapableFacetIfAny.isEmpty()) {

            val cls = objectSpec.getCorrespondingClass();

            ValidationFailure.raise(
                    objectSpec.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(cls)),
                    String.format(
                            "%s: error in JDOQL query, class name for '%s' "
                            + "clause is not annotated as @PersistenceCapable (JDOQL : %s)",
                            cls.getName(),
                            clause,
                            query)
                    );
        }
    }

}
