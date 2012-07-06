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

package org.apache.isis.core.commons.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.isis.core.commons.exceptions.IsisException;

public final class ClassUtil {

    private ClassUtil() {
    }

    public static Object newInstance(final Class<?> type, final Class<?> constructorParamType, final Object constructorArg) {
        return ClassUtil.newInstance(type, new Class[] { constructorParamType }, new Object[] { constructorArg });
    }

    /**
     * Tries to instantiate using a constructor accepting the supplied
     * arguments; if no such constructor then falls back to trying the no-arg
     * constructor.
     */
    public static Object newInstance(final Class<?> type, final Class<?>[] constructorParamTypes, final Object[] constructorArgs) {
        try {
            Constructor<?> constructor;
            try {
                constructor = type.getConstructor(constructorParamTypes);
                return constructor.newInstance(constructorArgs);
            } catch (final NoSuchMethodException ex) {
                try {
                    constructor = type.getConstructor();
                    return constructor.newInstance();
                } catch (final NoSuchMethodException e) {
                    throw new IsisException(e);
                }
            }
        } catch (final SecurityException ex) {
            throw new IsisException(ex);
        } catch (final IllegalArgumentException e) {
            throw new IsisException(e);
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        } catch (final InvocationTargetException e) {
            throw new IsisException(e);
        }
    }

}
