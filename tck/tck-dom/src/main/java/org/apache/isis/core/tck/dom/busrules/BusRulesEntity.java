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

package org.apache.isis.core.tck.dom.busrules;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("BSRL")
@javax.jdo.annotations.Query(
        name="prmv_findByIntProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.busrules.BusRulesEntity WHERE intProperty == :i")
@ObjectType("BSRL")
public class BusRulesEntity extends AbstractDomainObject {

    // {{ Id (Integer)
    private Integer id;

    @javax.jdo.annotations.PrimaryKey // must be on the getter.
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }
    // }}

    // {{ Title
    public String title() {
        return null;
    }
    // }}


    // {{ visibleAndEditableProperty
    private int visibleAndEditableProperty;

    @MemberOrder(sequence = "1")
    public int getVisibleAndEditableProperty() {
        return visibleAndEditableProperty;
    }

    public void setVisibleAndEditableProperty(final int intProperty) {
        this.visibleAndEditableProperty = intProperty;
    }
    // }}


    // {{ visibleButNotEditableProperty
    private int visibleButNotEditableProperty;

    @Disabled
    @MemberOrder(sequence = "2")
    public int getVisibleButNotEditableProperty() {
        return visibleButNotEditableProperty;
    }

    public void setVisibleButNotEditableProperty(final int intProperty) {
        this.visibleButNotEditableProperty = intProperty;
    }
    // }}

    // {{ invisibleProperty
    private int invisibleProperty;

    @Hidden
    @MemberOrder(sequence = "3")
    public int getInvisibleProperty() {
        return invisibleProperty;
    }

    public void setInvisibleProperty(final int intProperty) {
        this.invisibleProperty = intProperty;
    }
    // }}

    
    // {{ VisibleAndEditableCollection (Collection)
    private List<BusRulesEntityChild> editableCollection = new ArrayList<BusRulesEntityChild>();

    @MemberOrder(sequence = "11")
    public List<BusRulesEntityChild> getVisibleAndEditableCollection() {
        return editableCollection;
    }

    public void setVisibleAndEditableCollection(final List<BusRulesEntityChild> children) {
        this.editableCollection = children;
    }
    // }}

    
    // {{ VisibleButNotEditableCollection (Collection)
    private List<BusRulesEntityChild> notEditableCollection = new ArrayList<BusRulesEntityChild>();

    @MemberOrder(sequence = "11")
    public List<BusRulesEntityChild> getVisibleButNotEditableCollection() {
        return notEditableCollection;
    }

    public void setVisibleButNotEditableCollection(final List<BusRulesEntityChild> children) {
        this.notEditableCollection = children;
    }
    // }}

    // {{ InvisibleCollection (Collection)
    private List<BusRulesEntityChild> invisibleCollection = new ArrayList<BusRulesEntityChild>();

    @Hidden
    @MemberOrder(sequence = "11")
    public List<BusRulesEntityChild> getInvisibleCollection() {
        return invisibleCollection;
    }

    public void setInvisibleCollection(final List<BusRulesEntityChild> children) {
        this.invisibleCollection = children;
    }
    // }}

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity visibleAndInvokableAction() {
        return this;
    }

    @Disabled
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity visibleButUninvokableAction() {
        return this;
    }

    @Hidden
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity invisibleAction() {
        return this;
    }

}
