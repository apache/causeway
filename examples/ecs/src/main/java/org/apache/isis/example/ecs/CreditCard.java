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


package org.apache.isis.example.ecs;

import org.apache.isis.applib.AbstractDomainObject;


public class CreditCard extends AbstractDomainObject implements PaymentMethod {
	
	
    private String expires;
    public String getExpires() {
        resolve(expires);
        return expires;
    }
    public void setExpires(String expires) {
        this.expires = expires;
        objectChanged();
    }

    private String nameOnCard;
    public String getNameOnCard() {
        resolve(nameOnCard);
        return nameOnCard;
    }
    public void setNameOnCard(String nameOnCard) {
    	this.nameOnCard = nameOnCard;
    	objectChanged();
    }

    
    
    private String number;
    public String getNumber() {
        resolve(number);
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
        objectChanged();
    }

    
    public String toString() {
        if (getNumber() == null) {
            return "";
        } else {
            int pos = Math.max(0, getNumber().length() - 5);
            return "*****************".substring(0, pos).concat(getNumber().substring(pos));
        }
    }
}

