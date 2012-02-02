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

package org.apache.isis.core.metamodel.adapter.oid.stringable.directly;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;

public class OidStringifierDirect implements OidStringifier {

    private final Class<? extends Oid> oidClass;
    private final Method deStringMethod;

    public OidStringifierDirect(final Class<? extends Oid> oidClass) {
        Ensure.ensureThatArg(oidClass, is(not(nullValue(Class.class))));

        this.oidClass = oidClass;
        try {
            deStringMethod = oidClass.getMethod("deString", String.class);
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

    @Override
    public String enString(final Oid oid) {
        if (!(oid instanceof DirectlyStringableOid)) {
            throw new IllegalArgumentException("Must be DirectlyStringableOid; oid class: " + oid.getClass().getName());
        }
        final DirectlyStringableOid directlyStringableOid = (DirectlyStringableOid) oid;
        return directlyStringableOid.enString();
    }

    @Override
    public Oid deString(final String oidStr) {
        try {
            return (Oid) deStringMethod.invoke(null, oidStr);
        } catch (final IllegalAccessException ex) {
            throw new IllegalArgumentException("deString(String) method failed; ", ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalArgumentException("deString(String) method failed; ", ex);
        }
    }

    public Class<? extends Oid> getOidClass() {
        return oidClass;
    }

    public Method getDestringMethod() {
        return deStringMethod;
    }

}
