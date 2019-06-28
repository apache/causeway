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
package org.apache.isis.jdo.metamodel.facets.prop.column;

import javax.jdo.annotations.Column;

import org.datanucleus.enhancement.Persistable;

public abstract class SimpleObjectWithColumnAllowsNullAnnotations implements Persistable {


    private int primitiveWithNoAnnotation;
    public int getPrimitiveWithNoAnnotation() {
        return primitiveWithNoAnnotation;
    }
    public void setPrimitiveWithNoAnnotation(int primitive) {
        this.primitiveWithNoAnnotation = primitive;
    }
    
    private int primitiveWithNoAllowsNull;
    @Column
    public int getPrimitiveWithNoAllowsNull() {
        return primitiveWithNoAllowsNull;
    }
    public void setPrimitiveWithNoAllowsNull(int primitive) {
        this.primitiveWithNoAllowsNull = primitive;
    }
    
    private int primitiveWithAllowsNullFalse;
    @Column(allowsNull="false")
    public int getPrimitiveWithAllowsNullFalse() {
        return primitiveWithAllowsNullFalse;
    }
    public void setPrimitiveWithAllowsNullFalse(int primitive) {
        this.primitiveWithAllowsNullFalse = primitive;
    }
    

    private int primitiveWithAllowsNullTrue;
    @Column(allowsNull="true")
    public int getPrimitiveWithAllowsNullTrue() {
        return primitiveWithAllowsNullTrue;
    }
    public void setPrimitiveWithAllowsNullTrue(int primitive) {
        this.primitiveWithAllowsNullTrue = primitive;
    }
    
    
    private String referenceWithNoAnnotation;
    public String getReferenceWithNoAnnotation() {
        return referenceWithNoAnnotation;
    }
    public void setReferenceWithNoAnnotation(String reference) {
        this.referenceWithNoAnnotation = reference;
    }
    
    private String referenceWithNoAllowsNull;
    @Column
    public String getReferenceWithNoAllowsNull() {
        return referenceWithNoAllowsNull;
    }
    public void setReferenceWithNoAllowsNull(String reference) {
        this.referenceWithNoAllowsNull = reference;
    }
    
    private String referenceWithAllowsNullFalse;
    @Column(allowsNull="false")
    public String getReferenceWithAllowsNullFalse() {
        return referenceWithAllowsNullFalse;
    }
    public void setReferenceWithAllowsNullFalse(String referenceWithAllowsNullFalse) {
        this.referenceWithAllowsNullFalse = referenceWithAllowsNullFalse;
    }
    
    
    private String referenceWithAllowsNullTrue;
    @Column(allowsNull="true")
    public String getReferenceWithAllowsNullTrue() {
        return referenceWithAllowsNullTrue;
    }
    public void setReferenceWithAllowsNullTrue(String referenceWithAllowsNullTrue) {
        this.referenceWithAllowsNullTrue = referenceWithAllowsNullTrue;
    }
    

}