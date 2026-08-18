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

import assert from 'node:assert/strict';
import test from 'node:test';
import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const {
  CausewayObjectElement,
  OBJECT_LAYOUT_DIAGNOSTIC_EVENT,
  OBJECT_LAYOUT_STATE_EVENT,
  defineCausewayWebComponents
} = await import('../src/index.mjs');
const {
  COMPLETE_OBJECT_GRID,
  MALFORMED_GRIDS,
  objectLayoutMembers
} = await import('./fixtures/object-layout-fixtures.mjs');
const {waitFor} = await import('./fixtures/rich-schema-fixture.mjs');

defineCausewayWebComponents();

test('requires the nearest authoritative object context', () => {
  const element = new CausewayObjectElement();
  document.body.appendChild(element);
  assert.equal(element.getAttribute('data-layout-state'), 'error');
  assert.match(element.innerHTML, /Place &lt;causeway-object&gt; beneath &lt;causeway-object-context&gt;/);
});

test('loads the authorized effective grid and generates established semantic children in light DOM', async () => {
  const context = createLayoutContext({xml: COMPLETE_OBJECT_GRID});
  const element = new CausewayObjectElement();
  element.context = context;
  element.editable = true;
  const states = [];
  element.addEventListener(OBJECT_LAYOUT_STATE_EVENT, event => states.push(event.detail));
  document.body.appendChild(element);
  await waitFor(() => element.getAttribute('data-layout-state') === 'ready');

  assert.equal(context.requirements.length, 1);
  assert.deepEqual(context.requirements[0], {kind: 'layout'});
  assert.deepEqual(context.resources, ['/graphql/object/type:id/_meta/grid']);
  assert.match(element.innerHTML, /<causeway-object-header/);
  assert.match(element.innerHTML, /<causeway-property[^>]*member="name"[^>]* editable/);
  assert.match(element.innerHTML, /<causeway-action[^>]*member="changeName"/);
  assert.match(element.innerHTML, /<causeway-collection[^>]*member="staffMembers"/);
  assert.match(element.innerHTML, /role="tablist"/);
  assert.equal(occurrences(element.innerHTML, 'member="name"'), 1);
  assert.equal(occurrences(element.innerHTML, 'member="changeName"'), 1);
  assert.equal(states.at(-1).status, 'ready');
  assert.equal(states.at(-1).source, 'grid');
});

test('uses canonical fallback without resource retrieval when explicitly configured', async () => {
  const context = createLayoutContext({xml: COMPLETE_OBJECT_GRID});
  const element = new CausewayObjectElement();
  element.context = context;
  element.layoutMode = 'fallback';
  document.body.appendChild(element);
  await waitFor(() => element.getAttribute('data-layout-state') === 'fallback');
  assert.deepEqual(context.resources, []);
  assert.match(element.innerHTML, /data-layout-source="fallback"/);
  assert.match(element.innerHTML, /data-span="4"/);
  assert.match(element.innerHTML, /data-span="8"/);
});

test('falls back with bounded redacted diagnostics when an effective resource is malformed', async () => {
  const context = createLayoutContext({xml: MALFORMED_GRIDS[1]});
  const element = new CausewayObjectElement();
  element.context = context;
  const diagnostics = [];
  element.addEventListener(OBJECT_LAYOUT_DIAGNOSTIC_EVENT, event => diagnostics.push(event.detail.diagnostic));
  document.body.appendChild(element);
  await waitFor(() => element.getAttribute('data-layout-state') === 'fallback');
  assert.equal(diagnostics.length, 1);
  assert.equal(diagnostics[0].code, 'GRID_XML_DECLARATION_FORBIDDEN');
  assert.doesNotMatch(JSON.stringify(diagnostics), /file:\/\/|passwd|ENTITY secret/);
  assert.match(element.innerHTML, /<causeway-object-header/);
});

test('retains a rendered plan across ordinary state notifications and refreshes only when requested', async () => {
  const context = createLayoutContext({xml: COMPLETE_OBJECT_GRID});
  const element = new CausewayObjectElement();
  element.context = context;
  document.body.appendChild(element);
  await waitFor(() => element.getAttribute('data-layout-state') === 'ready');
  const rendered = element.innerHTML;
  context.publishReady();
  await new Promise(resolve => setTimeout(resolve, 0));
  assert.equal(context.resources.length, 1);
  assert.equal(element.innerHTML, rendered);

  element.refreshLayout();
  await waitFor(() => context.resources.length === 2 && element.getAttribute('data-layout-state') === 'ready');
});

test('reports an actionable error for an unsupported layout mode', async () => {
  const context = createLayoutContext({xml: COMPLETE_OBJECT_GRID});
  const element = new CausewayObjectElement();
  element.context = context;
  element.layoutMode = 'custom';
  document.body.appendChild(element);
  await waitFor(() => element.getAttribute('data-layout-state') === 'error');
  assert.match(element.innerHTML, /Use &#39;auto&#39; or &#39;fallback&#39;/);
  assert.deepEqual(context.resources, []);
});

function createLayoutContext({xml}) {
  const listeners = new Set();
  const context = {
    identity: {logicalTypeName: 'university.dept.Department', id: '42'},
    description: {logicalTypeName: 'university.dept.Department', members: objectLayoutMembers()},
    requirements: [],
    resources: [],
    registerRequirement(requirement, listener) {
      this.requirements.push(requirement);
      listeners.add(listener);
      queueMicrotask(() => listener(readyLayoutState()));
      return () => listeners.delete(listener);
    },
    async describeObject() {
      return this.description;
    },
    async loadStructuralResource(path, {signal} = {}) {
      this.resources.push(path);
      if (signal?.aborted) {
        const error = new Error('Aborted');
        error.name = 'AbortError';
        throw error;
      }
      return {path, mediaType: 'application/xml', text: xml};
    },
    publishReady() {
      for (const listener of listeners) {
        listener(readyLayoutState());
      }
    }
  };
  return context;
}

function readyLayoutState() {
  return Object.freeze({
    status: 'ready',
    descriptor: null,
    data: {grid: '/graphql/object/type:id/_meta/grid', layout: null, cssClass: null},
    errors: Object.freeze([]),
    generation: 1
  });
}

function occurrences(value, search) {
  return value.split(search).length - 1;
}
