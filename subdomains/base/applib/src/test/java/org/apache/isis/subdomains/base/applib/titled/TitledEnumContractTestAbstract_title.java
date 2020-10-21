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
package org.apache.isis.subdomains.base.applib.titled;

import org.junit.Test;

import org.apache.isis.subdomains.base.applib.TitledEnum;

import lombok.val;

public abstract class TitledEnumContractTestAbstract_title {
    protected final Iterable<Class<? extends TitledEnum>> candidates;

    /**
     * @apiNote Usage example:<br>
     * {@code import org.reflections.Reflections;}<br>
     * {@code val reflections = new Reflections(packagePrefix);}<br>
     * {@code val candidates = reflections.getSubTypesOf(TitledEnum.class);}
     */
    public TitledEnumContractTestAbstract_title(final Iterable<Class<? extends TitledEnum>> candidates) {
        this.candidates = candidates;
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void searchAndTest() {
        for (Class<? extends TitledEnum> subtype : candidates) {
            if(!Enum.class.isAssignableFrom(subtype)) {
                continue; // ignore non-enums
            }
            val enumType = (Class<? extends Enum<?>>) subtype;
            new TitledEnumContractTester(enumType).test();
        }
    }
}
