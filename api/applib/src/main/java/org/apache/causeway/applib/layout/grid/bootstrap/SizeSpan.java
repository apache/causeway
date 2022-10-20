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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
/**
 * @since 1.x {@index}
 */
@XmlType(
        name = "sizeSpan"
        , propOrder = {}
        )
public class SizeSpan implements Serializable {

    private static final long serialVersionUID = 1L;


    public static SizeSpan with(final Size size, final int span) {
        final SizeSpan ss = new SizeSpan();
        ss.setSize(size);
        ss.setSpan(span);
        return ss;
    }

    public static SizeSpan offset(final Size size, final int span) {
        final SizeSpan ss = with(size, span);
        ss.setOffset(true);
        return ss;
    }

    private Size size;

    @XmlAttribute(required = true)
    public Size getSize() {
        return size;
    }

    public void setSize(final Size size) {
        this.size = size;
    }


    private int span;

    @XmlAttribute(required = true)
    public int getSpan() {
        return span;
    }

    public void setSpan(final int span) {
        this.span = span;
    }


    private Boolean offset;


    @XmlAttribute(required = false)
    public Boolean isOffset() {
        return offset;
    }

    public void setOffset(final Boolean offset) {
        this.offset = offset;
    }


    public String toCssClassFragment() {
        return appendCssClassFragment(new StringBuilder()).toString();
    }

    public StringBuilder appendCssClassFragment(final StringBuilder buf) {
        if(buf.length() > 0) {
            buf.append(" ");
        }
        if(Boolean.TRUE.equals(offset)) {
            buf.append("offset-").append(span);
        } else {
        	buf.append("col-");
        	switch(size) {
        	case XS:
        		buf.append("" + span);
        		break;
        	default:
        		buf.append(size.toCssClassFragment()).append("-").append("" + span);
        		break;
        	}
        }
        return buf;
    }
}
