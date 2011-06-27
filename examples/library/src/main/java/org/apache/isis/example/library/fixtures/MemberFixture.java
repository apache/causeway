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


package org.apache.isis.example.library.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.library.dom.Member;


public class MemberFixture extends AbstractFixture {

    public void install() {
        createMember("3992", "Harry Smith", null, "83 Car Place", true, null);
        createMember("6332", "Joan Smith", null, "23 Mill Lane", true, "37788292");
        createMember("3962", "J P Smith", "jsmith@home.net", "78 Thread Street", false, null);
        createMember("3962", "Harriet Jones", null, "102 Thread Street", true, null);
    }

    private void createMember(String code, String name, String email, String address, boolean junior, String phone) {
        Member member = newTransientInstance(Member.class);
        member.setCode(code);
        member.setName(name);
        member.setEmail(email);
        member.setAddress(address);
        member.setPhone(phone);
        member.setJunior(junior);
        persist(member);
    }
}

