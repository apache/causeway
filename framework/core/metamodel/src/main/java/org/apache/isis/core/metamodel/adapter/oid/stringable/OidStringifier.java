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

package org.apache.isis.core.metamodel.adapter.oid.stringable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;

public class OidStringifier {

    private final Class<? extends Oid> oidClass;
    private final Method deStringMethod;

    public OidStringifier(final Class<? extends Oid> oidClass) {
        Ensure.ensureThatArg(oidClass, is(not(nullValue(Class.class))));

        this.oidClass = oidClass;
        try {
            deStringMethod = oidClass.getMethod("deString", String.class);
            final Class<?> returnType = deStringMethod.getReturnType();
            if(!RootOid.class.isAssignableFrom(returnType)) {
                throw new IllegalArgumentException("deString(String) method must return RootOid, from class '" + oidClass.getName() + "'");
            }
        } catch (final SecurityException ex) {
            throw new IllegalArgumentException("Trying to obtain 'deString(String)' method from  Oid class '" + oidClass.getName() + "'", ex);
        } catch (final NoSuchMethodException ex) {
            throw new IllegalArgumentException("Trying to obtain 'deString(String)' method from  Oid class '" + oidClass.getName() + "'", ex);
        }
        if (!JavaClassUtils.isStatic(deStringMethod)) {
            throw new IllegalArgumentException("'deString(String)' method for Oid class '" + oidClass.getName() + "' must be static");
        }
        if (!JavaClassUtils.isPublic(deStringMethod)) {
            throw new IllegalArgumentException("'deString(String)' method for Oid class '" + oidClass.getName() + "' must be public");
        }
    }

    public String enString(final RootOid rootOid) {
        return rootOid.enString();
    }

    public RootOid deString(final String urlEncodedOidStr) {
        // we do need to URL decode here, though.
        final String oidStr = UrlEncodingUtils.urlDecode(urlEncodedOidStr);
        try {
            return (RootOid) deStringMethod.invoke(null, oidStr);
        } catch (final IllegalAccessException ex) {
            throw new IllegalArgumentException("deString(String) method failed; ", ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalArgumentException("deString(String) method failed; ", ex);
        }
    }

    Class<? extends Oid> getOidClass() {
        return oidClass;
    }

    Method getDestringMethod() {
        return deStringMethod;
    }

}
