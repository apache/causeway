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
package org.apache.isis.applib.layout.bootstrap3;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;

/**
 * Contains a row of content, either on the top-level {@link BS3Page page} or at any other lower-level element that can
 * contain rows, eg {@link BS3Tab tabs}.
 *
 * <p>
 *     It is rendered as a &lt;div class=&quot;row ...&quot;&gt;
 * </p>
 */
@XmlType(
        name = "row"
        , propOrder = {
            "cols"
        }
)
public class BS3Row extends BS3ElementAbstract {

    private static final long serialVersionUID = 1L;

    private List<BS3RowContent> cols = new ArrayList<BS3RowContent>(){{
        add(new BS3Col());
    }};

    // no wrapper
    @XmlElementRefs({
            @XmlElementRef(type = BS3Col.class, name="col", required = true),
            @XmlElementRef(type = BS3ClearFixVisible.class,  name="clearFixVisible", required = false),
            @XmlElementRef(type = BS3ClearFixHidden.class,  name="clearFixHidden", required = false)
    })
    public List<BS3RowContent> getCols() {
        return cols;
    }

    public void setCols(final List<BS3RowContent> cols) {
        this.cols = cols;
    }

}
