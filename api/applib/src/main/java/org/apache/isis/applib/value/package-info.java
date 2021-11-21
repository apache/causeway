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
 * This package defines an additional set of
 * {@link org.apache.isis.applib.annotation.Value} types, supported in addition
 * to the usual JDK ones (of {@link java.lang.String}, {@link java.lang.Integer}, {@link java.math.BigDecimal}, {@link java.util.Date}
 * etc and the primitives).
 *
 * <p>
 * Each of these value types has a corresponding implementation of
 * {@link org.apache.isis.applib.value.semantics.ValueSemanticsProvider} (implemented
 * within the <tt>core.progmodel</tt> module).
 */
package org.apache.isis.applib.value;