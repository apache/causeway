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


package org.apache.isis.example.library.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.example.library.dom.Member;

public class MemberRepository extends AbstractFactoryAndRepository {

    
    public List<Member> findByName(@Named("Name") final String name) {
        Member memberPattern = newTransientInstance(Member.class);
        memberPattern.setName(name);
        memberPattern.setJoined(null);
        memberPattern.setCode(null);
        memberPattern.setStatus(null);
        memberPattern.setJunior(true);

        List<Member> allMatches = new ArrayList<Member>();
        List<Member> juniorMatches = allMatches(Member.class, memberPattern);
        allMatches.addAll(juniorMatches);
        memberPattern.setJunior(false);
        List<Member> seniorMatches = allMatches(Member.class, memberPattern);
        allMatches.addAll(seniorMatches);
        
        return allMatches;
        /*
        Member[] list = new Member[allMatches.size()];
        int i = 0;
        for (Member member : allMatches) {
            list[i++] = member;
        }
        return list;
        */
    }

    public Member[] listMembers() {
        List<Member> allInstances = allInstances();
        Member[] list = new Member[allInstances.size()];
        int i = 0;
        for (Member member : allInstances) {
            list[i++] = member;
        }
        return list;
    }
    
    @Exploration
    public List<Member> allInstances() {
        return allInstances(Member.class);
    }
    
    public String title() {
        return "Members";
    }
    
    public String getId() {
        return "members";
    }

    public Member newMember() {
       return newTransientInstance(Member.class);
    }
        
    public String iconName() {
        return "Member";
    }
}

