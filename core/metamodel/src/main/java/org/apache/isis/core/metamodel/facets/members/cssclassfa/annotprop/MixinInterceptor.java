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

package org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.core.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.val;

/**
 * To solve <a href="https://issues.apache.org/jira/browse/ISIS-1743">ISIS-1743</a>.<br/>
 * Could be better integrated into Isis' meta-model.
 *
 */
class MixinInterceptor {

    /**
     * If method originates from a mixin then we infer the intended name
     * from the mixin's class name.
     *
     * @param method within the mixin type itself.
     * @return the intended name of the method
     */
    static String intendedNameOf(Method method) {

        val declaringClass = method.getDeclaringClass();
        val mixinIfAny = _Annotations.findNearestAnnotation(declaringClass, Mixin.class);

        if(mixinIfAny.isPresent()) {
            val methodName = method.getName();
            val mixinAnnotMethodName = mixinIfAny.get().method();
            if(mixinAnnotMethodName.equals(methodName)) {
                val mixinMethodName = 
                        ObjectMemberAbstract.deriveMemberNameFrom(method.getDeclaringClass().getName());
                if(mixinMethodName!=null) {
                    return mixinMethodName;
                }
            }
        }
        // default behavior
        return method.getName();
    }

}
