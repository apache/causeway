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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Predicate;

/**
 * Represents a tab within a {@link BS3TabGroup tab group}.
 *
 * <p>
 *     They simply contain one or more {@link BS3Row row}s.
 * </p>
 */
@XmlType(
        name = "tab"
        , propOrder = {
            "name",
            "rows"
        }
)
public class BS3Tab extends BS3ElementAbstract implements BS3RowOwner {

    private static final long serialVersionUID = 1L;

    private String name;
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    // at least one row
    private List<BS3Row> rows = new ArrayList<BS3Row>(){{
        add(new BS3Row());
    }};

    // no wrapper
    @XmlElement(name = "row", required = true)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
    }



    private BS3TabOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3TabOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3TabOwner owner) {
        this.owner = owner;
    }


    public static class Predicates {
        public static Predicate<BS3Tab> notEmpty() {
            return new Predicate<BS3Tab>() {
                @Override
                public boolean apply(final BS3Tab bs3Tab) {
                    return !bs3Tab.getRows().isEmpty();
                }
            };
        }
    }

}
