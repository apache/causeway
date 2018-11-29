/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.guice;

import java.lang.annotation.Annotation;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * A domain service acting as a bridge between Isis services and Guice.
 */
public interface GuiceBeanProvider {

    /**
     * Looks up a Guice bean by class type
     *
     * @param beanType The class type of the Guice bean
     * @param <T> The type of the Guice bean
     * @return The resolved bean
     */
    @Programmatic
    <T> T lookup(Class<T> beanType);

    /**
     * Looks up a Guice bean by class type
     *
     * @param beanType The class type of the Guice bean
     * @param qualifier  An annotation identifying the bean instance
     * @param <T> The type of the Guice bean
     * @return The resolved bean
     */
    @Programmatic
    <T> T lookup(Class<T> beanType, final Annotation qualifier);
}
