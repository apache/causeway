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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.ObjectType;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator("BSRC")
@javax.jdo.annotations.Query(
        name="prmv_findByIntProperty", language="JDOQL",  
        value="SELECT FROM org.apache.isis.tck.dom.busrules.BusRulesEntityChild WHERE intProperty == :i")
@ObjectType("BSRC")
public class BusRulesEntityChild extends AbstractDomainObject {


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


    
}
