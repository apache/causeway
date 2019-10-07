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

package org.apache.isis.metamodel.specloader;

import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;

public final class ReflectorConstants {

    /**
     * Key used to lookup implementation of {@link ProgrammingModel} in
     * {@link IsisConfigurationLegacy}.
     *
     * @see #FACET_FACTORY_INCLUDE_CLASS_NAME_LIST
     * @see #FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST
     */
    public static final String PROGRAMMING_MODEL_FACETS_CLASS_NAME = ConfigurationConstants.ROOT + "reflector.facets";
    public static final String PROGRAMMING_MODEL_FACETS_CLASS_NAME_DEFAULT = ProgrammingModelFacetsJava8.class.getName();

    /**
     * Key used to lookup comma-separated list of {@link FacetFactory}s to
     * include.
     *
     * @see #FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST
     */
    public static final String FACET_FACTORY_INCLUDE_CLASS_NAME_LIST = ConfigurationConstants.ROOT + "reflector.facets.include";

    /**
     * Key used to lookup comma-separated list of {@link FacetFactory}s to
     * exclude.
     *
     * @see #FACET_FACTORY_INCLUDE_CLASS_NAME_LIST
     */
    public static final String FACET_FACTORY_EXCLUDE_CLASS_NAME_LIST = ConfigurationConstants.ROOT + "reflector.facets.exclude";

    private ReflectorConstants() {
    }

}
