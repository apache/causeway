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
    class="causeway-vue-route-page petclinic-owner-page"
    data-causeway-route-page
    data-testid="petclinic-vue-owner-page"
    data-page-kind="pet-owner"
    data-route-state="loading"
    tabindex="-1"
    aria-label="Pet owner page"
  >
    <cw-object-context
      data-causeway-route-context
      :logical-type="logicalTypeName"
      :object-id="objectId"
    >
      <cw-action-results
        data-causeway-page-result
        class="petclinic-card"
        aria-label="Pet owner action results"
        hidden
      />
      <cw-breadcrumbs data-testid="petclinic-vue-breadcrumbs" />
      <div class="petclinic-owner-heading">
        <cw-object-header />
        <div class="petclinic-toolbar" aria-label="Owner actions">
          <cw-action id="allOwners" named="Show all owners" />
          <cw-action id="relatedOwners" named="Show related owners">
            <cw-standalone-collection named="Related owners">
              <cw-collection-column id="name" label="Owner" />
              <cw-collection-column id="knownAs" label="Known as" />
            </cw-standalone-collection>
          </cw-action>
          <cw-action id="delete" named="Remove this owner" />
        </div>
      </div>

      <div class="petclinic-owner-grid">
        <section class="petclinic-card" aria-labelledby="owner-details-heading">
          <h2 id="owner-details-heading">Owner details</h2>
          <cw-property id="name" named="Full name">
            <cw-action id="updateName" named="Change the owner's name" prompt-style="INLINE">
              <cw-parameter id="name" named="Owner's full name" />
            </cw-action>
          </cw-property>
          <cw-property id="knownAs" editable />
          <cw-property id="telephoneNumber" editable />
          <cw-property id="emailAddress" editable />
          <cw-property id="notes" editable multi-line="5" />
          <cw-property id="lastVisit" editable label-position="TOP" min="2000-01-01" max="today" />
        </section>

        <section class="petclinic-card" aria-labelledby="owner-pets-heading">
          <h2 id="owner-pets-heading">Pets</h2>
          <cw-collection id="pets" named="Companion animals" active paged="5" sortable filterable>
            <cw-peek>
              <section class="petclinic-preview" aria-label="Pet preview">
                <cw-object-header />
                <cw-property id="name" />
                <cw-property id="species" />
                <cw-property id="notes" editable multi-line="3" />
                <cw-action id="clearNotes" named="Clear pet notes" />
                <cw-collection id="visits" named="Pet visits" active paged="10">
                  <cw-collection-column id="visitAt" label="When" />
                  <cw-collection-column id="reason" label="Reason" />
                </cw-collection>
              </section>
            </cw-peek>
            <cw-collection-column id="name" label="Name" />
            <cw-collection-column id="species" label="Species" />
            <cw-collection-column id="notes" label="Notes" />
            <cw-action id="addPet" named="Register a pet" prompt-style="DIALOG_SIDEBAR">
              <cw-parameter id="name" named="Pet name" />
            </cw-action>
            <cw-action id="removePet" />
          </cw-collection>
        </section>

        <section class="petclinic-card" aria-labelledby="owner-visits-heading">
          <h2 id="owner-visits-heading">Visits</h2>
          <cw-collection id="visits" named="Visit history" active paged="8">
            <cw-peek />
            <cw-collection-column id="visitAt" label="When" />
            <cw-collection-column id="reason" label="Reason" />
            <cw-action id="bookVisit" prompt-style="DIALOG_MODAL">
              <cw-parameter id="visitDate" min="tomorrow" />
              <cw-parameter id="visitTime" min="08:00" max="17:00" />
              <cw-parameter id="reason" multi-line="3" />
            </cw-action>
          </cw-collection>
        </section>
      </div>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
