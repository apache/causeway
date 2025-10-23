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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BSUtil {

    public boolean hasContent(final BSTab thisBsTab) {
        final AtomicBoolean foundContent = new AtomicBoolean(false);
        new BSWalker(thisBsTab).visit(new BSElement.Visitor() {
            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                foundContent.set(true);
            }
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                foundContent.set(true);
            }
            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                foundContent.set(true);
            }
            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                foundContent.set(true);
            }
        });
        return foundContent.get();
    }


}
