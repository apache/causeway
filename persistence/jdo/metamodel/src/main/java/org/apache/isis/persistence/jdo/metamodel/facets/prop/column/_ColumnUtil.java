package org.apache.isis.persistence.jdo.metamodel.facets.prop.column;

import java.util.Optional;
import java.util.function.Consumer;

import javax.jdo.annotations.Column;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics;

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
