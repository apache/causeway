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

package org.apache.isis.applib;

/**
 * @deprecated - annotate with DomainService#repositoryFor()} instead.
 */
@Deprecated
public abstract class AbstractFactoryAndRepository extends AbstractService {

    /**
     * @see DomainObjectContainer#newPersistentInstance(Class)
     * @deprecated - see {@link DomainObjectContainer#newPersistentInstance(Class)} for rationale.
     */
    @Deprecated
    protected <T> T newPersistentInstance(final Class<T> ofClass) {
        return getContainer().newPersistentInstance(ofClass);
    }

    /**
     * @see DomainObjectContainer#newInstance(Class, Object)
     *
     * @deprecated - this method supports a rare use case, causing unnecessary interface bloat for very little gain.
     */
    @Deprecated
    protected <T> T newInstance(final Class<T> ofClass, final Object object) {
        return getContainer().newInstance(ofClass, object);
    }

}
