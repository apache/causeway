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
package org.apache.isis.extensions.secman.api.feature.dom;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

import lombok.NoArgsConstructor;

@DomainObject(
        objectType = "isis.ext.secman.ApplicationTypeMember"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
        )
@NoArgsConstructor
public abstract class ApplicationTypeMember extends ApplicationFeatureViewModel {

    public static abstract class PropertyDomainEvent<S extends ApplicationTypeMember, T> extends ApplicationFeatureViewModel.PropertyDomainEvent<ApplicationTypeMember, T> {}


    // -- constructors
    public ApplicationTypeMember(final ApplicationFeatureId featureId) {
        super(featureId);
    }


    // -- memberName (properties)

    public static class MemberNameDomainEvent extends PropertyDomainEvent<ApplicationTypeMember, String> {}

    @Override
    @Property(
            domainEvent = MemberNameDomainEvent.class
            )
    @PropertyLayout(fieldSetId="Id", sequence = "2.4")
    public String getMemberName() {
        return super.getMemberName();
    }


}


