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

package org.apache.isis.viewer.wicket.model.models;

import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

import org.apache.isis.core.commons.lang.IterableExtensions;
import org.apache.isis.core.commons.matchers.IsisMatchers;

public class LowestCommonSuperclassClosureTest {

    static class Animal {}
    static class Mineral {}
    static class Vegetable {}
    static class Mammal extends Animal {}
    static class Lion extends Mammal {}
    
    @Test
    public void nothingInCommon() throws Exception {
        assertLowestCommonOfListIs(listOf(Animal.class, Mineral.class, Vegetable.class), Object.class);
    }

    @Test
    public void superclassInCommon() throws Exception {
        assertLowestCommonOfListIs(listOf(Animal.class, Mammal.class), Animal.class);
    }
    
    @Test
    public void subclassInCommon() throws Exception {
        assertLowestCommonOfListIs(listOf(Lion.class, Lion.class), Lion.class);
    }
    
    private static void assertLowestCommonOfListIs(List<Class<? extends Object>> list, Class<?> expected) {
        EntityCollectionModel.LowestCommonSuperclassClosure closure = new EntityCollectionModel.LowestCommonSuperclassClosure();
        IterableExtensions.fold(list, closure);
        assertThat(closure.getLowestCommonSuperclass(), IsisMatchers.classEqualTo(expected));
    }

    private static List<Class<? extends Object>> listOf(Class<?>... classes) {
        return Lists.newArrayList(classes);
    }


}
