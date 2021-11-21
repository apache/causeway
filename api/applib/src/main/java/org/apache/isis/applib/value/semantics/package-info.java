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

/**
 * The classes in this package are used for implementing custom value types.
 *
 * <p>
 *     <b>NOTE THAT</b> in addition to implementing the interfaces here, it is also necessary to provide appropriate
 *   components for Wicket viewer to edit the values and to implement the appropriate SPI/extension points so that
 *   DataNucleus can persist the objects to the database.
 * </p>
 *
 *
 * <p>
 * The {@link org.apache.isis.applib.value.semantics.ValueSemanticsProvider} interface
 * allows the framework to recognize its corresponding type as being a value
 * type (that is, having value semantics).  The {@link org.apache.isis.applib.value.semantics.ValueSemanticsAbstract}
 * class is an base adapter for this interface.
 *
 * <p>
 * The association between {@link org.apache.isis.applib.value.semantics.ValueSemanticsProvider}
 * and its corresponding type can be done in several ways.  Most straightforward
 * is to annotate the class with the {@link org.apache.isis.applib.annotation.Value}
 * annotation.  However, if the value type source code cannot be modified (for
 * example, if it is a third-party type such as joda-time), then the association
 * can be made using configuration properties.
 *
 * @deprecated
 */
package org.apache.isis.applib.value.semantics;