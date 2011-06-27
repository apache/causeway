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


package org.apache.isis.example.claims.fixture;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.claims.dom.claim.Claim;
import org.apache.isis.example.claims.dom.claim.ClaimItem;
import org.apache.isis.example.claims.dom.employee.Employee;


public class ClaimsFixture extends AbstractFixture {

    @Override
    public void install() {
        Employee fred = createEmployee("Fred Smith", null /*, new Location().north(0.5)*/);
        Employee tom = createEmployee("Tom Brown", fred /*, new Location().west(0.5)*/);
        createEmployee("Sam Jones", fred /*, new Location().west(1).south(0.25)*/);

        Claim claim = createClaim(tom, -16, "Meeting with client");
        addItem(claim, -16, 38.50, "Lunch with client");
        addItem(claim, -16, 16.50, "Euston - Mayfair (return)");
        
        claim = createClaim(tom, -18, "Meeting in city office");
        addItem(claim, -16, 18.00, "Car parking");
        addItem(claim, -16, 26.50, "Reading - London (return)");

        claim = createClaim(fred, -14, "Meeting at clients");
        addItem(claim, -14, 18.00, "Car parking");
        addItem(claim, -14, 26.50, "Reading - London (return)");

    }
    
    private Employee createEmployee(String name, Employee approver /*, Location location*/) {
        Employee claimant;
        claimant = newTransientInstance(Employee.class);
        claimant.setName(name);
        claimant.setApprover(approver);
        //claimant.setLocation(location);
        persist(claimant);
        return claimant;
    }

    @SuppressWarnings("unused")
    private Image asImage(String name) {
        String imageFileName = asImageFileName(name);
        try {
            return ImageIO.read(new File(imageFileName));
        } catch (IOException e) {
            return null;
        }
    }

    private String asImageFileName(String name) {
        return "images/"+name.replaceAll("[ ]", "")+".jpg";
    }

    private Claim createClaim(Employee claimant, int days, String description) { 
        Claim claim = newTransientInstance(Claim.class);
        claim.setClaimant(claimant);
        claim.setDescription(description);
        Date date = new Date();
        date = date.add(0,0, days);
        claim.setDate(date);
        persist(claim);
        return claim;
    }

    private void addItem(Claim claim, int days, double amount, String description) { 
        ClaimItem claimItem = newTransientInstance(ClaimItem.class);
        Date date = new Date();
        date = date.add(0,0, days);
        claimItem.setDateIncurred(date);
        claimItem.setDescription(description);
        claimItem.setAmount(new Money(amount, "USD"));
        persist(claimItem);
        claim.addToItems(claimItem);
    }

}
