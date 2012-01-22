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

package org.apache.isis.tck.objstore.dflt.defaults;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.tck.dom.defaults.WithDefaultsEntity;
import org.apache.isis.tck.dom.defaults.WithDefaultsEntityRepository;

public class WithDefaultsEntityRepositoryDefault extends AbstractFactoryAndRepository implements WithDefaultsEntityRepository {

    @Override
    public String getId() {
        return "withDefaultsEntities";
    }

    @Override
    @QueryOnly
    public List<WithDefaultsEntity> list() {
        final Filter<Object> filterPersistentOnly = new Filter<Object>() {

            @Override
            public boolean accept(final Object t) {
                return getContainer().isPersistent(t);
            }
        };
        return allMatches(WithDefaultsEntity.class, filterPersistentOnly);
    }

    @Override
    public WithDefaultsEntity newTransientEntity() {
        return newTransientInstance(WithDefaultsEntity.class);
    }

}
