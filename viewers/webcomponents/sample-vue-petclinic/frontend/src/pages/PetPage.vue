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
    data-testid="petclinic-vue-pet-page"
    data-page-kind="pet"
    data-route-state="loading"
    tabindex="-1"
    aria-label="Object page"
  >
    <cw-object-context
      data-causeway-route-context
      :logical-type="logicalTypeName"
      :object-id="objectId"
    >
      <article class="petclinic-page petclinic-pet-page" data-testid="petclinic-pet-page">
        <cw-action-results
          data-causeway-page-result
          aria-label="Pet action results"
          presentation-style="SIDEBAR"
          data-testid="petclinic-sidebar-results"
          hidden
        />
        <cw-breadcrumbs data-testid="petclinic-breadcrumbs" />
        <cw-object-header />

        <div class="petclinic-object-grid">
          <section class="petclinic-card" aria-labelledby="petclinic-pet-identity-heading">
            <h2 id="petclinic-pet-identity-heading">Identity</h2>
            <div class="petclinic-field-list">
              <cw-property id="petOwner" />
              <cw-property id="name" />
            </div>
          </section>

          <section class="petclinic-card" aria-labelledby="petclinic-pet-details-heading">
            <h2 id="petclinic-pet-details-heading">Details</h2>
            <div class="petclinic-field-list">
              <cw-property id="species" />
              <cw-property id="notes" editable multi-line="5" />
            </div>
          </section>
        </div>

        <section class="petclinic-card" aria-labelledby="petclinic-pet-documents-heading">
          <h2 id="petclinic-pet-documents-heading">Documents</h2>
          <cw-property
            id="pdfManual"
            pdf-render="manual"
            pdf-initial-page="2"
            pdf-zoom="page-fit"
            data-testid="petclinic-pdf-manual"
          />
        </section>
      </article>
      <cw-interaction-controller data-causeway-route-interactions />
    </cw-object-context>
  </section>
</template>
