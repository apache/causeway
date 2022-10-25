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
package org.apache.causeway.core.metamodel.facets.object.navparent.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.Evaluators;
import org.apache.causeway.core.metamodel.facets.Evaluators.MethodEvaluator;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.navparent.method.NavigableParentFacetViaMethod;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * For detailed behavioral specification see
 * <a href="https://issues.apache.org/jira/browse/CAUSEWAY-1816">CAUSEWAY-1816</a>.
 *
 * @since 2.0
 *
 */
@Log4j2
public class NavigableParentAnnotationFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public NavigableParentAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // Starting from the current domain-object class, we search down the object
        // inheritance hierarchy (super class, super super class, ...), until we find
        // the first class that has a @PropertyLayout(navigable=Navigable.PARENT) annotation.
        // That's the one we use to
        // resolve the current domain-object's navigable parent.

        final Optional<Evaluators.Evaluator> evaluators =
                Evaluators.streamEvaluators(cls,
                        NavigableParentAnnotationFacetFactory::isNavigableParentFlagSet,
                        TypeHierarchyPolicy.INCLUDE,
                        InterfacePolicy.EXCLUDE)
                .findFirst();

        if (evaluators.isEmpty()) {
            return; // no parent resolvable
        }

        final Evaluators.Evaluator parentEvaluator = evaluators.get();

        final Method method;

        // find method that provides the parent ...
        if(parentEvaluator instanceof Evaluators.MethodEvaluator) {
            // we have a 'parent' annotated method
            method = ((Evaluators.MethodEvaluator) parentEvaluator).getMethod();
        } else if(parentEvaluator instanceof Evaluators.FieldEvaluator) {
            // we have a 'parent' annotated field (useful if one uses lombok's @Getter on a field)
            method = ((Evaluators.FieldEvaluator) parentEvaluator).getCorrespondingGetter().orElse(null);
            if(method==null)
                return; // code should not be reached, since case should be handled by meta-data validation

        } else {
            return; // no parent resolvable
        }

        addFacetIfPresent(
                NavigableParentFacetViaMethod.create(cls, method, facetHolder));
    }

    private static boolean isNavigableParentFlagSet(final AnnotatedElement annotatedElement){
        return _Annotations
                .synthesize(annotatedElement, PropertyLayout.class)
                .map(propertyLayout->propertyLayout.navigable().isParent())
                .orElse(false);
    }

    /**
     * For detailed behavior see
     * <a href="https://issues.apache.org/jira/browse/CAUSEWAY-1816">CAUSEWAY-1816</a>.
     */
    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(spec->{

            val cls = spec.getCorrespondingClass();

            if(!spec.lookupFacet(NavigableParentFacet.class).isPresent()) {
                return; // skip check
            }

            val evaluators =
                    Evaluators.streamEvaluators(cls,
                            NavigableParentAnnotationFacetFactory::isNavigableParentFlagSet,
                            TypeHierarchyPolicy.EXCLUDE,
                            InterfacePolicy.INCLUDE)
                    .collect(Can.toCan())
                    // guard against inherited method having identical synthesized annotations as bas method,
                    // while not actually overriding
                    .distinct((a, b)->{
                        if(!Objects.equals(a.getClass(), b.getClass())) {
                            return false; // different
                        }
                        if(a instanceof MethodEvaluator) {
                            val ma = (MethodEvaluator) a;
                            val mb = (MethodEvaluator) b;
                            return _Reflect.methodsSame(ma.getMethod(), mb.getMethod());
                        }
                        return true; // equal
                    });;

            if (evaluators.isEmpty()) {
                return; // no conflict, continue validation processing
            }

            if (evaluators.isCardinalityMultiple()) {

                val conflictingEvaluatorNames = evaluators.map(Evaluators.Evaluator::name).toSet();

                ValidationFailure.raiseFormatted(
                        spec,
                        "%s: conflict for determining a strategy for retrieval of (navigable) parent for class, "
                                + "contains multiple annotations '@%s' having navigable=PARENT, "
                                + "while at most one is allowed.\n\tConflicting members: %s",
                                spec.getFeatureIdentifier().getClassName(),
                                PropertyLayout.class.getName(),
                                conflictingEvaluatorNames.toString()
                                );

                return; // continue validation processing
            }

            final Evaluators.Evaluator parentEvaluator = evaluators.getSingletonOrFail();

            if(parentEvaluator instanceof Evaluators.FieldEvaluator) {
                // we have a @Parent annotated field (useful if one uses lombok's @Getter on a field)

                final Evaluators.FieldEvaluator fieldEvaluator =
                        (Evaluators.FieldEvaluator) parentEvaluator;

                if(!fieldEvaluator.getCorrespondingGetter().isPresent()) {

                    ValidationFailure.raiseFormatted(
                            spec,
                            "%s: unable to determine a strategy for retrieval of (navigable) parent for class, "
                                    + "field '%s' annotated with '@%s' having navigable=PARENT does not provide a getter.",
                                    spec.getFeatureIdentifier().getClassName(),
                                    fieldEvaluator.getField().getName(),
                                    PropertyLayout.class.getName());
                }

            }

        });

    }


}
