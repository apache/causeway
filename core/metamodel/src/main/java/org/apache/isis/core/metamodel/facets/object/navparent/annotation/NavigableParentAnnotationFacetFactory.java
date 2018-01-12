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

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Parent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.reflection.Reflect;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.object.navparent.method.NavigableParentFacetMethod;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * 
 * @author ahuber@apache.org
 * @since 2.0.0
 *
 */
public class NavigableParentAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private static final String NAVIGABLE_PARENT_METHOD_NAME = "parent";


    public NavigableParentAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final List<Annotations.Evaluator<Parent>> evaluators = Annotations.getEvaluators(cls, Parent.class);
        if (evaluators.isEmpty()) {
            return;
        } else if (evaluators.size()>1) {
        	throw new RuntimeException("unable to determine navigable parent due to ambiguity");
        }
        
        final Annotations.Evaluator<Parent> parentEvaluator = evaluators.get(0);
        
        final Method method;

        // find method that provides the parent ...
        if(parentEvaluator instanceof Annotations.MethodEvaluator) {
        	// we have a @Parent annotated method
        	method = ((Annotations.MethodEvaluator<Parent>) parentEvaluator).getMethod();
        } else if(parentEvaluator instanceof Annotations.FieldEvaluator) {
        	// we have a @Parent annotated field (occurs if one uses lombok's @Getter on a field)
        	final Field field = ((Annotations.FieldEvaluator<Parent>) parentEvaluator).getField();
        	try {
				method = Reflect.getGetter(cls, field.getName());
			} catch (IntrospectionException e) {
				return;
			}
        } else {
        	return;
        }
        
        try {
			FacetUtil.addFacet(new NavigableParentFacetMethod(method, facetHolder));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }


    /**
     * Violation if there is a class that has both a <tt>parent()</tt> method and also 
     * any non-inherited method annotated with <tt>@Parent</tt>.
     * <p>
     * If there are only inherited methods annotated with <tt>@Parent</tt> then this is 
     * <i>not</i> a violation; but the imperative <tt>parent()</tt> method will take precedence.
     * </p>
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

        	//TODO [ahuber] code is a copy of the TitleAnnotationFacetFactory, not sure ...
        	// 1) what the wanted behavior should be (what about annotations in interfaces, ambiguity, etc.)
        	// 2) what this code fragment does
        	
            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                final Class<?> cls = objectSpec.getCorrespondingClass();

                final Method parentMethod =
                		MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, NAVIGABLE_PARENT_METHOD_NAME, Object.class, null);
                if (parentMethod == null) {
                    return true; // no conflict
                }
                
                // determine if cls contains a @Parent annotated method, not inherited from superclass
                final Class<?> supClass = cls.getSuperclass();
                if (supClass == null) {
                    return true; // no conflict
                }
                
                final List<Method> methods = methodsWithParentAnnotation(cls);
                final List<Method> superClassMethods = methodsWithParentAnnotation(supClass);
                if (methods.size() > superClassMethods.size()) {
                    validationFailures.add(
                            "%s: conflict for determining a strategy for retrieval of (navigable) parent for class, "
                            + "contains a method '%s' and an annotation '@%s'",
                            objectSpec.getIdentifier().getClassName(),
                            NAVIGABLE_PARENT_METHOD_NAME,
                            Parent.class.getName());
                }

                return true;
            }

            private List<Method> methodsWithParentAnnotation(final Class<?> cls) {
                return MethodFinderUtils.findMethodsWithAnnotation(cls, MethodScope.OBJECT, Parent.class);
            }

        }));
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        adapterManager = servicesInjector.getPersistenceSessionServiceInternal();
    }

    PersistenceSessionServiceInternal adapterManager;

}
