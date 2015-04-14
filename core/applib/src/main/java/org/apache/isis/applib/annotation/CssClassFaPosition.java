/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.applib.annotation;


/**
 * Represents the location in the user interface where a class member is to be rendered.
 *
 * <p>
 * Used to control visibility (eg using the {@link Hidden} annotation) and enablement (eg using the {@link Disabled}
 * annotation) in different regions of the user interface.
 *
 * <p>
 * The application programmer may use any of the values of this enum. Some represent concrete locations (eg
 * {@link #OBJECT_FORMS}, {@link #PARENTED_TABLES}), whereas some represent a combination of locations (eg
 * {@link #ALL_TABLES}, {@link #ANYWHERE}).
 *
 * <h4>Framework Implementation Notes</h4>
 * <p>
 * This enum is also used internally within the framework. When rendering an element, the framework developer should
 * only use those values that represent concrete locations.
 */
public enum CssClassFaPosition {
    LEFT, RIGHT
}
