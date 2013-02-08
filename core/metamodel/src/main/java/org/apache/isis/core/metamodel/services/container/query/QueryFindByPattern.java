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

package org.apache.isis.core.metamodel.services.container.query;

import java.io.Serializable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryBuiltInAbstract;

/**
 * Although implements {@link Query} and thus is ought to be
 * {@link Serializable}, it will be converted into a <tt>PersistenceQuery</tt>
 * in the runtime for remoting purposes and so does not need to be (and indeed
 * isn't).
 * 
 * <p>
 * See discussion in {@link QueryBuiltInAbstract} for further details.
 */
public class QueryFindByPattern<T> extends QueryBuiltInAbstract<T> {

    private static final long serialVersionUID = 1L;

    private final T pattern;
    
    public QueryFindByPattern(final Class<T> type, final T pattern, final long ... range){
        super(type, range);
        this.pattern = pattern;
    }


    public T getPattern() {
        return pattern;
    }

    @Override
    public String getDescription() {
        return getResultTypeName() + " (matching pattern)";
    }

}
