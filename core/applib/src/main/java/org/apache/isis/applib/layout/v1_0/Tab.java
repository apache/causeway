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
package org.apache.isis.applib.layout.v1_0;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(
        propOrder = {
                "name"
                , "left"
                , "middle"
                , "right"
        }
)
public class Tab {

    private String name;

    @XmlElement(required = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private Column left = new Column();

    @XmlElement(required = true)
    public Column getLeft() {
        return left;
    }

    public void setLeft(final Column left) {
        this.left = left;
    }


    private Column middle;

    @XmlElement(required = false)
    public Column getMiddle() {
        return middle;
    }

    public void setMiddle(final Column middle) {
        this.middle = middle;
    }


    private Column right;

    @XmlElement(required = false)
    public Column getRight() {
        return right;
    }

    public void setRight(final Column right) {
        this.right = right;
    }
}
