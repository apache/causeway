package org.apache.isis.objectstore.jdo.metamodel.facets.object.query;

import java.util.List;
import java.util.Objects;

import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

class VisitorForFromClause extends VisitorForClauseAbstract {

    VisitorForFromClause(
            final JdoQueryAnnotationFacetFactory specificationLoader) {
        super(specificationLoader, "FROM");
    }

    @Override
    String deriveClause(final String query) {
        return JdoQueryAnnotationFacetFactory.from(query);
    }

    @Override
    void postInterpretJdoql(
            final String classNameFromClause,
            final ObjectSpecification objectSpec,
            final String query,
            final ValidationFailures validationFailures) {

        final String className = objectSpec.getCorrespondingClass().getName();
        if (Objects.equals(classNameFromClause, className)) {
            return;
        }
        final ObjectSpecification fromSpec = getSpecificationLoader().loadSpecification(classNameFromClause);
        List<ObjectSpecification> subclasses = fromSpec.subclasses(Hierarchical.Depth.TRANSITIVE);
        if(subclasses.contains(objectSpec)) {
            return;
        }
        validationFailures.add(
                "%s: error in JDOQL query, class name after '%s' clause should be same as class name on which annotated, or one of its supertypes (JDOQL : %s)",
                className, clause, query);
    }


}
