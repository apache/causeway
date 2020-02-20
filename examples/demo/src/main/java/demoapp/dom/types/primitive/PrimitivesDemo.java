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
package demoapp.dom.types.primitive;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.commons.internal.collections._Lists;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.Primitives", editing=Editing.ENABLED)
@Log4j2
public class PrimitivesDemo extends DemoStub {

    @Override
    public String title() {
        return "Primitives Demo";
    }

    // -- NULL
    
    @Property(editing=Editing.DISABLED)
    @PropertyLayout(describedAs="null")
    @Getter @Setter private Object nullObject;
    
    @Action
    public List<String> calculateNullCollection() {
        return null;
    }
    
    @Action
    public PrimitivesDemo calculateNull() {
        return null;
    }
    
    @Action
    public void calculateVoid() {
    }
    
    // -- BOOLEAN
    
    @Property(
            optionality = Optionality.MANDATORY,
            editing=Editing.NOT_SPECIFIED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Boolean")
    @Getter @Setter private Boolean javaLangBoolean;
    
    @Property(
            optionality = Optionality.OPTIONAL,
            editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970)
    @PropertyLayout(describedAs="Nullable (3 state)")
    @Getter @Setter private Boolean nullableBoolean;
    
    @Getter private boolean primitiveFalse = false;
    @Getter private boolean primitiveTrue = true;
    
    @Action
    public Boolean calculateBoolean() {
        return Boolean.TRUE;
    }
    
    @Action
    public List<Boolean> calculateBooleans() {
        return _Lists.of(Boolean.FALSE, Boolean.TRUE, null);
    }
    
    
    // -- BYTE

    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Byte")
    @Getter @Setter private Byte javaLangByte;
    
    @Getter private byte primitiveByte = Byte.MIN_VALUE;
    
    @Action
    public Byte calculateByte() {
        return Byte.MAX_VALUE;
    }
    
    @Action
    public List<Byte> calculateBytes() {
        return _Lists.of(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }
    
    // -- SHORT

    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Short")
    @Getter @Setter private Short javaLangShort;
    
    @Getter private short primitiveShort = Short.MIN_VALUE;
    
    @Action
    public Short calculateShort() {
        return Short.MAX_VALUE;
    }
    
    @Action
    public List<Short> calculateShorts() {
        return _Lists.of(Short.MIN_VALUE, Short.MAX_VALUE);
    }
    
    // -- INTEGER
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Integer")
    @Getter @Setter private Integer javaLangInteger;
    
    @Getter private int primitiveInteger = Integer.MIN_VALUE;
    
    @Action
    public Integer calculateInteger() {
        return Integer.MAX_VALUE;
    }
    
    @Action
    public List<Integer> calculateIntegers() {
        return _Lists.of(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    // -- LONG
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Long")
    @Getter @Setter private Long javaLangLong;
    
    @Getter private long primitiveLong = Long.MIN_VALUE;
    
    @Action
    public Long calculateLong() {
        return Long.MAX_VALUE;
    }
    
    @Action
    public List<Long> calculateLongs() {
        return _Lists.of(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    // -- FLOAT
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Float")
    @Getter @Setter private Float javaLangFloat;
    
    @Getter private float primitiveFloat = Float.MIN_VALUE;
    
    @Action
    public Float calculateFloat() {
        return Float.MAX_VALUE;
    }
    
    @Action
    public List<Float> calculateFloats() {
        return _Lists.of(Float.MIN_VALUE, Float.MAX_VALUE);
    }
    
    // -- DOUBLE
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.lang.Double")
    @Getter @Setter private Double javaLangDouble;
    
    @Getter private double primitiveDouble = Double.MIN_VALUE;
    
    @Action
    public Double calculateDouble() {
        return Double.MAX_VALUE;
    }
    
    @Action
    public List<Double> calculateDoubles() {
        return _Lists.of(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    // --

    @Override
    public void initDefaults() {

        log.info("PrimitivesDemo::initDefaults");

        javaLangByte = Byte.MAX_VALUE;
        javaLangShort = Short.MAX_VALUE;
        javaLangInteger = Integer.MAX_VALUE;
        javaLangLong = Long.MAX_VALUE;
        
        javaLangFloat = Float.MAX_VALUE;
        javaLangDouble = Double.MAX_VALUE;
        
    }


}
