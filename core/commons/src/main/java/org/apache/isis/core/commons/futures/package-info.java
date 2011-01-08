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
 * Defines an {@link org.apache.isis.core.commons.futures.FutureResultFactory API} to generate results of
 * any given type as a future.
 * 
 * <p>
 * Also incorporates an {@link org.apache.isis.core.commons.futures.FutureFactory SPI} by which
 * a bytecode manipulation library (eg cglib or javassist) can synthesize the future
 * as a proxy that will delegate to the provided {@link org.apache.isis.core.commons.futures.FutureResultFactory} to
 * actually generate the result.
 * 
 * <p>
 * Note: at the time of writing this mini-framework is unused.  It was originally
 * developed for JUnit viewer, though subsequent refactorings have meant that
 * it is not required at the moment.  Nevertheless, we have chosen to keep this
 * code base rather than remove it.
 */
package org.apache.isis.core.commons.futures;