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

package org.apache.isis.support.prototype.junit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.support.prototype.dom.claim.Approver;
import org.apache.isis.support.prototype.dom.claim.Claim;
import org.apache.isis.support.prototype.fixture.ClaimsFixture;
import org.apache.isis.viewer.junit.Fixture;
import org.apache.isis.viewer.junit.Fixtures;
import org.junit.Test;

@Fixtures({ @Fixture(ClaimsFixture.class) })
public class ClaimSubmitTest extends AbstractTest {

    @Test
    public void cannotSubmitTwice() throws Exception {
        Claim tomsSubmittedClaim = tomsSubmittedClaim();
        try {
            Approver approver = tomEmployee.getApprover();
            tomsSubmittedClaim.submit(approver);
            fail("Should not be able to submit again");
        } catch (DisabledException e) {
            assertThat(e.getMessage(), is("Claim has already been submitted"));
        }
    }

    private Claim tomsSubmittedClaim() {
        List<Claim> tomsClaims = claimRepository.claimsFor(tomEmployee);
        Claim tomsClaim1 = tomsClaims.get(0);
        tomsClaim1.submit(tomEmployee.getApprover());
        assertThat(tomsClaim1.getStatus(), is("Submitted"));
        return wrapped(tomsClaim1);
    }

}
