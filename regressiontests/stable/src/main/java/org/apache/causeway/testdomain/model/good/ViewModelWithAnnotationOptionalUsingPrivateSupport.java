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

import java.io.Serializable;
import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Title;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * Subject to testing whether all support methods are picked up
 * when private, even though policy states ANNOTATION_OPTIONAL.
 *
 */
@DomainObject(
        nature = Nature.VIEW_MODEL,
        introspection = Introspection.ANNOTATION_OPTIONAL)
public class ViewModelWithAnnotationOptionalUsingPrivateSupport
implements Serializable {

    private static final long serialVersionUID = 1L;

    // allowed to be private since 2.0.0-M7
    @Title
    private String title() {
        return "view-model with annotation optional using private support";
    }

    // -- PUBLIC ACTION WITH PRIVATE SUPPORT

    @Action
    public String myAction() {
        return "Hallo World!";
    }

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disableMyAction() {
        return "action disabled for testing purposes";
    }

    // -- PROPERTY WITH PUBLIC GETTER AND SETTER, PRIVATE SUPPORT

    @Property
    @Getter @Setter
    private String propWithPrivateAccessors = "Foo";

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disablePropWithPrivateAccessors() {
        return "property disabled for testing purposes";
    }

    // -- COLLECTION WITH PUBLIC GETTER AND SETTER, PRIVATE SUPPORT

    @Collection
    @Getter @Setter
    private List<String> collWithPrivateAccessors = List.of("Foo");

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disableCollWithPrivateAccessors() {
        return "collection disabled for testing purposes";
    }

    // -- PRIVATE OBJECT SUPPORT

    // allowed to be private since 2.0.0-M7
    @ObjectSupport
    private String disabled() {
        return "object disabled for testing purposes";
    }

    // allowed to be private since 2.0.0-M7
    @ObjectSupport
    private boolean hidden() {
        return false;
    }

    // -- PRIVATE OBJECT LAYOUT SUPPORT


}
