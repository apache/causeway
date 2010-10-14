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


package org.apache.isis.extensions.dnd.view.composite;

import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.icon.Icon;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewFactory;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.base.IconGraphic;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.border.IconBorder;
import org.apache.isis.extensions.dnd.view.border.LineBorder;
import org.apache.isis.extensions.dnd.view.text.ObjectTitleText;
import org.apache.isis.runtime.context.IsisContext;

public class IconGridViewSpecification extends AbstractCollectionViewSpecification {

    public IconGridViewSpecification() {
        addViewDecorator(new IconBorder.Factory());
    }

        protected ViewFactory createElementFactory() {
            return new ViewFactory() {
                public View createView(Content content, Axes axes, int sequence) {
                    View icon = new ImageViewSpecification().createView(content, axes, sequence);
                    /*
                    Icon icon = new Icon(content, IconGridViewSpecification.this);
                    Text textStyle = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
                    icon.setTitle(new ObjectTitleText(icon, textStyle));
                    icon.setSelectedGraphic(new IconGraphic(icon, 68));
                    icon.setUnselectedGraphic(new IconGraphic(icon, 60));
                    icon.setVertical(true);
                    
                    // return icon;
                    */
                    LineBorder lineBorderedIcon = new LineBorder(icon);
                    lineBorderedIcon.setPadding(4);
                    lineBorderedIcon.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
                    
            //        return lineBorderedIcon;
                    
                    return new ReplaceViewBorder(lineBorderedIcon);
                }
            };
        }

    public Layout createLayout(Content content, Axes axes) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setSize(3);
        return gridLayout;
    }
    
    public String getName() {
        return "Icon Grid";
    }
    
    
    
    
    
    
    /*
    private static final ObjectSpecification BOOK_SPECIFICATION = IsisContext.getSpecificationLoader().loadSpecification("org.apache.isis.example.library.dom.Book");
    public boolean canDisplay(ViewRequirement requirement) {
        return super.canDisplay(requirement) && requirement.getAdapter().getTypeOfFacet().valueSpec() == BOOK_SPECIFICATION;
    }
    */
}


