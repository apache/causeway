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

package org.apache.isis.core.metamodel.facetdecorator.i18n;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;

/**
 * Authorises the user in the current session view and use members of an object.
 * 
 * <p>
 * TODO: allow description and help to be found for parameters
 */
public interface I18nManager extends ApplicationScopedComponent {

    /**
     * Get the localized name for the specified identified action/property.
     * 
     * <p>
     * Returns null if no name available.
     */
    String getName(Identifier identifier);

    /**
     * Get the localized description for the specified identified
     * action/property.
     * 
     * <p>
     * Returns null if no description available.
     */
    String getDescription(Identifier identifier);

    /**
     * Get the localized help text for the specified identified action/property.
     * 
     * <p>
     * Returns null if no help text available.
     */
    String getHelp(Identifier identifier);

    /**
     * Get the localized parameter names for the specified identified
     * action/property.
     * 
     * <p>
     * Returns null if no parameters are available. Otherwise returns an array
     * of String objects the size of the number of parameters, where each
     * element is the localised name for the corresponding parameter, or is null
     * if no parameter name is available.
     */
    List<String> getParameterNames(Identifier identifier);
}
