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


package org.apache.isis.metamodel.facets;

import java.util.List;

import org.apache.isis.applib.annotation.ActionOrder;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Debug;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Executed;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.FieldOrder;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotInRepositoryMenu;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Plural;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.applib.annotation.When;


@Bounded
@ActionOrder("1, 2, 3")
@FieldOrder("4, 5, 6")
@Immutable(When.ONCE_PERSISTED)
@Named("singular name")
@Plural("plural name")
public class JavaObjectWithAnnotations {

    public void side(@Named("one") @Optional final String param) {}

    @Debug
    public void start() {}

    @Exploration
    public void top() {}

    @Prototype
    public void proto() {}

    @NotContributed
    public void notContributed() {}

    @NotInRepositoryMenu
    public void notInRepositoryMenu() {}

    @Hidden
    public void stop() {}

    @NotPersisted
    public int getOne() {
        return 1;
    }

    public void setOne(final int value) {}

    @Disabled
    public String getTwo() {
        return "";
    }

    @TypeOf(Long.class)
    @DescribedAs("description text")
    @Named("name text")
    public List getCollection() {
        return null;
    }

    @Executed(Executed.Where.LOCALLY)
    public void left() {}

    @Executed(Executed.Where.REMOTELY)
    public void right() {}

    @Disabled
    public void bottom() {}

    public void complete(final String notMultiline, @MultiLine(numberOfLines = 10) final String multiLine) {}

}

