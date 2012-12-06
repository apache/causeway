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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class ViewRequirement {
    public static final int NONE = 0;
    public static final int CLOSED = 0x01;
    // public static final int SUMMARY = 0x02;
    public static final int OPEN = 0x04;

    public static final int EDITABLE = 0x10;

    public static final int FIXED = 0x100;
    public static final int EXPANDABLE = 0x200;

    public static final int ROOT = 0x1000;
    public static final int SUBVIEW = 0x2000;

    public static final int DESIGN = 0x10000;

    private final Content content;
    private final int status;

    public ViewRequirement(final Content content, int status) {
        Assert.assertNotNull(content);
        this.content = content;
        this.status = status;
        status = CLOSED;
    }

    public Content getContent() {
        return content;
    }

    public boolean is(final int status) {
        return (this.status & status) == status;
    }

    public boolean isClosed() {
        return is(CLOSED);
    }

    public boolean isOpen() {
        return is(OPEN);
    }

    public boolean isFixed() {
        return is(FIXED);
    }

    public boolean isExpandable() {
        return is(EXPANDABLE);
    }

    public boolean isSubview() {
        return is(SUBVIEW);
    }

    public boolean isEditable() {
        return is(EDITABLE);
    }

    @Deprecated
    public boolean isDesign() {
        return true;
    }

    public boolean isObject() {
        return content.isObject();
    }

    public boolean isCollection() {
        return content.isCollection();
    }

    public boolean isTextParseable() {
        return content.isTextParseable();
    }

    public boolean isFor(final Class<?> cls) {
        return cls.isAssignableFrom(content.getClass());
    }

    public boolean hasReference() {
        return content.getAdapter() != null;
    }

    public boolean isForValueType(final Class<? extends Facet> cls) {
        final ObjectSpecification specification = content.getSpecification();
        return specification != null && specification.containsFacet(cls);
    }

    public ObjectSpecification getSpecification() {
        return content.getAdapter().getSpecification();
    }

    public ObjectAdapter getAdapter() {
        return content.getAdapter();
    }

}
