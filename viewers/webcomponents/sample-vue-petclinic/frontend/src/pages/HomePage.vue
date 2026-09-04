<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->
<script setup lang="ts">
import type {CausewayRoutePageProps} from '@apache-causeway/vue-viewer';

defineProps<CausewayRoutePageProps>();
</script>

<template>
  <section
    class="causeway-vue-route-page causeway-route-page causeway-route-object"
    data-causeway-route-page
    data-testid="petclinic-vue-home-page"
    data-page-kind="home"
    data-route-state="loading"
    tabindex="-1"
    aria-label="Object page"
  >
    <cw-object-context
      data-causeway-route-context
      :logical-type="logicalTypeName"
      :object-id="objectId"
    >
      <article class="petclinic-page petclinic-dashboard" data-testid="petclinic-custom-home">
        <cw-object-header />
        <div class="petclinic-dashboard-grid">
          <section aria-labelledby="petclinic-owner-heading">
            <h2 id="petclinic-owner-heading">Pet owners</h2>
            <cw-collection id="petOwners" label="Pet owners" active paged="5" sortable filterable>
              <cw-preview>
                <section class="petclinic-preview" aria-label="Owner preview">
                  <cw-object-header />
                  <cw-property id="knownAs" editable />
                  <cw-property id="telephoneNumber" />
                  <cw-property id="emailAddress" />
                  <cw-action id="updateName" named="Change owner name" />
                  <cw-collection id="pets" named="Companion animals" active paged="2">
                    <cw-collection-column id="name" label="Name" />
                    <cw-collection-column id="species" label="Species" />
                  </cw-collection>
                </section>
              </cw-preview>
              <cw-collection-column id="name" label="Owner" />
              <cw-collection-column id="telephoneNumber" label="Telephone" />
              <cw-collection-column id="emailAddress" label="Email" />
            </cw-collection>
          </section>
          <section aria-labelledby="petclinic-visit-heading">
            <h2 id="petclinic-visit-heading">Upcoming visits</h2>
            <cw-collection id="futureVisits" named="Next appointments" active paged="10" filterable>
              <cw-preview>
                <section class="petclinic-preview" aria-label="Visit preview">
                  <cw-object-header />
                  <cw-property id="visitAt" named="Appointment" />
                  <cw-property id="reason" editable />
                  <cw-property id="notes" editable multi-line="3" />
                </section>
              </cw-preview>
              <cw-collection-column id="visitAt" label="When" />
              <cw-collection-column id="reason" label="Reason" />
              <cw-collection-column id="notes" label="Notes" />
            </cw-collection>
          </section>
        </div>
      </article>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
