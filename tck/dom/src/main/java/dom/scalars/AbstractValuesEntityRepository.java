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

package dom.scalars;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;

public abstract class AbstractValuesEntityRepository<T> extends AbstractFactoryAndRepository {
    
    private Class<T> cls;

    public AbstractValuesEntityRepository(Class<T> cls) {
        super();
        this.cls = cls;
    }


    public List<T> list() {
        return allInstances(cls);
    }

    
    public T newEntity() {
        T entity = newTransientInstance(cls);
        persist(entity);
        return entity;
    }


}
