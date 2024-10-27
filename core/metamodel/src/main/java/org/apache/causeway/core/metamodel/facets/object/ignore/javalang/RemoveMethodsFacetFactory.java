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
package org.apache.causeway.core.metamodel.facets.object.ignore.javalang;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Designed to simply filter out any synthetic methods.
 *
 * <p>
 * Does not add any {@link Facet}s.
 */
public class RemoveMethodsFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private static final String JAVA_CLASS_PREFIX = "java.";

    @Inject
    public RemoveMethodsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        super.process(processClassContext);

        var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();
        var isConcreteMixin = facetHolder instanceof ObjectSpecification
                ? ((ObjectSpecification)facetHolder).getBeanSort().isMixin()
                : false;

        var isActionAnnotationRequired = processClassContext.getIntrospectionPolicy()
                .getMemberAnnotationPolicy().isMemberAnnotationsRequired();

        getClassCache()
            .streamPublicMethods(cls)
            .forEach(method->{
                // remove methods in the context of non-static inner classes,
                // except cls when is a mixin
                if (!isConcreteMixin
                        && _Reflect.isNonStaticInnerMethod(method.method())) {
                    processClassContext.removeMethod(method);
                    return;
                }

                // removeJavaLangComparable(processClassContext);
                if(method.name().equals("compareTo")) {
                    processClassContext.removeMethod(method);
                    return;
                }

                // remove property setter, if has not explicitly an @Action annotation
                // this code block is not required, if @Action annotations are explicit per config
                if(!isActionAnnotationRequired
                        && method.isSingleArg()
                        && method.name().startsWith("set")
                        && method.name().length() > 3) {

                    if(!_Annotations.synthesize(method.method(), Action.class).isPresent()) {
                        processClassContext.removeMethod(method);
                        return;
                    }
                }
            });

        removeSuperclassMethods(processClassContext.getCls(), processClassContext);

        // no need to remove java.lang.Object methods, as this is already taken care of by the ClassCache (tested)

        // removeInitMethod(processClassContext);
        processClassContext.removeMethod("init", void.class, _Constants.emptyClasses);
    }

    private void removeSuperclassMethods(final Class<?> type, final ProcessClassContext processClassContext) {
        if (type == null) {
            return;
        }

        if (!_Reflect.isJavaApiClass(type)) {
            removeSuperclassMethods(type.getSuperclass(), processClassContext);
            return;
        }

        getClassCache()
            .streamPublicMethods(type)
            .forEach(processClassContext::removeMethod);
    }

}
