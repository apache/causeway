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

package org.apache.isis.core.runtime.persistence.query;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.persistence.PersistenceQuery;

public interface PersistenceQueryBuiltIn extends PersistenceQuery {

    /**
     * The built-in queries iterate over all instances.
     * 
     * <p>
     * This is similar to the {@link Filter} interface in the applib, except the
     * filtering is done within the object store as opposed to be the
     * {@link DomainObjectContainer}.
     * 
     * <p>
     * Object store implementations do not necessarily need to rely on this
     * method. For example, an RDBMS-based implementation may use an alternative
     * mechanism to determine the matching results, for example using a
     * <tt>WHERE</tt> clause in some SQL query.
     * 
     */
    public boolean matches(final ObjectAdapter object);
}
