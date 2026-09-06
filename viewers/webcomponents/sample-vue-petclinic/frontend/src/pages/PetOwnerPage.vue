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
          </div>
        </div>

        <cw-row data-testid="petclinic-owner-macro-layout">
          <cw-column span="4" data-testid="petclinic-owner-details">
            <cw-tabgroup name="Owner information" data-testid="petclinic-owner-layout-tabs">
              <cw-tab name="Identity" selected>
                <cw-row>
                  <cw-column span="12">
                    <cw-fieldset name="Identity" class="petclinic-card">
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
                    </cw-fieldset>
                  </cw-column>
                </cw-row>
              </cw-tab>
              <cw-unreferenced-properties data-testid="petclinic-owner-unreferenced-properties" />
              <cw-tab name="Metadata">
                <cw-row>
                  <cw-column span="12">
                    <cw-metadata data-testid="petclinic-owner-metadata" />
                  </cw-column>
                </cw-row>
              </cw-tab>
            </cw-tabgroup>

            <cw-fieldset name="Contact" class="petclinic-card">
              <cw-property id="telephoneNumber" editable />
              <cw-property id="emailAddress" editable />
            </cw-fieldset>

            <cw-fieldset name="Details" class="petclinic-card">
              <cw-property id="notes" editable multi-line="5" />
              <cw-property id="lastVisit" editable label-position="TOP" min="2000-01-01" max="today" />
            </cw-fieldset>
          </cw-column>

          <cw-column span="8" data-testid="petclinic-owner-collections">
            <cw-collection id="pets" class="petclinic-card" named="Pets" active paged="5" sortable filterable>
                <cw-preview>
                  <section class="petclinic-preview" aria-label="Pet preview">
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

            <cw-collection
              id="visits"
              class="petclinic-card"
              named="Visits"
                described-as="All visits recorded for this owner's pets."
                active
                paged="8"
              >
                <cw-preview>
                  <section class="petclinic-preview" aria-label="Visit preview">
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

            <cw-fieldset name="Agreement" class="petclinic-card petclinic-agreement-card">
              <cw-property
                id="agreement"
                label-position="NONE"
                pdf-render="auto"
                pdf-initial-page="1"
                pdf-zoom="page-width"
                data-testid="petclinic-owner-agreement"
              />
            </cw-fieldset>

            <cw-unreferenced-collections data-testid="petclinic-owner-unreferenced-collections" />
            <cw-unreferenced-actions data-testid="petclinic-owner-unreferenced-actions" />
          </cw-column>
        </cw-row>
      </article>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
