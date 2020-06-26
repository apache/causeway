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
package demoapp.dom.types.primitive.doubles.holder;

import org.apache.isis.applib.annotation.MemberOrder;

import lombok.RequiredArgsConstructor;

import demoapp.dom.types.primitive.doubles.holder.PrimitiveDoubleHolder;


//@Property()
//@PropertyLayout(named = "<i>Named<i/> <b>not</b> escaped property", namedEscaped = false, describedAs = "named = \"(some markup)\", namedEscaped = false") // TODO: this doesn't render, and causes bootlint issues
@RequiredArgsConstructor
public class PrimitiveDoubleHolder_mixinPropertyNamedNotEscaped {

    private final PrimitiveDoubleHolder primitiveDoubleHolder;

    @MemberOrder(name = "contributed", sequence = "3")
    public double prop() {
        return primitiveDoubleHolder.getReadOnlyProperty();
    }


}
