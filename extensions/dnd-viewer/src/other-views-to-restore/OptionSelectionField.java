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


package org.apache.isis.viewer.dnd.value;

import org.apache.isis.object.InvalidEntryException;
import org.apache.isis.object.[[NAME]];
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.ObjectContent;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.basic.SimpleIdentifier;
import org.apache.isis.viewer.dnd.core.AbstractFieldSpecification;
import org.apache.isis.viewer.dnd.special.OpenOptionFieldBorder;

import javax.swing.text.html.Option;

public class OptionSelectionField extends TextField {

    private String selected;

    public static class Specification extends AbstractFieldSpecification {
        public boolean canDisplay([[NAME]]) {
            return object.getObject() instanceof Option;
        }
        
        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new OptionSelectionFieldBorder(new OptionSelectionField(content, this, axis)));
        }

        public String getName() {
            return "Drop down list";
        }
    }

    public OptionSelectionField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis, false);
    }
    
    Option getOption() {
        ObjectContent content = ((ObjectContent) getContent());
        Option value = (Option) content.getObject().getObject();

        return value;
    }
    
    void set(String selected) {
        this.selected = selected;
        initiateSave();
    }
    
    protected void save() {
        try {
            parseEntry(selected);
        } catch (InvalidEntryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class OptionSelectionFieldBorder extends OpenOptionFieldBorder {

    public OptionSelectionFieldBorder(OptionSelectionField wrappedView) {
        super(wrappedView);
    }

    protected View createOverlay() {
            return new OptionSelectionFieldOverlay((OptionSelectionField) wrappedView);
    }
    
    public void firstClick(Click click) {
        if (canChangeValue()) {
            super.firstClick(click);
        }
    }

}

/*
 * [[NAME]] - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 [[NAME]] Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via isis.apache.org (the registered address
 * of [[NAME]] Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
