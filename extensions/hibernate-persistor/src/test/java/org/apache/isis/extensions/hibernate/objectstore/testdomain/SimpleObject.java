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


package org.apache.isis.extensions.hibernate.objectstore.testdomain;

import java.util.Date;


public class SimpleObject {
    public static String fieldOrder() {
        return "string, someDate, someTime, longField";
    }

    // Property: Hibernate id/fields
    private Long id;
    protected Long getId() {
        return id;
    }
    protected void setId(final Long id) {
        this.id = id;
    }

    
    // {{ Property: String
    private String myString = new String();
    public String getString() {
        return myString;
    }
    public void setString(final String string) {
        myString = string;
    }
    // }}

    

    // {{ Property: LongField
    private long longField;
    public long getLongField() {
        return longField;
    }
    public void setLongField(final long value) {
        longField = value;
    }
    // }}
    

    // {{ Property: SomeDate
    private org.apache.isis.applib.value.Date someDate;
    public org.apache.isis.applib.value.Date getSomeDate() {
        return someDate;
    }
    public void setSomeDate(final org.apache.isis.applib.value.Date date) {
        this.someDate = date;
    }
    // }}

    
    // {{ Property: SomeTime
    private org.apache.isis.applib.value.Time someTime;
    public org.apache.isis.applib.value.Time getSomeTime() {
        return someTime;
    }
    public void setSomeTime(final org.apache.isis.applib.value.Time time) {
        this.someTime = time;
    }
    // }}
    


    
    // {{ special property: LastUpdateUser
    private String lastUpdateUser;
    protected String getLastUpdateUser() {
        return lastUpdateUser;
    }
    protected void setLastUpdateUser(final String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }
    // }}
    


    // {{ special property: LastUpdated
    private Date lastUpdated;
    protected Date getLastUpdated() {
        return lastUpdated;
    }
    protected void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    // }}
    





}
