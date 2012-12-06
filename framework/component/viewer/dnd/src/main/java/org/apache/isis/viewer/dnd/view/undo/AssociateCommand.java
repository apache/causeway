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

package org.apache.isis.viewer.dnd.view.undo;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.dnd.view.Command;

public class AssociateCommand implements Command {
    private final String description;
    private final OneToOneAssociation field;
    private final ObjectAdapter object;
    private final ObjectAdapter associatedObject;
    private final String name;

    public AssociateCommand(final ObjectAdapter object, final ObjectAdapter associatedObject, final OneToOneAssociation field) {
        this.description = "Clear association of " + associatedObject.titleString();
        this.name = "associate " + associatedObject.titleString();
        this.object = object;
        this.associatedObject = associatedObject;
        this.field = field;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void undo() {
        field.clearAssociation(object);
    }

    @Override
    public void execute() {
        field.setAssociation(object, associatedObject);
    }
}
