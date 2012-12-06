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

package org.apache.isis.core.metamodel.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.core.commons.exceptions.IsisException;

public final class ServiceUtil {

    private ServiceUtil() {
    }

    public static String id(final Object object) {
        final Class<?> cls = object.getClass();
        try {
            final Method m = cls.getMethod("getId", new Class[0]);
            return (String) m.invoke(object, new Object[0]);
        } catch (final SecurityException e) {
            throw new IsisException(e);
        } catch (final NoSuchMethodException e) {
            final String id = object.getClass().getName();
            return id.substring(id.lastIndexOf('.') + 1);
        } catch (final IllegalArgumentException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        } catch (final InvocationTargetException e) {
            throw new IsisException(e);
        }
    }
}
