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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.apache.isis.metamodel.commons.matchers.IsisMatchers;

import static org.junit.Assert.assertThat;

public class LowestCommonSuperclassClosureTest {

    static class Animal {}
    static class Mineral {}
    static class Vegetable {}
    static class Mammal extends Animal {}
    static class Lion extends Mammal {}

    @Test
    public void nothingInCommon() {
        assertLowestCommonOfListIs(Arrays.asList(new Animal(), new Mineral(), new Vegetable()), Object.class);
    }

    @Test
    public void superclassInCommon() {
        assertLowestCommonOfListIs(Arrays.asList(new Animal(), new Mammal()), Animal.class);
    }

    @Test
    public void subclassInCommon() {
        assertLowestCommonOfListIs(Arrays.asList(new Lion(), new Lion()), Lion.class);
    }

    private static void assertLowestCommonOfListIs(List<Object> list, Class<?> expected) {
        Util.LowestCommonSuperclassFinder finder = 
                new Util.LowestCommonSuperclassFinder();
        list.forEach(finder::collect);
        assertThat(finder.getLowestCommonSuperclass().get(), IsisMatchers.classEqualTo(expected));
    }


}
