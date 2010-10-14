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


package org.apache.isis.extensions.hibernate.objectstore.tools.internal;

import java.util.StringTokenizer;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.runtimecontext.spec.ObjectSpecificationNoMember;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.hibernate.objectstore.HibernateConstants;
import org.apache.isis.runtime.context.IsisContext;


public class MappingHelper {
    private static final String LIST_SEPARATOR = ", ";
    private static final String NAKEDOBJECTS_CLASSES_LIST = HibernateConstants.PROPERTY_PREFIX + "classes";
    private static final String NAKEDOBJECTS_CLASSES_PREFIX = HibernateConstants.PROPERTY_PREFIX + "classes.prefix";

    public static void loadRequiredClasses() {
        final IsisConfiguration configuration = IsisContext.getConfiguration();
        final SpecificationLoader loader = IsisContext.getSpecificationLoader();
        String classPrefix = configuration.getString(NAKEDOBJECTS_CLASSES_PREFIX);
        classPrefix = classPrefix == null ? "" : classPrefix.trim();
        if (classPrefix.length() > 0 && !classPrefix.endsWith(".")) {
            classPrefix = classPrefix + ".";
        }
        final String classList = configuration.getString(NAKEDOBJECTS_CLASSES_LIST);
        if (classList != null) {
            final StringTokenizer classes = new StringTokenizer(classList, LIST_SEPARATOR);
            while (classes.hasMoreTokens()) {
                final ObjectSpecification specification = loader.loadSpecification(classPrefix + classes.nextToken().trim());
                if (specification instanceof ObjectSpecificationNoMember) {
                    throw new IsisException("No such class " + specification.getFullName());
                }
            }
        }
    }
}
