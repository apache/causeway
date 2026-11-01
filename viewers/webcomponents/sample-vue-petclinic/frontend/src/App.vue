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
import {onBeforeUnmount, onMounted, ref} from 'vue';
import {RouterView, useRoute} from 'vue-router';
import {useCausewayShell, useCausewayViewer} from '@apache-causeway/vue-viewer';
import {authenticationContext, graphQlExecutor} from './authentication';

const viewer = useCausewayViewer();
const route = useRoute();
const shell = useCausewayShell(ref<HTMLElement | null>(null));
let titleObserver: MutationObserver | undefined;

function applyApplicationTitle(): void {
  const title = document.title.trim();
  if (title && !title.endsWith(' · Pet Clinic')) {
    document.title = `${title} · Pet Clinic`;
  }
}

onMounted(() => {
  applyApplicationTitle();
  const titleElement = document.querySelector('title');
  if (titleElement) {
    titleObserver = new MutationObserver(applyApplicationTitle);
    titleObserver.observe(titleElement, {childList: true});
  }
});

onBeforeUnmount(() => titleObserver?.disconnect());
</script>

<template>
  <div ref="shell" class="causeway-app-shell" data-testid="petclinic-vue-application-shell">
    <a class="causeway-skip-link" href="#causeway-vue-route">Skip to main content</a>
    <cw-graphql-client data-causeway-shell-client :endpoint="viewer.endpoint" :executor.prop="graphQlExecutor">
      <header class="causeway-shell-header">
        <div class="causeway-shell-navbar">
          <a class="causeway-shell-brand" href="/vue/" aria-label="Pet Clinic home" @click.prevent="viewer.router.push('/')">
            <span class="causeway-shell-mark" aria-hidden="true">C</span>
            <span>Pet Clinic</span>
          </a>
          <cw-menubars />
          <form
            v-if="authenticationContext"
            method="post"
            :action="authenticationContext.logoutPath"
            data-causeway-authentication-logout
            hidden
          >
            <input
              type="hidden"
              :name="authenticationContext.csrfParameterName"
              :value="authenticationContext.csrfToken"
            >
          </form>
        </div>
      </header>
      <div
        data-causeway-route-loading
        class="causeway-route-loading"
        role="status"
        aria-live="polite"
        hidden
      >
        Loading page…
      </div>
      <div
        data-causeway-route-announcement
        class="causeway-visually-hidden"
        aria-live="polite"
        aria-atomic="true"
      />
      <cw-action-results
        id="causeway-result"
        data-causeway-shell-result
        class="causeway-shell-result"
        aria-label="Application action results"
        aria-live="polite"
        hidden
      />
      <main
        id="causeway-vue-route"
        data-causeway-router-view
        class="causeway-shell-main"
        aria-busy="false"
        tabindex="-1"
      >
        <RouterView v-slot="{ Component }">
          <component :is="Component" :key="route.path" />
        </RouterView>
      </main>
      <footer class="causeway-shell-footer">
        <span>Powered by Apache Causeway</span>
        <span>Vue viewer</span>
      </footer>
    </cw-graphql-client>
  </div>
</template>
