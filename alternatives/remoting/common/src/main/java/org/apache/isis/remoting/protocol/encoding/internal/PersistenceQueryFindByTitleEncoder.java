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


package org.apache.isis.remoting.protocol.encoding.internal;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.data.query.PersistenceQueryFindByTitleData;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindByTitle;

public class PersistenceQueryFindByTitleEncoder extends PersistenceQueryEncoderAbstract {

    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindByTitle.class;
    }

    public PersistenceQueryData encode(
    		final PersistenceQuery persistenceQuery) {
        PersistenceQueryFindByTitle queryByTitle = downcast(persistenceQuery);
		return new PersistenceQueryFindByTitleData(
        		persistenceQuery.getSpecification(), 
        		queryByTitle.getTitle());
    }

    @Override
    protected PersistenceQuery doDecode(
            final ObjectSpecification specification,
            final PersistenceQueryData persistenceQueryData) {
        final String title = (downcast(persistenceQueryData)).getTitle();
        return new PersistenceQueryFindByTitle(specification, title);
    }

	private PersistenceQueryFindByTitle downcast(
			final PersistenceQuery persistenceQuery) {
		return (PersistenceQueryFindByTitle) persistenceQuery;
	}

	private PersistenceQueryFindByTitleData downcast(
			final PersistenceQueryData persistenceQueryData) {
		return (PersistenceQueryFindByTitleData) persistenceQueryData;
	}

}

