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

package org.apache.isis.core.tck.dom.defaults;

import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;

public class WithDefaultsEntity extends AbstractDomainObject {

    // {{ Name (string, title)
    private String name;

    @MemberOrder(sequence = "1")
    @Title
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String defaultName() {
        return "default-name";
    }

    // }}

    // {{ Flag (boolean)
    private boolean flag;

    @MemberOrder(sequence = "1")
    public boolean getFlag() {
        return flag;
    }

    public void setFlag(final boolean flag) {
        this.flag = flag;
    }

    public boolean defaultFlag() {
        return true;
    }

    // }}

    // {{ AnInt (int)
    private int anInt;

    @MemberOrder(sequence = "1")
    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(final int anInt) {
        this.anInt = anInt;
    }

    public int defaultAnInt() {
        return 42;
    }

    // }}

    // {{ Other (property)
    private WithDefaultsEntity other;

    @MemberOrder(sequence = "1")
    public WithDefaultsEntity getOther() {
        return other;
    }

    public void setOtherEntity(final WithDefaultsEntity withDefaultsEntity) {
        this.other = withDefaultsEntity;
    }

    public WithDefaultsEntity defaultOther() {
        final List<WithDefaultsEntity> list = repository.list();
        return list.size() > 0 ? list.get(list.size() - 1) : null;
    }

    // }}

    // {{ injected: WithDefaultsEntityRepository
    private WithDefaultsEntityRepository repository;

    public void setWithDefaultsEntityRepository(final WithDefaultsEntityRepository repository) {
        this.repository = repository;
    }
    // }}

}
