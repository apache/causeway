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

package org.apache.isis.example.application.claims.objstore.jdo;

import java.util.List;

import org.apache.isis.applib.value.Date;
import org.apache.isis.example.application.claims.dom.claim.Claim;
import org.apache.isis.example.application.claims.dom.claim.ClaimRepository;
import org.apache.isis.example.application.claims.dom.claim.Claimant;

public class ClaimRepositoryJdo extends ClaimRepository {



    // {{ action: findClaims
    @Override
    public List<Claim> findClaims(final String description) {
        // TODO: convert to JDO
        return super.findClaims(description);
    }

    // }}

    // {{ action: claimsFor
    @Override
    public List<Claim> claimsFor(final Claimant claimant) {
        // TODO: convert to JDO
        return super.claimsFor(claimant);
    }

    // }}

    // {{ action: claimsSince
    @Override
    public List<Claim> claimsSince(final Claimant claimant, final Date since) {
        // TODO: convert to JDO
        return super.claimsSince(claimant, since);
    }
    // }}

}
