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
package org.apache.isis.applib.services.registry;

import java.util.Comparator;

import javax.annotation.Priority;

import org.springframework.context.annotation.Primary;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.val;

/**
 * A comparator that orders objects according to their {@link Priority} annotation.
 *
 * @since 2.0  {@index}
 */
public class InstanceByPriorityComparator implements Comparator<Object> {

    private static final InstanceByPriorityComparator INSTANCE =
            new InstanceByPriorityComparator();

    public static InstanceByPriorityComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(Object o1, Object o2) {

        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1; // o1 later (lower) priority than o2
            }
        }
        if (o2 == null) {
            return 1; // o1 earlier (higher) priority than o2
        }

        val primaryAnnot1 = _Reflect.getAnnotation(o1.getClass(), Primary.class);
        val primaryAnnot2 = _Reflect.getAnnotation(o2.getClass(), Primary.class);
        if(primaryAnnot1 != null && primaryAnnot2 == null) { return +1; }
        if(primaryAnnot1 == null && primaryAnnot2 != null) { return -1; }

        val prioAnnot1 = _Reflect.getAnnotation(o1.getClass(), Priority.class);
        val prioAnnot2 = _Reflect.getAnnotation(o2.getClass(), Priority.class);
        val prio1 = prioAnnot1 != null ? prioAnnot1.value() : PriorityPrecedence.LAST;
        val prio2 = prioAnnot2 != null ? prioAnnot2.value() : PriorityPrecedence.LAST;
        return Integer.compare(prio2, prio1);
    }

}
