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
package demoapp.dom.domain.properties.Property.regexPattern;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyRegexPatternVm",
        editing = Editing.ENABLED
)
public class PropertyRegexPatternVm implements HasAsciiDocDescription {

    public String title() {
        return "Property#regexPattern";
    }

//tag::annotation[]
    @Property(
        regexPattern = "^\\w+@\\w+[.]com$"                          // <.>
        , regexPatternReplacement = "Must be .com email address"    // <.>
        , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(regexPattern = \"^\\w+@\\w+[.]com$\"\")",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String emailAddressPropertyUsingAnnotation;
//end::annotation[]

//tag::meta-annotated[]
    @RegexPatternEmailComMetaAnnotation                             // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@RegexPatternEmailComMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String emailAddressPropertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @RegexPatternEmailComMetaAnnotation                             // <.>
    @Property(
        regexPattern = "^\\w+@\\w+[.]org$"                          // <.>
        , regexPatternReplacement = "Must be .org email address"
        , regexPatternFlags = Pattern.COMMENTS
    )
    @PropertyLayout(
        describedAs =
            "@RegexPatternEmailComMetaAnnotation " +
            "@Property(regexPattern = \"^\\w+@\\w+[.]org$\"\")",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String emailAddressPropertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
