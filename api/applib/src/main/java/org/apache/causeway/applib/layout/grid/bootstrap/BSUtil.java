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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.resources._Serializables;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BSUtil {

    /**
     * Not required when
     * org.apache.causeway.core.metamodel.facets.object.grid.BSGridTransformer.EmptyTabRemover
     * has already run.
     *
     * However, might still be useful to handle 'dynamically' visible tabs,
     * based on feature visibility.
     *
     * @implNote at the time of writing, I'm not sure whether presence of a layout
     * feature entry guarantees presence of a 'real' feature from the meta-model.
     */
    public boolean hasContent(final BSTab thisBsTab) {
        final AtomicBoolean foundContent = new AtomicBoolean(false);
        new BSWalker(thisBsTab).walk(new BSElementVisitor() {
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

    /**
     * Creates a deep copy of given original grid.
     */
    public BSGrid deepCopy(final BSGrid orig) {
        var bytes = _Serializables.write(orig);
        return _Casts.uncheckedCast(
                _Serializables.read(BSGrid.class, bytes));
    }

    public BSGrid resolveOwners(final BSGrid grid) {
        new BSElementOwnerResolvingWalker(grid).walk();
        return grid;
    }

    // -- REMOVERS

    /** removes the tab from its owner and returns the owner */
    public Optional<BSTabOwner> remove(final BSTab tab) {
        var ownerOpt = Optional.ofNullable(tab.owner());
        ownerOpt.ifPresent(owner->owner.getTabs().remove(tab));
        tab.owner(null);
        return ownerOpt;
    }
    /** removes the col from its owner and returns the owner */
    public Optional<BSRowContentOwner> remove(final BSCol col) {
        var ownerOpt = Optional.ofNullable(col.owner());
        ownerOpt.ifPresent(owner->owner.getRowContents().remove(col));
        col.owner(null);
        return ownerOpt;
    }
    /** removes the tabGroup from its owner and returns the owner */
    public Optional<BSTabGroupOwner> remove(final BSTabGroup tabGroup) {
        var ownerOpt = Optional.ofNullable(tabGroup.owner());
        ownerOpt.ifPresent(owner->owner.getTabGroups().remove(tabGroup));
        tabGroup.owner(null);
        return ownerOpt;
    }
    /** removes the row from its owner and returns the owner */
    public Optional<BSRowOwner> remove(final BSRow row) {
        var ownerOpt = Optional.ofNullable(row.owner());
        ownerOpt.ifPresent(owner->owner.getRows().remove(row));
        row.owner(null);
        return ownerOpt;
    }

    /** removes the action layout from its owner and returns the owner */
    public Optional<ActionLayoutDataOwner> remove(final ActionLayoutData actionLayoutData) {
        var ownerOpt = Optional.ofNullable(actionLayoutData.owner());
        ownerOpt.ifPresent(owner->owner.getActions().remove(actionLayoutData));
        actionLayoutData.owner(null);
        return ownerOpt;
    }
    /** removes the property layout from its owner and returns the owner */
    public Optional<FieldSet> remove(final PropertyLayoutData propertyLayoutData) {
        var ownerOpt = Optional.ofNullable(propertyLayoutData.owner());
        ownerOpt.ifPresent(owner->owner.getProperties().remove(propertyLayoutData));
        propertyLayoutData.owner(null);
        return ownerOpt;
    }
    /** removes the collection layout from its owner and returns the owner */
    public Optional<CollectionLayoutDataOwner> remove(final CollectionLayoutData collectionLayoutData) {
        var ownerOpt = Optional.ofNullable(collectionLayoutData.owner());
        ownerOpt.ifPresent(owner->owner.getCollections().remove(collectionLayoutData));
        collectionLayoutData.owner(null);
        return ownerOpt;
    }

    /** replaces the domain object layout with an empty one and returns the owner */
    public Optional<DomainObjectLayoutDataOwner> replaceWithEmpty(final DomainObjectLayoutData domainObjectLayoutData) {
        var ownerOpt = Optional.ofNullable(domainObjectLayoutData.owner());
        ownerOpt.ifPresent(owner->owner.setDomainObject(new DomainObjectLayoutData()));
        domainObjectLayoutData.owner(null);
        return ownerOpt;
    }

}
