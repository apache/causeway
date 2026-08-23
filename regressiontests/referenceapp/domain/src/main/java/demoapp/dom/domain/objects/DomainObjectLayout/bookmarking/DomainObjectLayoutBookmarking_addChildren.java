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
package demoapp.dom.domain.objects.DomainObjectLayout.bookmarking;

import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom.types.Samples;
import lombok.RequiredArgsConstructor;

@Action
@RequiredArgsConstructor
public class DomainObjectLayoutBookmarking_addChildren {

    private final DomainObjectLayoutBookmarkingEntity parent;

    @MemberSupport
    public DomainObjectLayoutBookmarkingEntity act(final int number) {
        var strings = samples.stream().collect(Collectors.toList());
        for (int i = 0; i < number; i++) {
            parent.addChild(parent.getName() + " - " + strings.get(i));
        }
        return parent;
    }
    @MemberSupport public int default0Act() {
        return 3;
    }

    @Inject Samples<String> samples;
}
