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
package org.apache.causeway.applib.services.priming;

import java.util.Collection;

/**
 * Startup-only registration API for application-defined ORM cache primers.
 *
 * <p>
 * Applications contribute registrations from {@link PrimingRegistrar} rather
 * than retaining this registry or mutating it after metamodel initialization.
 * Registrations use exact metamodel types and do not apply by Java type
 * hierarchy matching.
 * </p>
 *
 * @since 2.2 {@index}
 */
public interface PrimingRegistry {

    <T> void action(
            Class<T> domainType,
            String actionLogicalName,
            ActionPrimer<? super T> primer);

    <T> void actions(
            Class<T> domainType,
            Collection<String> actionLogicalNames,
            ActionPrimer<? super T> primer);

    <T> void view(
            Class<T> domainType,
            ViewPrimer<? super T> primer);
}
