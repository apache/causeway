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
package org.apache.isis.applib.layout.grid.bootstrap3;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * One of the <a href="http://getbootstrap.com/css/#responsive-utilities">Responsive utility classes</a>.
 *
 *
 * <p>
 *     It is rendered as a (eg) &lt;div class=&quot;clearfix visible-xs-block ...&quot;&gt;
 * </p>
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "clearFixVisible"
        )
@XmlType(
        name = "clearFixVisible"
        )
public class BS3ClearFixVisible extends BS3ClearFix {

    private static final long serialVersionUID = 1L;

    public enum CssDisplay {
        BLOCK,
        INLINE,
        INLINE_BLOCK;

        public String toCssClassFragment() {
            return name().toLowerCase().replace('_', '-');
        }
    }

    private CssDisplay cssDisplay;


    @XmlAttribute(required = true)
    public CssDisplay getCssDisplay() {
        return cssDisplay;
    }

    public void setCssDisplay(final CssDisplay cssDisplay) {
        this.cssDisplay = cssDisplay;
    }


    @Override
    public String toCssClass() {
        return "clearfix "
                + getSize().toCssDBlockClassFragment() 
                // + "-" + getCssDisplay().toCssClassFragment() 
                + (getCssClass() != null? " " + getCssClass(): "");
    }

}
