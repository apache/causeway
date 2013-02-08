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

package org.apache.isis.applib.query;

import java.io.Serializable;

import org.apache.isis.applib.DomainObjectContainer;

/**
 * Although (through this class) the subclasses implements {@link Query} and
 * thus are meant to be {@link Serializable}, this isn't actually required of
 * the built-in queries because they are all converted into corresponding
 * <tt>PersistenceQuery</tt> in the runtime for remoting purposes.
 * 
 * <p>
 * The principle reason for this is to reduce the size of the API from the
 * {@link DomainObjectContainer} to <tt>RuntimeContext</tt>. It also means that the
 * requirements for writing an object store are more easily expressed: support
 * the three built-in queries, plus any others.
 * 
 * <p>
 * Note also that the {@link QueryFindByPattern} isn't actually serializable
 * (because it references an arbitrary pojo).
 * 
 * <p>
 * REVIEW: now that we've dropped remoting, could we get rid of the <tt>PersistenceQuery</tt>
 * classes and just use these classes throughout?
 */
public abstract class QueryBuiltInAbstract<T> extends QueryAbstract<T> {

    private static final long serialVersionUID = 1L;
    
    public QueryBuiltInAbstract(final Class<T> type, final long ... range) {
        super(type, range);
    }

    public QueryBuiltInAbstract(final String typeName, final long ... range) {
        super(typeName, range);
    }
    
}
