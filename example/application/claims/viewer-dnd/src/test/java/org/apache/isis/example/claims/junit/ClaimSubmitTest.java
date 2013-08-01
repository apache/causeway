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

package org.apache.isis.example.claims.junit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.core.integtestsupport.legacy.Fixture;
import org.apache.isis.core.integtestsupport.legacy.Fixtures;
import org.apache.isis.example.application.claims.dom.claim.Approver;
import org.apache.isis.example.application.claims.dom.claim.Claim;
import org.apache.isis.example.application.claims.fixture.ClaimsFixture;

@Fixtures({ @Fixture(ClaimsFixture.class) })
public class ClaimSubmitTest extends AbstractTest {

    @Test
    public void cannotSubmitTwice() throws Exception {
        final Claim tomsSubmittedClaim = tomsSubmittedClaim();
        try {
            final Approver approver = tomEmployee.getDefaultApprover();
            tomsSubmittedClaim.submit(approver);
            fail("Should not be able to submit again");
        } catch (final DisabledException e) {
            assertThat(e.getMessage(), Matchers.containsString("Claim has already been submitted"));
        }
    }

    private Claim tomsSubmittedClaim() {
        final List<Claim> tomsClaims = claimRepository.claimsFor(tomEmployee);
        final Claim tomsClaim1 = tomsClaims.get(0);
        tomsClaim1.submit(tomEmployee.getDefaultApprover());
        assertThat(tomsClaim1.getStatus(), is("Submitted"));
        return wrapped(tomsClaim1);
    }

}
