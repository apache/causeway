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
    data-testid="petclinic-vue-visit-page"
    data-page-kind="visit"
    data-route-state="loading"
    tabindex="-1"
    aria-label="Object page"
  >
    <cw-object-context
      data-causeway-route-context
      :logical-type="logicalTypeName"
      :object-id="objectId"
    >
      <article class="petclinic-page petclinic-visit-page" data-testid="petclinic-visit-page">
        <cw-action-results
          data-causeway-page-result
          aria-label="Visit action results"
          presentation-style="DIALOG"
          data-testid="petclinic-dialog-results"
          hidden
        />
        <cw-breadcrumbs data-testid="petclinic-breadcrumbs" />
        <cw-object-header />

        <div class="petclinic-object-grid">
          <section class="petclinic-card" aria-labelledby="petclinic-visit-appointment-heading">
            <h2 id="petclinic-visit-appointment-heading">Appointment</h2>
            <div class="petclinic-field-list">
              <cw-property id="pet" />
              <cw-property id="visitAt" />
            </div>
          </section>

          <section class="petclinic-card" aria-labelledby="petclinic-visit-details-heading">
            <h2 id="petclinic-visit-details-heading">Details</h2>
            <div class="petclinic-field-list">
              <cw-property id="reason" editable />
              <cw-property id="notes" editable multi-line="5" />
            </div>
          </section>
        </div>

        <section class="petclinic-card" aria-labelledby="petclinic-visit-documents-heading">
          <h2 id="petclinic-visit-documents-heading">Documents</h2>
          <cw-property id="ownerAgreementDocument" pdf-render="link" data-testid="petclinic-owner-agreement-document" />
        </section>
      </article>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
