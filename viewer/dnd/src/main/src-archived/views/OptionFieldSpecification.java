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


package org.apache.isis.viewer.dnd.metal;

import org.apache.isis.object.ObjectAdapter;
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.basic.SimpleIdentifier;
import org.apache.isis.viewer.dnd.core.AbstractFieldSpecification;
import org.apache.isis.viewer.dnd.special.OpenOptionFieldBorder;

import javax.swing.text.html.Option;



public class OptionFieldSpecification extends AbstractFieldSpecification {
    public boolean canDisplay(ObjectAdapter object) {
        return object instanceof Option;
    }
    
    public View createView(Content content, ViewAxis axis) {
        return new SimpleIdentifier(new OptionSelectionFieldBorder(new OptionSelectionField(content, this, axis)));
    }

    public String getName() {
        return "Drop down list";
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

