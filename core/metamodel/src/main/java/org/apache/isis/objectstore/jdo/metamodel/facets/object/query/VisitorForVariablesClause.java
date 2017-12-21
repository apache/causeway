package org.apache.isis.objectstore.jdo.metamodel.facets.object.query;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

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
            final ValidationFailures validationFailures) {


        final String className = objectSpec.getCorrespondingClass().getName();

        ObjectSpecification objectSpecification = getSpecificationLoader().loadSpecification(classNameFromClause);
        JdoPersistenceCapableFacet persistenceCapableFacet =
                objectSpecification.getFacet(JdoPersistenceCapableFacet.class);

        if(persistenceCapableFacet == null) {
            validationFailures.add(
                    "%s: error in JDOQL query, class name for '%s' clause is not annotated as @PersistenceCapable (JDOQL : %s)",
                    className, clause, query);
            return;
        }
    }

}
