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


package org.apache.isis.app.cart;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.RegEx;


public class PaymentMethod extends AbstractDomainObject {
    private String nameOnCard;
    private String number;
    
    public String title() {
        String number = getNumber();
        if (number == null) {
            return "";
        } else {
            int len = number.length();
            if (len > 5) {
               return "********************".substring(0, len - 4) + number.substring(len - 4, len);
            }
            return number; 
        }
    }

    public String getNameOnCard() {
        resolve(nameOnCard);
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
        objectChanged();
    }
    
    @RegEx(validation="[0-9]*")
    @MaxLength(18)
    public String getNumber() {
        resolve(number);
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        objectChanged();
    }
}

