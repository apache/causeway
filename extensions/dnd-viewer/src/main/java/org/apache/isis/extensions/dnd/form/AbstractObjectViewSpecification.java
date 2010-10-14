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


package org.apache.isis.extensions.dnd.form;

import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ViewFactory;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewSpecification;
import org.apache.isis.extensions.dnd.view.composite.ObjectFieldBuilder;
import org.apache.isis.extensions.dnd.view.composite.StackLayout;
import org.apache.isis.extensions.dnd.view.composite.StandardFields;


public abstract class AbstractObjectViewSpecification extends CompositeViewSpecification {

    public AbstractObjectViewSpecification() {
        builder = new ObjectFieldBuilder(createFieldFactory());
        init();
    }
        
    protected void init() {}

    protected ViewFactory createFieldFactory() {
        return new StandardFields();
    }

    public Layout createLayout(Content content, Axes axes) {
        return new StackLayout();
    }

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isObject() && !requirement.isTextParseable() && requirement.hasReference() && requirement.isOpen();
    }
}

