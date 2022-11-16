/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.causeway.persistence.jdo.metamodel.facets.prop.column;

import java.util.Optional;
import java.util.function.Consumer;

import javax.jdo.annotations.Column;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _ColumnUtil {

    void processColumnAnnotations(final ProcessMethodContext processMethodContext,
            final Consumer<Optional<javax.jdo.annotations.Column>> onJdoColumn,
            final Consumer<Optional<javax.persistence.Column>> onJpaColumn) {

        val jdoColumnIfAny = processMethodContext.synthesizeOnMethod(javax.jdo.annotations.Column.class);
        if(jdoColumnIfAny.isPresent()) {
            onJdoColumn.accept(jdoColumnIfAny);
        } else {
            val jpaColumnIfAny = processMethodContext.synthesizeOnMethod(javax.persistence.Column.class);
            if(jpaColumnIfAny.isPresent()) {
                onJpaColumn.accept(jpaColumnIfAny);
            }
        }

    }

    void inferSemantics(final ProcessMethodContext processMethodContext,
            final Consumer<Semantics> onColumnPresent,
            final Consumer<Semantics> onColumnNotPresent) {
        val jdoColumnIfAny = processMethodContext.synthesizeOnMethod(javax.jdo.annotations.Column.class);
        if(jdoColumnIfAny.isPresent()) {
            onColumnPresent.accept(inferSemantics(processMethodContext, jdoColumnIfAny));
            return;
        }
        val jpaColumnIfAny = processMethodContext.synthesizeOnMethod(javax.persistence.Column.class);
        if(jpaColumnIfAny.isPresent()) {
            onColumnPresent.accept(Semantics.of(!jpaColumnIfAny.get().nullable()));
            return;
        }
        onColumnNotPresent.accept(inferSemantics(processMethodContext, Optional.empty()));
    }

    // -- HELPER

    private Semantics inferSemantics(
            final ProcessMethodContext processMethodContext,
            final Optional<Column> columnIfAny) {

        final String allowsNull = columnIfAny.isPresent()
                ? columnIfAny.get().allowsNull()
                : null;

        if(_Strings.isNotEmpty(allowsNull)) {
            // if miss-spelled, then DN assumes is not-nullable
            return Semantics.of(!"true".equalsIgnoreCase(allowsNull.trim()));
        }

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        // per JDO spec
        return returnType != null
                && returnType.isPrimitive()
            ? Semantics.REQUIRED
            : Semantics.OPTIONAL;

    }

}
