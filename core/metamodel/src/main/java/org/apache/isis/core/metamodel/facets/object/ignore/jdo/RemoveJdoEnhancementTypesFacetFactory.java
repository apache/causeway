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

package org.apache.isis.core.metamodel.facets.object.ignore.jdo;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveMethodsFacetFactory;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

/**
 * Removes all methods inherited from <tt>javax.jdo.spi.PersistenceCapable</tt> (if JDO is on the classpath).
 */
public class RemoveJdoEnhancementTypesFacetFactory extends FacetFactoryAbstract {

    private final List<RemoveMethodsFacetFactory.MethodAndParameterTypes> jdoEnhancementmethodsToIgnore = _Lists.newArrayList();

    public RemoveJdoEnhancementTypesFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);

        final String typeToIgnoreIfOnClasspath = "javax.jdo.spi.PersistenceCapable";
        try {
            Class<?> typeToIgnore = InstanceUtil.loadClass(typeToIgnoreIfOnClasspath);
            addMethodsToBeIgnored(typeToIgnore);
        } catch(Exception ex) {
            // ignore
        }
    }

    private void addMethodsToBeIgnored(Class<?> typeToIgnore) {
        final Method[] methods = typeToIgnore.getMethods();
        for (final Method method : methods) {
            jdoEnhancementmethodsToIgnore
            .add(new RemoveMethodsFacetFactory.MethodAndParameterTypes(method.getName(), method.getParameterTypes()));
        }
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        for (final RemoveMethodsFacetFactory.MethodAndParameterTypes mapt : jdoEnhancementmethodsToIgnore) {
            processClassContext.removeMethod(MethodScope.OBJECT, mapt.methodName, null, mapt.methodParameters);
        }
    }


}
