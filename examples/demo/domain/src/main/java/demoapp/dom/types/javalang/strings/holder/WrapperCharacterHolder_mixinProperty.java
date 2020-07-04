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
package demoapp.dom.types.javalang.strings.holder;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

import demoapp.dom.types.primitive.chars.holder.PrimitiveCharHolder;


//tag::class[]
@Property()
@PropertyLayout(hidden = Where.ALL_TABLES)
@RequiredArgsConstructor
public class JavaLangStringHolder_mixinProperty {

    private final JavaLangStringHolder holder;

    @MemberOrder(name = "contributed", sequence = "1")
    public Character prop() {
        return holder.getReadOnlyProperty();
    }

}
//end::class[]
