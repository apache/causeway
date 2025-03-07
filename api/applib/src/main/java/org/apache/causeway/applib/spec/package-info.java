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
 * This package defines a {@link org.apache.causeway.applib.spec.Specification}
 * interface (and various implementations), used by the
 * {@link org.apache.causeway.applib.annotation.MustSatisfy} annotation to
 * declare valid values for properties or parameters.
 *
 * <p>
 * This is an implementation of the DDD &quot;Specification&quot;, allowing
 * validation that might otherwise be repeated for both properties and
 * parameters (in the <tt>validateXxx()</tt> methods to be factored out.
 *
 * <p>
 * That said, there is still some repetition in that the {@link org.apache.causeway.applib.annotation.MustSatisfy}
 * annotation must be applied in all appropriate cases.  If it is the case that
 * the validation rules would apply <i>every</i> case, then it is generally
 * preferable to implement a {@link org.apache.causeway.applib.annotation.Value} type
 * through the {@link org.apache.causeway.applib.value.semantics.ValueSemanticsProvider}
 * interface.
 */
package org.apache.causeway.applib.spec;