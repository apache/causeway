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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * One of the <a href="http://getbootstrap.com/css/#responsive-utilities">Responsive utility classes</a>.
 *
 *
 * <p>
 *     It is rendered as a (eg) &lt;div class=&quot;clearfix hidden-xs ...&quot;&gt;
 * </p>
 */
@XmlRootElement(
        name = "clearFixHidden"
        )
@XmlType(
        name = "clearFixHidden"
        )
public class BS3ClearFixHidden extends BS3ClearFix {

    private static final long serialVersionUID = 1L;

    @Override
    public String toCssClass() {
        return "clearfix "
                + "hidden-" + getSize().toCssClassFragment()  +
                (getCssClass() != null? " " + getCssClass(): "");
    }

}
