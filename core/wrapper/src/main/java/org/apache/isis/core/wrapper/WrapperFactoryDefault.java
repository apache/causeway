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

package org.apache.isis.core.wrapper;


import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;

/**
 * This service provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implies by
 * the Isis programming model.
 *
 * <p>
 * This implementation has no UI-visible actions and is the supported implementation, so it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.   This means that by including
 * <tt>o.a.i.core:isis-core-wrapper</tt> on the classpath, the service is automatically registered; no further
 * configuration is required.
 */
@DomainService(nature = NatureOfService.DOMAIN)
public class WrapperFactoryDefault extends WrapperFactoryJavassist {

}
