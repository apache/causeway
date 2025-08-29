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
package org.apache.causeway.testdomain.model.good;

import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

abstract class ProperFullyAbstract {

    @ObjectSupport public abstract String title();
    @ObjectSupport public abstract ObjectSupport.IconResource icon(ObjectSupport.IconWhere iconWhere);
    @ObjectSupport public abstract String cssClass();
    @ObjectSupport public abstract String layout();

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    public abstract void sampleAction();

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    public abstract String getSampleProperty();
    public abstract void setSampleProperty(String sampleProperty);

    @Collection
    @CollectionLayout(named = "foo", describedAs = "bar")
    public abstract List<String> getSampleCollection();
    public abstract void setSampleCollection(List<String> sampleCollection);

}
