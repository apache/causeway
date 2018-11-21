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

package org.apache.isis.core.metamodel.facets.object.navparent.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.navparent.method.NavigableParentFacetMethod;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * For detailed behavioral specification see
 * <a href="https://issues.apache.org/jira/browse/ISIS-1816">ISIS-1816</a>.
 *
 * @since 2.0.0
 *
 */
public class NavigableParentAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public NavigableParentAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
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

        final List<Annotations.Evaluator<PropertyLayout>> evaluators =
                Annotations.firstEvaluatorsInHierarchyHaving(cls, PropertyLayout.class,
                        NavigableParentAnnotationFacetFactory::isNavigableParentFlagSet);

        if (_NullSafe.isEmpty(evaluators)) {
            return; // no parent resolvable
        } else if (evaluators.size()>1) {
            // code should not be reached, since case should be handled by meta-data validation
            throw new RuntimeException("unable to determine navigable parent due to ambiguity");
        }

        final Annotations.Evaluator<PropertyLayout> parentEvaluator = evaluators.get(0);

        final Method method;

        // find method that provides the parent ...
        if(parentEvaluator instanceof Annotations.MethodEvaluator) {
            // we have a @Parent annotated method
            method = ((Annotations.MethodEvaluator<PropertyLayout>) parentEvaluator).getMethod();
        } else if(parentEvaluator instanceof Annotations.FieldEvaluator) {
            // we have a @Parent annotated field (useful if one uses lombok's @Getter on a field)
            method = ((Annotations.FieldEvaluator<PropertyLayout>) parentEvaluator).getGetter(cls).orElse(null);
            if(method==null)
                return; // code should not be reached, since case should be handled by meta-data validation

        } else {
            return; // no parent resolvable
        }

        try {
            FacetUtil.addFacet(new NavigableParentFacetMethod(method, facetHolder));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean isNavigableParentFlagSet(Annotations.Evaluator<PropertyLayout> evaluator){
        return evaluator.getAnnotation().navigable().isParent();
    }


    /**
     * For detailed behavioral specification see
     * <a href="https://issues.apache.org/jira/browse/ISIS-1816">ISIS-1816</a>.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                final Class<?> cls = objectSpec.getCorrespondingClass();

                final List<Annotations.Evaluator<PropertyLayout>> evaluators =
                        Annotations.firstEvaluatorsInHierarchyHaving(cls, PropertyLayout.class,
                                NavigableParentAnnotationFacetFactory::isNavigableParentFlagSet);

                if (_NullSafe.isEmpty(evaluators)) {
                    return true; // no conflict, continue validation processing
                } else if (evaluators.size()>1) {

                    validationFailures.add(
                            "%s: conflict for determining a strategy for retrieval of (navigable) parent for class, "
                                    + "contains multiple annotations '@%s' having navigable=PARENT, while at most one is allowed.",
                                    objectSpec.getIdentifier().getClassName(),
                                    PropertyLayout.class.getName());

                    return true; // continue validation processing
                }

                final Annotations.Evaluator<PropertyLayout> parentEvaluator = evaluators.get(0);

                if(parentEvaluator instanceof Annotations.FieldEvaluator) {
                    // we have a @Parent annotated field (useful if one uses lombok's @Getter on a field)

                    final Annotations.FieldEvaluator<PropertyLayout> fieldEvaluator =
                            (Annotations.FieldEvaluator<PropertyLayout>) parentEvaluator;

                    if(!fieldEvaluator.getGetter(cls).isPresent()) {

                        validationFailures.add(
                                "%s: unable to determine a strategy for retrieval of (navigable) parent for class, "
                                        + "field '%s' annotated with '@%s' having navigable=PARENT does not provide a getter.",
                                        objectSpec.getIdentifier().getClassName(),
                                        fieldEvaluator.getField().getName(),
                                        PropertyLayout.class.getName());
                    }

                }


                return true; //continue validation processing

            }

        }));
    }

    // -- ADAPTER INJECTION

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        adapterManager = servicesInjector.getPersistenceSessionServiceInternal();
    }

    PersistenceSessionServiceInternal adapterManager;

}
