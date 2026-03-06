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

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.commons.internal._Java17Ex;
import org.springframework.util.Assert;
import org.springframework.util.SerializationUtils;

import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(_Java17Ex.class)
public class BSUtil {
    
    /**
     * Creates a deep copy of given original grid.
     */
    public BSGrid deepCopy(final BSGrid orig) {
		Object result = SerializationUtils.deserialize(SerializationUtils.serialize(orig));
		Assert.state(result != null, "Deserialized object must not be null");
        return (BSGrid)orig;
    }

    public BSGrid resolveOwners(final BSGrid grid) {
        new BSElementOwnerResolvingWalker(grid).walk();
        return grid;
    }
    
    /**
     * Useful for debugging or comparing grid instances (e.g. JUnit tests).
     */
    public String toYaml(final BSGrid grid) {
        class TinyWriter {
            StringBuilder sb = new StringBuilder();
            int indent = 0;
            void inc() { indent++; }
            void dec() { indent--; }
            void writeln(final String line) {
                sb.append("  ".repeat(indent)).append(line).append("\n");
            }
        }
        var w = new TinyWriter();

        grid.visit(new BSElementVisitor() {
            @Override public void enter(final BSGrid bsGrid) {
                w.writeln("bsGrid:");
                w.inc();
                w.writeln("class: %s".formatted(bsGrid.domainClass().getName()));
                w.writeln("layoutKey: %s".formatted(bsGrid.layoutKey()));
            }
            @Override public void exit(final BSGrid bsGrid) {
                w.dec();
            }
            @Override public void enter(final BSRow bsRow) {
                w.writeln("row:");
                w.inc();
            }
            @Override public void exit(final BSRow bsRow) {
                w.dec();
            }
            @Override public void enter(final BSCol bsCol) {
                w.writeln("col:");
                w.inc();
                w.writeln("span: %s".formatted(bsCol.getSpan()));
                if(bsCol.isUnreferencedActions()==Boolean.TRUE) {
                    w.writeln("unreferencedActions: true");
                }
                if(bsCol.isUnreferencedCollections()==Boolean.TRUE) {
                    w.writeln("unreferencedCollections: true");
                }
            }
            @Override public void exit(final BSCol bsCol) {
                w.dec();
            }
            @Override public void enter(final BSTabGroup bsTabGroup) {
                w.writeln("tabGroup:");
                w.inc();
            }
            @Override public void exit(final BSTabGroup bsTabGroup) {
                w.dec();
            }
            @Override public void enter(final BSTab bsTab) {
                w.writeln("tab:");
                w.inc();
                w.writeln("name: %s".formatted(bsTab.getName()));
            }
            @Override public void exit(final BSTab bsTab) {
                w.dec();
            }

            @Override public void visit(final BSClearFix bsClearFix) {}
            @Override public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                w.inc();
                w.writeln("domainObject:");
                w.inc();
                w.writeln("named: %s".formatted(domainObjectLayoutData.getNamed()));
                w.writeln("bookmarking: %s".formatted(domainObjectLayoutData.getBookmarking()));
                w.dec();
                w.dec();
            }
            @Override public void visit(final ActionLayoutData actionLayoutData) {
                w.inc();
                w.writeln("action:");
                w.inc();
                w.writeln("id: %s".formatted(actionLayoutData.getId()));
                w.writeln("named: %s".formatted(actionLayoutData.getNamed()));
                w.writeln("position: %s".formatted(actionLayoutData.getPosition()));
                w.writeln("promptStyle: %s".formatted(actionLayoutData.getPromptStyle()));
                w.writeln("hidden: %s".formatted(actionLayoutData.getHidden()));
                w.dec();
                w.dec();
            }
            @Override public void visit(final PropertyLayoutData propertyLayoutData) {
                w.inc();
                w.writeln("property:");
                w.inc();
                w.writeln("id: %s".formatted(propertyLayoutData.getId()));
                w.writeln("named: %s".formatted(propertyLayoutData.getNamed()));
                w.writeln("hidden: %s".formatted(propertyLayoutData.getHidden()));
                w.dec();
                w.dec();
            }
            @Override public void visit(final CollectionLayoutData collectionLayoutData) {
                w.inc();
                w.writeln("collection:");
                w.inc();
                w.writeln("id: %s".formatted(collectionLayoutData.getId()));
                w.writeln("named: %s".formatted(collectionLayoutData.getNamed()));
                w.writeln("hidden: %s".formatted(collectionLayoutData.getHidden()));
                w.dec();
                w.dec();
            }
            @Override public void visit(final FieldSet fieldSet) {
                w.writeln("fieldSet:");
                w.inc();
                w.writeln("id: %s".formatted(fieldSet.getId()));
                w.writeln("name: %s".formatted(fieldSet.getName()));
                if(fieldSet.isUnreferencedActions()==Boolean.TRUE) {
                    w.writeln("unreferencedActions: true");
                }
                if(fieldSet.isUnreferencedProperties()==Boolean.TRUE) {
                    w.writeln("unreferencedProperties: true");
                }
                w.dec();
            }
        });
        return w.sb.toString();
    }

    // -- REMOVERS

    /** removes the tab from its owner and returns the owner */
    public Optional<BSTabOwner> remove(final BSTab tab) {
        var ownerOpt = Optional.ofNullable(tab.getOwner());
        ownerOpt.ifPresent(owner->owner.getTabs().remove(tab));
        tab.setOwner(null);
        return ownerOpt;
    }
    /** removes the col from its owner and returns the owner */
    public Optional<BSRowContentOwner> remove(final BSCol col) {
        var ownerOpt = Optional.ofNullable(col.getOwner());
        ownerOpt.ifPresent(owner->{ 
        	if(owner instanceof BSRow) {
        		((BSRow)owner).getRowContents().remove(col);
        	}
        });
        col.setOwner(null);
        return ownerOpt;
    }
    /** removes the tabGroup from its owner and returns the owner */
    public Optional<BSTabGroupOwner> remove(final BSTabGroup tabGroup) {
        var ownerOpt = Optional.ofNullable(tabGroup.getOwner());
        ownerOpt.ifPresent(owner->owner.getTabGroups().remove(tabGroup));
        tabGroup.setOwner(null);
        return ownerOpt;
    }
    /** removes the row from its owner and returns the owner */
    public Optional<BSRowOwner> remove(final BSRow row) {
        var ownerOpt = Optional.ofNullable(row.getOwner());
        ownerOpt.ifPresent(owner->owner.getRows().remove(row));
        row.setOwner(null);
        return ownerOpt;
    }

    /** removes the action layout from its owner and returns the owner */
    public Optional<ActionLayoutDataOwner> remove(final ActionLayoutData actionLayoutData) {
        var ownerOpt = Optional.ofNullable(actionLayoutData.getOwner());
        ownerOpt.ifPresent(owner->owner.getActions().remove(actionLayoutData));
        actionLayoutData.setOwner(null);
        return ownerOpt;
    }
    /** removes the property layout from its owner and returns the owner */
    public Optional<FieldSet> remove(final PropertyLayoutData propertyLayoutData) {
        var ownerOpt = Optional.ofNullable(propertyLayoutData.getOwner());
        ownerOpt.ifPresent(owner->owner.getProperties().remove(propertyLayoutData));
        propertyLayoutData.setOwner(null);
        return ownerOpt;
    }
    /** removes the collection layout from its owner and returns the owner */
    public Optional<CollectionLayoutDataOwner> remove(final CollectionLayoutData collectionLayoutData) {
        var ownerOpt = Optional.ofNullable(collectionLayoutData.getOwner());
        ownerOpt.ifPresent(owner->owner.getCollections().remove(collectionLayoutData));
        collectionLayoutData.setOwner(null);
        return ownerOpt;
    }

    /** replaces the domain object layout with an empty one and returns the owner */
    public Optional<DomainObjectLayoutDataOwner> replaceWithEmpty(final DomainObjectLayoutData domainObjectLayoutData) {
        var ownerOpt = Optional.ofNullable(domainObjectLayoutData.getOwner());
        ownerOpt.ifPresent(owner->owner.setDomainObject(new DomainObjectLayoutData()));
        domainObjectLayoutData.setOwner(null);
        return ownerOpt;
    }

}
