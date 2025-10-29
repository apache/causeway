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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.io.Serializable;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;

/**
 * @since 1.x {@index}
 */
public interface BSElement extends Serializable {

    /**
     * Any additional CSS classes to render on the page element corresponding to this object,
     * eg as per the <a href="http://getbootstrap.com/css/#grid-less">Bootstrap mixins</a> or just for
     * custom styling.
     */
    String getCssClass();
    void setCssClass(final String cssClass);

    public interface BSElementVisitor {
        default void enter(final BSGrid bsGrid) {}
        default void exit(final BSGrid bsGrid) {}
        default void enter(final BSRow bsRow) {}
        default void exit(final BSRow bsRow) {}
        default void enter(final BSCol bsCol) {}
        default void exit(final BSCol bsCol) {}
        default void visit(final BSClearFix bsClearFix) {}
        default void enter(final BSTabGroup bsTabGroup) {}
        default void exit(final BSTabGroup bsTabGroup) {}
        default void enter(final BSTab bsTab) {}
        default void exit(final BSTab bsTab) {}

        default void visit(final DomainObjectLayoutData domainObjectLayoutData) {}
        default void visit(final ActionLayoutData actionLayoutData) {}
        default void visit(final PropertyLayoutData propertyLayoutData) {}
        default void visit(final CollectionLayoutData collectionLayoutData) {}
        default void visit(final FieldSet fieldSet) {}
    }

}
