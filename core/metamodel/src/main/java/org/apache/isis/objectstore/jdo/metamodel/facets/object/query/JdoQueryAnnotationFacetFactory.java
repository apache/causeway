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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class JdoQueryAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public static final String ISIS_REFLECTOR_VALIDATOR_JDOQL_FROM_CLAUSE_KEY = "isis.reflector.validator.jdoqlFromClause";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JDOQL_FROM_CLAUSE_DEFAULT = true;

    public static final String ISIS_REFLECTOR_VALIDATOR_JDOQL_VARIABLES_CLAUSE_KEY = "isis.reflector.validator.jdoqlVariablesClause";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JDOQL_VARIABLES_CLAUSE_DEFAULT = true;

    public JdoQueryAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls)) {
            return;
        }

        final Queries namedQueriesAnnotation = Annotations.getAnnotation(cls, Queries.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        
        if (namedQueriesAnnotation != null) {
            FacetUtil.addFacet(new JdoQueriesFacetAnnotation(
                    namedQueriesAnnotation.value(), facetHolder));
            return;
        }

        final Query namedQueryAnnotation = Annotations.getAnnotation(cls, Query.class);
        if (namedQueryAnnotation != null) {
            FacetUtil.addFacet(new JdoQueryFacetAnnotation(
                    namedQueryAnnotation, facetHolder));
        }
    }

    @Override
    public void refineMetaModelValidator(
            final MetaModelValidatorComposite metaModelValidator,
            final IsisConfiguration configuration) {

        final boolean validateFromClause = configuration.getBoolean(
                                ISIS_REFLECTOR_VALIDATOR_JDOQL_FROM_CLAUSE_KEY,
                                ISIS_REFLECTOR_VALIDATOR_JDOQL_FROM_CLAUSE_DEFAULT);
        if (validateFromClause) {
            final MetaModelValidator queryFromValidator = new MetaModelValidatorVisiting(
                    new MetaModelValidatorVisiting.Visitor() {
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
                                final String query = namedQuery.getQuery();
                                final String fromClassName = from(query);
                                interpret(fromClassName, objectSpec, query, validationFailures);
                            }
                        }

                        void interpret(
                                final String fromClassName, final ObjectSpecification objectSpec,
                                final String query,
                                final ValidationFailures validationFailures) {

                            if(fromClassName == null) {
                                // may be an update statement, for example
                                return;
                            }

                            final String className = objectSpec.getCorrespondingClass().getName();
                            if(!getSpecificationLoader().loaded(fromClassName)) {
                                validationFailures.add(
                                        "%s: error in JDOQL query, class name for FROM clause not recognized (JDOQL : %s)",
                                        className, query);
                                return;

                            }

                            if (Objects.equals(fromClassName, className)) {
                                return;
                            }
                            final ObjectSpecification fromSpec = getSpecificationLoader().loadSpecification(fromClassName);
                            List<ObjectSpecification> subclasses = fromSpec.subclasses(Hierarchical.Depth.TRANSITIVE);
                            if(subclasses.contains(objectSpec)) {
                                return;
                            }
                            validationFailures.add(
                                    "%s: error in JDOQL query, class name after FROM clause should be same as class name on which annotated, or one of its supertypes (JDOQL : %s)",
                                    className, query);
                        }

                    });
            metaModelValidator.add(queryFromValidator);
        }

        final boolean validateVariablesClause = configuration.getBoolean(
                                ISIS_REFLECTOR_VALIDATOR_JDOQL_VARIABLES_CLAUSE_KEY,
                                ISIS_REFLECTOR_VALIDATOR_JDOQL_VARIABLES_CLAUSE_DEFAULT);
        if (validateVariablesClause) {
            final MetaModelValidator queryFromValidator = new MetaModelValidatorVisiting(
                    new MetaModelValidatorVisiting.Visitor() {
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
                            final String className = objectSpec.getCorrespondingClass().getName();
                            final JdoQueryFacet facet = objectSpec.getFacet(JdoQueryFacet.class);
                            if(facet == null) {
                                return;
                            }
                            final List<JdoNamedQuery> namedQueries = facet.getNamedQueries();
                            for (final JdoNamedQuery namedQuery : namedQueries) {
                                final String query = namedQuery.getQuery();
                                final String variablesClassName = variables(query);
                                interpret(variablesClassName, className, query, validationFailures);
                            }
                        }

                        void interpret(
                                final String variablesClassName, final String className,
                                final String query,
                                final ValidationFailures validationFailures) {

                            if(variablesClassName == null) {
                                return;
                            }

                            if(!getSpecificationLoader().loaded(variablesClassName)) {
                                validationFailures.add(
                                        "%s: error in JDOQL query, class name for VARIABLES clause not recognized (JDOQL : %s)",
                                        className, query);
                                return;
                            }
                            ObjectSpecification objectSpecification = getSpecificationLoader()
                                    .loadSpecification(variablesClassName);
                            JdoPersistenceCapableFacet persistenceCapableFacet = objectSpecification
                                    .getFacet(JdoPersistenceCapableFacet.class);

                            if(persistenceCapableFacet == null) {
                                validationFailures.add(
                                        "%s: error in JDOQL query, class name for VARIABLES clause is not annotated as @PersistenceCapable (JDOQL : %s)",
                                        className, query);
                                return;
                            }
                        }
                    });
            metaModelValidator.add(queryFromValidator);
        }
    }

    private final static Pattern fromPattern = Pattern.compile("SELECT.*?FROM[\\s]+([^\\s]+).*", Pattern.CASE_INSENSITIVE);

    static String from(final String query) {
        final Matcher matcher = fromPattern.matcher(query);
        return matcher.matches() ? matcher.group(1) :  null;
    }

    private final static Pattern variablesPattern = Pattern.compile(".*?VARIABLES[\\s]+([^\\s]+).*", Pattern.CASE_INSENSITIVE);
    static String variables(final String query) {
        final Matcher matcher = variablesPattern.matcher(query);
        return matcher.matches() ? matcher.group(1) :  null;
    }



}
