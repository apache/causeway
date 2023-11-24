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
package org.apache.isis.applib.spec;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslatableString;

/**
 * Optional extension to the base {@link Specification}, to allow for i18n.
 *
 * <p>
 *     If implemented, then the {@link #satisfies(Object)} inherited from {@link Specification} can just return <tt>null</tt>;
 *     it will never be called by the framework.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface Specification2 extends Specification {

    /**
     * If <tt>null</tt> then satisfied, otherwise is the reason (as a {@link TranslatableString} translatable string) as to why the specification is not satisfied.
     */
    @Programmatic
    TranslatableString satisfiesTranslatable(Object obj);

}
