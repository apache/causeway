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
package org.apache.isis.testing.unittestsupport.applib.dom.bidir;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class ParentDomainObject {

    // {{ Children (Collection)
    @Persistent(mappedBy="parent")
    private SortedSet<ChildDomainObject> children = new TreeSet<ChildDomainObject>();

    public SortedSet<ChildDomainObject> getChildren() {
        return children;
    }

    public void setChildren(final SortedSet<ChildDomainObject> children) {
        this.children = children;
    }

    public void addToChildren(final ChildDomainObject child) {
        // check for no-op
        if (child == null || getChildren().contains(child)) {
            return;
        }
        // dissociate arg from its current parent (if any).
        child.clearParent();
        // associate arg
        child.setParent(this);
        getChildren().add(child);
    }

    public void removeFromChildren(final ChildDomainObject child) {
        // check for no-op
        if (child == null || !getChildren().contains(child)) {
            return;
        }
        // dissociate arg
        child.setParent(null);
        getChildren().remove(child);
    }

    // }}


}
