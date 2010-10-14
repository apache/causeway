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


package org.apache.isis.extensions.hibernate.objectstore.persistence.objectfactory;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.collection.PersistentCollection;
import org.apache.isis.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactoryAbstract;


public class HibernateObjectFactory extends ObjectFactoryAbstract {

    @Override
    public <T> T doInstantiate(Class<T> cls) throws ObjectInstantiationException {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        try {
            return cls.newInstance();
        } catch (IllegalAccessException e) {
            throw new ObjectInstantiationException(e);
        } catch (InstantiationException e) {
            throw new ObjectInstantiationException(e);
        }
    }

    /**
     * Converts Hibernate's {@link PersistentCollection} classes to the correct collection type ( {@link List},
     * {@link Set} or {@link Map} ).
     * 
     * <p>
     * TODO: this approach prevents programmers from subclassing collection classes. We will therefore need to
     * revisit this area when we get around to adding actions to collection classes.
     */
    public Class<?> getClass(final Class<?> cls) {
        if (PersistentCollection.class.isAssignableFrom(cls)) {
            if (List.class.isAssignableFrom(cls)) {
                return List.class;
            }
            if (Set.class.isAssignableFrom(cls)) {
                return Set.class;
            }
            if (Map.class.isAssignableFrom(cls)) {
                return Map.class;
            }
        }
        // if (HibernateProxy.class.isAssignableFrom(cls)) {
        // return cls.getSuperclass();
        // }
        return cls;
    }

}
