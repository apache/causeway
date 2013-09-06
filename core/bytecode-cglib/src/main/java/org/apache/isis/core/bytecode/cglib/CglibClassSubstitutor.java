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

package org.apache.isis.core.bytecode.cglib;

import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.CglibEnhanced;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutorAbstract;

public class CglibClassSubstitutor extends ClassSubstitutorAbstract {

    public CglibClassSubstitutor() {
    }

    /**
     * If {@link CglibEnhanced} then return superclass, else as per
     * {@link ClassSubstitutorAbstract#getClass(Class) superclass'}
     * implementation.
     */
    @Override
    public Class<?> getClass(final Class<?> cls) {
        if (ClassUtil.directlyImplements(cls, CglibEnhanced.class)) {
            return getClass(cls.getSuperclass());
        }
        return super.getClass(cls);
    }

}
