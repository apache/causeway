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
import java.text.MessageFormat;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryBuiltInAbstract;

/**
 * Although implements {@link Query} and thus is intended to be (and indeed is)
 * {@link Serializable}, it will be converted into a <tt>PersistenceQuery</tt>
 * in the runtime for remoting purposes.
 * 
 * <p>
 * See discussion in {@link QueryBuiltInAbstract} for further details.
 */
public class QueryFindByTitle<T> extends QueryBuiltInAbstract<T> {

    private static final long serialVersionUID = 1L;

    private final String title;

    public QueryFindByTitle(final Class<T> type, final String title, final long ... range) {
        super(type, range);
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format("{0} (matching title: '{1}')", getResultTypeName(), getTitle());
    }

}
