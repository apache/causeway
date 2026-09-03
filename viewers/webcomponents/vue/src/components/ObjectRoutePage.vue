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
import {computed, defineAsyncComponent, defineComponent, h, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {validateRouteBoundary} from '../boundary';
import {useCausewayViewer} from '../plugin';
import {isPageLoader} from '../registry';
import {canonicalRouteKey, parseCanonicalObjectPath} from '../route-codec';
import GenericObjectPage from './GenericObjectPage.vue';

const route = useRoute();
const runtime = useCausewayViewer();
const host = ref<HTMLElement | null>(null);
const boundaryError = ref<string | null>(null);
const identity = computed(() => parseCanonicalObjectPath(route.path, '/'));
const LoadingPage = defineComponent(() => () => h('section', {
  class: 'causeway-vue-route-page causeway-vue-status',
  'data-route-state': 'loading',
  tabindex: -1,
  role: 'status'
}, [h('h1', 'Loading page…')]));
const ErrorPage = defineComponent(() => () => h('section', {
  class: 'causeway-vue-route-page causeway-vue-status causeway-vue-status-danger',
  'data-route-state': 'terminal-error',
  tabindex: -1,
  role: 'alert'
}, [h('h1', 'Page unavailable'), h('p', 'The application page could not be loaded.') ]));
const routeKey = computed(() => canonicalRouteKey(identity.value));
const registration = computed(() => runtime.pages.get(identity.value.logicalTypeName));
const selectedPage = computed(() => {
  const selected = registration.value;
  if (!selected) return GenericObjectPage;
  if (!isPageLoader(selected)) return selected;
  return defineAsyncComponent({
    loader: async () => {
      const loaded = await selected();
      return 'default' in loaded ? loaded.default : loaded;
    },
    delay: 0,
    timeout: 30000,
    loadingComponent: LoadingPage,
    errorComponent: ErrorPage,
    onError(error, _retry, fail) {
      runtime.policies.error?.(error, {
        router: runtime.router,
        basePath: runtime.basePath,
        shell: runtime.state.shell,
        routeGeneration: runtime.state.routeGeneration
      });
      fail();
    }
  });
});

function landmarks() {
  const shell = runtime.state.shell;
  return {
    route: shell?.querySelector<HTMLElement>('[data-causeway-router-view]') ?? null,
    loading: shell?.querySelector<HTMLElement>('[data-causeway-route-loading]') ?? null,
    announcement: shell?.querySelector<HTMLElement>('[data-causeway-route-announcement]') ?? null
  };
}

function announce(message: string, generation: number): void {
  const target = landmarks().announcement;
  if (!target) return;
  target.textContent = '';
  requestAnimationFrame(() => {
    if (generation === runtime.state.routeGeneration && target.isConnected) target.textContent = message;
  });
}

function setBusy(busy: boolean): void {
  const shellLandmarks = landmarks();
  shellLandmarks.route?.setAttribute('aria-busy', String(busy));
  if (shellLandmarks.loading) shellLandmarks.loading.hidden = !busy;
}

async function validateBoundary(): Promise<void> {
  const generation = runtime.state.routeGeneration;
  boundaryError.value = null;
  await nextTick();
  if (generation !== runtime.state.routeGeneration || !host.value) return;
  const validation = validateRouteBoundary(host.value, identity.value);
  if (!validation.valid) {
    boundaryError.value = validation.classification ?? 'invalid';
    setBusy(false);
    announce('Page unavailable', generation);
  }
}

function onObjectState(rawEvent: Event): void {
  const generation = runtime.state.routeGeneration;
  const event = rawEvent as CustomEvent;
  const state = event.detail?.state;
  const page = (event.target as Element | null)?.closest<HTMLElement>('[data-causeway-route-page]');
  if (!state || !page || !host.value?.contains(page)) return;
  let status = String(state.status ?? 'terminal-error');
  if (status === 'terminal-error') {
    const code = state.errors?.[0]?.extensions?.classification
      ?? state.errors?.[0]?.extensions?.code
      ?? state.error?.code;
    if (code === 'NOT_FOUND' || code === 'ACCESS_DENIED') status = 'unavailable';
  }
  page.dataset.routeState = status;
  if (status === 'ready' || status === 'partial-error') {
    setBusy(false);
    const title = state.snapshot?.data?._meta?.title;
    if (title) document.title = String(title);
    for (const collection of page.querySelectorAll<HTMLElement>('cw-collection:not([active])')) {
      (collection as HTMLElement & {activate?: () => void}).activate?.();
    }
    announce(status === 'ready' ? 'Page ready' : 'Page ready with partial information', generation);
  } else if (status === 'terminal-error' || status === 'unavailable') {
    setBusy(false);
    announce('Page unavailable', generation);
  }
}

watch(() => route.fullPath, () => {
  runtime.state.routeGeneration += 1;
  setBusy(true);
  announce('Loading page', runtime.state.routeGeneration);
  void validateBoundary();
}, {immediate: true});
onMounted(async () => {
  host.value?.addEventListener('causeway-object-context-state-change', onObjectState);
  await validateBoundary();
  const contextElement = host.value?.querySelector<HTMLElement & {context?: {state?: unknown}}>(
    'cw-object-context[data-causeway-route-context]');
  if (contextElement?.context?.state) {
    contextElement.dispatchEvent(new CustomEvent('causeway-object-context-state-change', {
      bubbles: true,
      composed: true,
      detail: {state: contextElement.context.state, context: contextElement.context}
    }));
  }
  host.value?.querySelector<HTMLElement>('[data-causeway-route-page]')?.focus({preventScroll: true});
});
onBeforeUnmount(() => host.value?.removeEventListener('causeway-object-context-state-change', onObjectState));
</script>

<template>
  <div ref="host" class="causeway-vue-object-route" :data-route-key="routeKey">
    <section
      v-if="boundaryError"
      class="causeway-vue-route-page causeway-vue-status causeway-vue-status-danger"
      data-route-state="terminal-error"
      tabindex="-1"
      role="alert"
    >
      <h1>Page unavailable</h1>
      <p>The application page has an invalid semantic context boundary.</p>
    </section>
    <component
      :is="selectedPage"
      v-else
      :key="routeKey"
      :logical-type-name="identity.logicalTypeName"
      :object-id="identity.objectId"
      :route-key="routeKey"
      @vue:mounted="validateBoundary"
      @vue:updated="validateBoundary"
    />
  </div>
</template>
