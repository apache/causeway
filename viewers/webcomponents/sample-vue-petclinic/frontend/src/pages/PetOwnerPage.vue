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
    data-testid="petclinic-vue-owner-page"
    data-page-kind="pet-owner"
    data-route-state="loading"
    tabindex="-1"
    aria-label="Object page"
  >
    <cw-object-context
      data-causeway-route-context
      :logical-type="logicalTypeName"
      :object-id="objectId"
    >
      <article class="petclinic-page petclinic-owner-page" data-testid="petclinic-owner-page">
        <cw-action-results
          data-causeway-page-result
          class="petclinic-card"
          aria-label="Pet owner action results"
          data-testid="petclinic-action-results"
          hidden
        />
        <cw-breadcrumbs data-testid="petclinic-breadcrumbs" />
        <div class="petclinic-object-heading">
          <cw-object-header />
          <div class="petclinic-page-toolbar" aria-label="Owner actions">
            <cw-action id="allOwners" named="Show all owners" />
            <cw-action id="noOwners" named="Show empty owner result" />
            <cw-action id="relatedOwners" named="Show related owners">
              <cw-standalone-collection named="Related owners">
                <cw-collection-column id="name" label="Owner" />
                <cw-collection-column id="knownAs" label="Known as" />
                <cw-collection-column id="notes" label="Notes" />
              </cw-standalone-collection>
            </cw-action>
            <cw-action id="delete" named="Remove this owner" />
          </div>
        </div>

        <div class="petclinic-object-grid">
          <div class="petclinic-object-details">
            <section class="petclinic-card" aria-labelledby="petclinic-owner-identity-heading">
              <h2 id="petclinic-owner-identity-heading">Identity</h2>
              <div class="petclinic-field-list">
                <cw-property id="name" named="Full name">
                  <cw-action id="updateName" named="Change the owner's name" prompt-style="INLINE">
                    <cw-parameter
                      id="name"
                      named="Owner's full name"
                      described-as="The complete name used to identify this pet owner."
                      description-as="tooltip"
                    />
                  </cw-action>
                </cw-property>
                <cw-property
                  id="knownAs"
                  editable
                  described-as="The familiar or preferred name used by this owner."
                />
              </div>
            </section>

            <section class="petclinic-card" aria-labelledby="petclinic-owner-contact-heading">
              <h2 id="petclinic-owner-contact-heading">Contact</h2>
              <div class="petclinic-field-list">
                <cw-property id="telephoneNumber" editable />
                <cw-property id="emailAddress" editable />
              </div>
            </section>

            <section class="petclinic-card" aria-labelledby="petclinic-owner-details-heading">
              <h2 id="petclinic-owner-details-heading">Details</h2>
              <div class="petclinic-field-list">
                <cw-property id="notes" editable multi-line="5" />
                <cw-property id="lastVisit" editable label-position="TOP" min="2000-01-01" max="today" />
                <cw-property id="daysSinceLastVisit" />
              </div>
            </section>
          </div>

          <div class="petclinic-object-collections">
            <section class="petclinic-card" aria-labelledby="petclinic-owner-pets-heading">
              <h2 id="petclinic-owner-pets-heading">Pets</h2>
              <cw-collection id="pets" named="Companion animals" active paged="5" sortable filterable>
                <cw-preview>
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
                </cw-preview>
                <cw-collection-column id="name" label="Name" />
                <cw-collection-column id="species" label="Species" />
                <cw-collection-column id="notes" label="Notes" />
                <cw-action id="addPet" named="Register a pet" prompt-style="DIALOG_SIDEBAR">
                  <cw-parameter
                    id="name"
                    named="Pet name"
                    described-as="The name used for this companion animal."
                    description-as="label"
                  />
                </cw-action>
                <cw-action id="removePet" />
              </cw-collection>
            </section>

            <section class="petclinic-card" aria-labelledby="petclinic-owner-visits-heading">
              <h2 id="petclinic-owner-visits-heading">Visits</h2>
              <cw-collection
                id="visits"
                named="Visit history"
                described-as="All visits recorded for this owner's pets."
                active
                paged="8"
              >
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
                <cw-action id="bookVisit" prompt-style="DIALOG_MODAL">
                  <cw-parameter id="visitDate" min="tomorrow" />
                  <cw-parameter id="visitTime" min="08:00" max="17:00" />
                  <cw-parameter
                    id="reason"
                    named="Reason for visit"
                    described-as="Describe the purpose of the appointment."
                    description-as="label"
                    multi-line="3"
                  />
                </cw-action>
              </cw-collection>
            </section>
          </div>
        </div>
      </article>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
