/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.layout.v1_0;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Where;

@XmlType(
        propOrder = {
                "named"
                , "describedAs"
        }
)
public class PropertyLayout {

    private String cssClass;

    @XmlAttribute(required = false)
    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }


    private String describedAs;

    @XmlElement(required = false)
    public String getDescribedAs() {
        return describedAs;
    }

    public void setDescribedAs(String describedAs) {
        this.describedAs = describedAs;
    }


    private Where hidden;

    @XmlAttribute(required = false)
    public Where getHidden() {
        return hidden;
    }

    public void setHidden(Where hidden) {
        this.hidden = hidden;
    }


    private LabelPosition labelPosition;

    @XmlAttribute(required = false)
    public LabelPosition getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(LabelPosition labelPosition) {
        this.labelPosition = labelPosition;
    }


    private Integer multiLine;

    @XmlAttribute(required = false)
    public Integer getMultiLine() {
        return multiLine;
    }

    public void setMultiLine(Integer multiLine) {
        this.multiLine = multiLine;
    }


    private String named;

    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
    }


    private Boolean namedEscaped;

    @XmlAttribute(required = false)
    public Boolean getNamedEscaped() {
        return namedEscaped;
    }

    public void setNamedEscaped(Boolean namedEscaped) {
        this.namedEscaped = namedEscaped;
    }


    private Boolean renderedAsDayBefore;

    @XmlAttribute(required = false)
    public Boolean getRenderedAsDayBefore() {
        return renderedAsDayBefore;
    }

    public void setRenderedAsDayBefore(Boolean renderedAsDayBefore) {
        this.renderedAsDayBefore = renderedAsDayBefore;
    }


    private Integer typicalLength;

    @XmlAttribute(required = false)
    public Integer getTypicalLength() {
        return typicalLength;
    }

    public void setTypicalLength(Integer typicalLength) {
        this.typicalLength = typicalLength;
    }
}
