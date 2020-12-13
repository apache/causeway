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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class has additional facets, and specifies the how to
 * obtain the <tt>FacetFactory</tt> to manufacture them.
 *
 * <p>
 * At least one named factory (as per {@link #facetFactoryNames()}) or one class
 * factory (as per {@link #facetFactoryClasses()}) should be specified.
 * @since 1.x {@index}
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Facets {

    /**
     * Array of fully qualified names of classes each implementing
     * <tt>org.apache.isis.core.metamodel.facets.FacetFactory</tt>.
     *
     * <p>
     * Either the array provided by this method or by
     * {@link #facetFactoryClasses()} should be non-empty.
     */
    String[] facetFactoryNames() default {};

    /**
     * Array of {@link Class}s, each indicating a class implementing
     * <tt>org.apache.isis.core.metamodel.facets.FacetFactory</tt>.
     *
     * <p>
     * Either the array provided by this method or by
     * {@link #facetFactoryNames()} should be non-empty.
     */
    Class<?>[] facetFactoryClasses() default {};

}
