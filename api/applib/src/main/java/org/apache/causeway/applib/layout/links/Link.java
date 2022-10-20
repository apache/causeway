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
package org.apache.causeway.applib.layout.links;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @since 1.x {@index}
 */
@XmlType(
        name = "link"
        , propOrder = {
                "rel"
                , "method"
                , "href"
                , "type"
        }
        )
@XmlAccessorType(XmlAccessType.FIELD)
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    public Link() {
    }

    public Link(
            final String rel,
            final String method,
            final String href,
            final String type) {
        this.rel = rel;
        this.method = method;
        this.href = href;
        this.type = type;
    }

    @XmlElement(required = true)
    private String rel;

    @XmlElement(required = true)
    private String method;

    @XmlElement(required = true)
    private String href;

    @XmlElement(required = true)
    private String type;

    public String getRel() {
        return rel;
    }

    public String getMethod() {
        return method;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "LinkData{" +
                "rel='" + rel + '\'' +
                ", method='" + method + '\'' +
                ", href='" + href + '\'' +
                ", type=" + type +
                '}';
    }
}
