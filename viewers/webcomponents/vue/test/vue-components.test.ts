/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {readFile} from 'node:fs/promises';
import {resolve} from 'node:path';
import {mount} from '@vue/test-utils';
import {defineComponent, markRaw, nextTick} from 'vue';
import {createMemoryHistory, createRouter, RouterView} from 'vue-router';
import {afterEach, describe, expect, it} from 'vitest';
import {createCausewayVueViewer} from '../src/plugin';
import {createCausewayRouteRecords} from '../src/routes';

const customElement = (tag: string) => tag.startsWith('cw-');

afterEach(() => document.body.replaceChildren());

describe('Vue custom-element rendering', () => {
  it('preserves attributes properties slots native events and disconnect', async () => {
    const name = 'cw-vue-integration-probe';
    let connected = 0;
    let disconnected = 0;
    if (!customElements.get(name)) {
      customElements.define(name, class extends HTMLElement {
        connectedCallback() { connected += 1; }
        disconnectedCallback() { disconnected += 1; }
      });
    }
    const structured = {value: 42};
    const received: unknown[] = [];
    const Component = defineComponent({
      components: {},
      data: () => ({structured: markRaw(structured)}),
      template: `<cw-vue-integration-probe data-label="native" :payload.prop="structured">
        <span data-slot-child>authored child</span>
      </cw-vue-integration-probe>`
    });
    const wrapper = mount(Component, {global: {config: {compilerOptions: {isCustomElement: customElement}}}, attachTo: document.body});
    const probe = wrapper.element as HTMLElement & {payload?: unknown};
    probe.addEventListener('causeway-test-event', event => received.push((event as CustomEvent).detail));
    probe.dispatchEvent(new CustomEvent('causeway-test-event', {bubbles: true, composed: true, detail: structured}));
    expect(probe.getAttribute('data-label')).toBe('native');
    expect(probe.payload).toBe(structured);
    expect(probe.querySelector('[data-slot-child]')?.textContent).toContain('authored child');
    expect(received).toEqual([structured]);
    expect(connected).toBeGreaterThan(0);
    wrapper.unmount();
    expect(disconnected).toBeGreaterThan(0);
  });

  it('renders exact custom pages before the generic fallback', async () => {
    const CustomPage = defineComponent({
      props: ['logicalTypeName', 'objectId', 'routeKey'],
      template: `<section data-causeway-route-page data-testid="custom-page">
        <cw-object-context data-causeway-route-context :logical-type="logicalTypeName" :object-id="objectId">
          <p>{{ routeKey }}</p><cw-interaction-controller data-causeway-route-interactions />
        </cw-object-context>
      </section>`
    });
    const router = createRouter({history: createMemoryHistory(), routes: createCausewayRouteRecords()});
    const runtime = createCausewayVueViewer({
      router,
      endpoint: '/graphql',
      pages: {'petclinic.PetOwner': CustomPage}
    });
    await router.push('/object/petclinic.PetOwner/s_owner-1');
    await router.isReady();
    const wrapper = mount(defineComponent({components: {RouterView}, template: '<RouterView />'}), {
      global: {
        plugins: [router, runtime.plugin],
        config: {compilerOptions: {isCustomElement: customElement}}
      },
      attachTo: document.body
    });
    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(wrapper.find('[data-testid="custom-page"]').exists()).toBe(true);
    expect(wrapper.find('cw-object').exists()).toBe(false);
    const context = wrapper.find('cw-object-context');
    expect(context.attributes('logical-type')).toBe('petclinic.PetOwner');
    expect(context.attributes('object-id')).toBe('s_owner-1');

    await router.push('/object/petclinic.Pet/s_pet-1');
    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(wrapper.find('cw-object').exists()).toBe(true);
    expect(wrapper.find('[data-testid="custom-page"]').exists()).toBe(false);
    wrapper.unmount();
  });

  it('keeps the framework-neutral object component free of Vue imports', async () => {
    const source = await readFile(resolve(process.cwd(), '../foundation/src/object-element.mjs'), 'utf8');
    expect(source).not.toMatch(/vue-router|from ['"]vue['"]|pageRegistry|page-registry/);
  });
});
