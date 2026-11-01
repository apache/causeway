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
import {onMounted, ref} from 'vue';
import {canonicalRouterObjectPath} from '../route-codec';
import {useCausewayViewer} from '../plugin';

const runtime = useCausewayViewer();
const page = ref<HTMLElement | null>(null);
const state = ref<'loading' | 'ready' | 'partial-error' | 'unsupported'>('loading');
const message = ref('Loading the application home page…');

function requestClient(element: HTMLElement): any {
  let client: any = null;
  element.dispatchEvent(new CustomEvent('causeway-graphql-client-request', {
    bubbles: true,
    composed: true,
    detail: {provide(candidate: any) { client ??= candidate; }}
  }));
  return client;
}

onMounted(async () => {
  const generation = ++runtime.state.routeGeneration;
  const client = page.value ? requestClient(page.value) : null;
  if (!client) {
    state.value = 'unsupported';
    message.value = 'Choose an application action to begin.';
    return;
  }
  try {
    const description = await client.describeApplicationEntry();
    if (generation !== runtime.state.routeGeneration) return;
    if (!description?.supported) {
      state.value = 'unsupported';
      message.value = 'Choose an application action to begin.';
      return;
    }
    const response = await client.readApplicationEntry({description});
    if (generation !== runtime.state.routeGeneration) return;
    const home = response?.data?.home;
    const metadata = home?.object?._meta;
    const logicalTypeName = metadata?.logicalTypeName ?? home?.logicalTypeName;
    const id = metadata?.id;
    const claim = {claimed: false, claim() { if (this.claimed) return false; this.claimed = true; return true; }};
    const policyContext = {router: runtime.router, basePath: runtime.basePath, shell: runtime.state.shell, routeGeneration: generation};
    const handled = await runtime.policies.home?.(response?.data, claim, policyContext);
    if (handled === true) claim.claim();
    if (generation !== runtime.state.routeGeneration || claim.claimed) return;
    if (home?.kind === 'OBJECT' && logicalTypeName && id) {
      await runtime.router.replace(canonicalRouterObjectPath({logicalTypeName, id}));
      return;
    }
    state.value = response?.errors?.length ? 'partial-error' : 'ready';
    message.value = 'Choose an application action to begin.';
  } catch (error) {
    if (generation !== runtime.state.routeGeneration) return;
    runtime.policies.error?.(error, {router: runtime.router, basePath: runtime.basePath, shell: runtime.state.shell, routeGeneration: generation});
    state.value = 'partial-error';
    message.value = 'The home page is unavailable; application menus remain available.';
  }
});
</script>

<template>
  <section ref="page" class="causeway-vue-route-page causeway-vue-status" :data-route-state="state" tabindex="-1">
    <h1>Welcome</h1>
    <p>{{ message }}</p>
  </section>
</template>
