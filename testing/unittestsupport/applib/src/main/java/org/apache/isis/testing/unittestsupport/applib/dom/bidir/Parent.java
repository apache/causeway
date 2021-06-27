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
package org.apache.isis.testing.unittestsupport.applib.dom.bidir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

class Parent {
    Class<?> entityType;
    Field childField;
    String mappedBy;
    Method getMethod;

    // for 1:m
    Method addToMethod;
    Method removeFromMethod;

    // if 1:1
    Method modifyMethod;
    Method clearMethod;

    Collection<?> getChildren(Object parent) throws RuntimeException {
        try {
            return (Collection<?>) getMethod.invoke(parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void addToChildren(Object parent, Object child) {
        try {
            addToMethod.invoke(parent, child);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void removeFromChildren(Object parent, Object child) {
        try {
            removeFromMethod.invoke(parent, child);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    Object getChild(Object parent) {
        try {
            return getMethod.invoke(parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void modifyChild(Object parent, Object child) {
        try {
            modifyMethod.invoke(parent, child);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void clearChild(Object parent) {
        try {
            clearMethod.invoke(parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String desc() {
        return entityType.getName()+"#"+childField.getName();
    }
    String descRel(Child c) {
        final boolean oneToMany = addToMethod != null;
        return entityType.getSimpleName() + "#" + childField.getName() + " 1:" + (oneToMany?"m":"1") + " "+ c.entityType.getSimpleName() + "#" + c.parentField.getName();
    }

    Object newParent(Instantiators instantiators) {
        return instantiators.newInstance(entityType);
    }
}