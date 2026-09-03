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
import {ref} from 'vue';
import {RouterView, useRoute} from 'vue-router';
import {useCausewayShell, useCausewayViewer} from '@apache-causeway/vue-viewer';

const viewer = useCausewayViewer();
const route = useRoute();
const shell = useCausewayShell(ref<HTMLElement | null>(null));
</script>

<template>
  <div ref="shell" class="petclinic-vue-shell" data-testid="petclinic-vue-application-shell">
    <a class="petclinic-skip-link" href="#causeway-vue-route">Skip to main content</a>
    <cw-graphql-client data-causeway-shell-client :endpoint="viewer.endpoint">
      <header class="petclinic-navbar">
        <a class="petclinic-brand" href="/vue/" @click.prevent="viewer.router.push('/')">
          <span aria-hidden="true">C</span>
          <span>Pet Clinic</span>
        </a>
        <cw-menubars />
      </header>
      <div data-causeway-route-loading class="petclinic-route-loading" role="status" aria-live="polite" hidden>
        Loading page…
      </div>
      <div data-causeway-route-announcement class="petclinic-visually-hidden" aria-live="polite" aria-atomic="true" />
      <cw-action-results
        data-causeway-shell-result
        class="petclinic-shell-results"
        aria-label="Application action results"
        aria-live="polite"
        hidden
      />
      <main id="causeway-vue-route" data-causeway-router-view class="petclinic-main" aria-busy="false" tabindex="-1">
        <RouterView v-slot="{ Component }">
          <component :is="Component" :key="route.path" />
        </RouterView>
      </main>
      <footer class="petclinic-footer">Powered by Apache Causeway · Vue viewer</footer>
    </cw-graphql-client>
  </div>
</template>
