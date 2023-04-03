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

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.types.Samples;
import lombok.val;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;

@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT        // <.>
)
public abstract class DomainObjectLayoutBookmarking
        implements
        HasAsciiDocDescription,
        ValueHolder<String> {

    public String title() {
        return value();
    }

    @Override
    public String value() {
        return getName();
    }

    public abstract String getName();
    public abstract void setName(String value);

    public abstract void addChild(String value);

    @Action
    public DomainObjectLayoutBookmarking addChildren(int number) {
        val strings = samples.stream().collect(Collectors.toList());
        for (int i = 0; i < number; i++) {
            addChild(strings.get(i));
        }
        return this;
    }

    public abstract Set<? extends DomainObjectLayoutBookmarkingChild> getChildren();

    @Inject Samples<String> samples;
}
