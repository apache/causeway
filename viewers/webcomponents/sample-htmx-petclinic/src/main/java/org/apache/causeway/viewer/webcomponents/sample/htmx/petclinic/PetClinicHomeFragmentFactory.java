/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic;

import org.springframework.stereotype.Component;

import org.apache.causeway.viewer.webcomponents.htmx.HtmxObjectRoute;
import org.apache.causeway.viewer.webcomponents.htmx.HtmxPageFragmentFactory;

@Component
public class PetClinicHomeFragmentFactory implements HtmxPageFragmentFactory {

    @Override
    public String logicalTypeName() {
        return "petclinic.HomePage";
    }

    @Override
    public String render(final HtmxObjectRoute route) {
        return """
                <article class="petclinic-dashboard" data-testid="petclinic-custom-home">
                  <causeway-object-header></causeway-object-header>
                  <div class="petclinic-dashboard-grid">
                    <section aria-labelledby="petclinic-owner-heading">
                      <h2 id="petclinic-owner-heading">Pet owners</h2>
                      <causeway-collection member="petOwners" label="Pet owners" active offset="0" size="10">
                        <causeway-collection-column member="name" label="Owner"></causeway-collection-column>
                        <causeway-collection-column member="telephoneNumber" label="Telephone"></causeway-collection-column>
                        <causeway-collection-column member="emailAddress" label="Email"></causeway-collection-column>
                      </causeway-collection>
                    </section>
                    <section aria-labelledby="petclinic-visit-heading">
                      <h2 id="petclinic-visit-heading">Upcoming visits</h2>
                      <causeway-collection member="futureVisits" label="Upcoming visits" active offset="0" size="10">
                        <causeway-collection-column member="visitAt" label="When"></causeway-collection-column>
                        <causeway-collection-column member="reason" label="Reason"></causeway-collection-column>
                        <causeway-collection-column member="notes" label="Notes"></causeway-collection-column>
                      </causeway-collection>
                    </section>
                  </div>
                </article>
                """;
    }
}
