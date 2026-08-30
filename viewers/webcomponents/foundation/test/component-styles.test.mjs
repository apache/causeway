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
import {readFile} from 'node:fs/promises';
import test from 'node:test';

import {CAUSEWAY_COMPONENT_STYLES} from '../src/component-styles.mjs';

test('external structural stylesheet remains synchronized with the installable styles', async () => {
  const external = await readFile(new URL('../src/component-styles.css', import.meta.url), 'utf8');
  const withoutLicense = external.slice(external.indexOf('cw-object {')).trim();
  assert.equal(withoutLicense, CAUSEWAY_COMPONENT_STYLES.trim());
});

test('property edit affordance remains a compact deterministic icon control', async () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-edit \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?padding: 0\.35rem;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-edit-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-property-edit \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?min-height: 0;/);
  assert.match(theme, /\.causeway-property-edit-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
});

test('property editor actions remain compact deterministic icon controls', async () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-editor-action \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?padding: 0\.35rem;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-editor-action-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-property-editor-action \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?min-height: 0;/);
  assert.match(theme, /\.causeway-property-editor-action-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
  assert.match(theme, /\.causeway-property-editor-actions button:first-child[\s\S]*?background: var\(--causeway-action-background\)/);
});

test('action icons retain component-owned spacing in native and Vaadin controls', () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /gap: var\(--causeway-action-icon-gap, 0\.75rem\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-action-control > vaadin-button > \.causeway-action-icon:first-child \{\s+margin-inline-end: var\(--causeway-action-icon-gap, 0\.75rem\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-action-control > vaadin-button > \.causeway-action-icon:last-child \{\s+margin-inline-start: var\(--causeway-action-icon-gap, 0\.75rem\);/);
});

test('member tooltips preserve sections, responsive bounds and pointer and focus access', async () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-disabled-tooltip \{[\s\S]*?cursor: help;[\s\S]*?position: relative;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip,\s+\.causeway-action-control-tooltip \{[\s\S]*?cursor: help;[\s\S]*?position: relative;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip::after,\s+\.causeway-action-control-tooltip::after \{[\s\S]*?content: attr\(data-tooltip\);[\s\S]*?max-inline-size: min\(24rem, calc\(100vw - 2rem\)\);[\s\S]*?white-space: pre-line;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip:hover::after,[\s\S]*?\.causeway-member-tooltip:focus-visible::after/);
  assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /causeway-property-disabled-indicator/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-value-string \{\s+text-align: start;/);
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-property-value-string \{\s+text-align: start;/);
  assert.match(theme, /\.causeway-action-description \{[\s\S]*?position: absolute !important;[\s\S]*?clip: rect\(0, 0, 0, 0\) !important;/);
});

test('collection descriptions remain quiet text directly below their labels', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-collection-description \{\s+margin: 0;\s+padding: var\(--causeway-space-2\) var\(--causeway-space-4\);\s+color: var\(--causeway-muted\);\s+font-size: var\(--causeway-font-size-sm\);/);
  assert.doesNotMatch(theme, /\.causeway-property-description,\s+\.causeway-collection-description/);
});

test('collection criteria, rows and paging retain compact aligned spacing', () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-content \{[\s\S]*?padding-block: var\(--causeway-collection-content-padding, 0\.75rem\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-search \{\s+align-items: center;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-search :is\(input, button\) \{\s+block-size: var\(--causeway-control-height, 2\.35rem\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection-grid \{[\s\S]*?font-size: var\(--causeway-font-size-sm, 0\.82rem\);[\s\S]*?--lumo-font-size-m: var\(--causeway-font-size-sm, 0\.82rem\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection-grid button\[data-causeway-collection-sort\] \{[\s\S]*?min-height: 0;[\s\S]*?padding: 0;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-sort-indicator \{[\s\S]*?margin-inline-start: 0\.25rem;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-table \{\s+border-collapse: collapse;\s+table-layout: fixed;/);
});

test('application native-control chrome does not cross into toolkit-owned slotted field inputs', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  const nativeInput = 'input:not([slot="input"])';
  const nativeSelect = 'select:not([slot="input"])';
  const nativeTextarea = 'textarea:not([slot="input"])';

  assert.ok(theme.includes(`button,\n${nativeInput},\n${nativeSelect},\n${nativeTextarea} {\n  max-width: 100%;\n  border: 1px solid var(--causeway-border-strong);`));
  assert.ok(theme.includes(`${nativeInput},\n${nativeSelect},\n${nativeTextarea} {\n  padding: 0.45rem 0.6rem;`));
  assert.ok(theme.includes(`${nativeTextarea} {\n  min-height: 6rem;\n  resize: vertical;`));
  assert.ok(theme.includes(`:where(a, button, ${nativeInput}, ${nativeSelect}, ${nativeTextarea}, [tabindex]):focus-visible`));
  assert.doesNotMatch(theme, /\ninput,\nselect,\ntextarea \{\n  max-width:/);
  assert.doesNotMatch(theme, /:where\(a, button, input, select, textarea, \[tabindex\]\):focus-visible/);
});

test('direct member associations wrap in semantic source and keyboard order', () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection\[data-causeway-action-group\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /> \.causeway-member-primary/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /> cw-action\[data-causeway-associated-action\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /flex-wrap: wrap/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /--causeway-associated-action-gap/);
});

test('property-associated actions align with the effective field column responsively', async () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\] \{[\s\S]*?display: grid;[\s\S]*?grid-template-columns: var\(--causeway-property-label-column/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 2;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /> cw-action\[data-causeway-associated-action\] > \.causeway-action \{\s+margin-block-start: var\(--causeway-associated-action-margin-block-start, 0\);/);
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /cw-property\[data-causeway-action-group\] \{\s+grid-template-columns: minmax\(7rem, var\(--causeway-label-width\)\) minmax\(0, 1fr\) auto;/);
  assert.match(theme, /cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 2 \/ -1;/);
  assert.match(theme, /@container \(max-width: 32rem\)[\s\S]*?cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 1 \/ -1;/);
  assert.match(theme, /@media \(max-width: 48rem\)[\s\S]*?cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 1 \/ -1;/);
});

test('standard theme gives effective multiline properties explicit responsive placement', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  const effectiveMultiline = '\\.causeway-property\\[data-multi-line\\]\\[data-label-position="LEFT"\\]';
  assert.match(theme, new RegExp(`${effectiveMultiline} \\.causeway-property-description \\{\\s+grid-column: 1;\\s+grid-row: 2;`));
  assert.match(theme, new RegExp(`${effectiveMultiline} \\.causeway-property-field \\{\\s+grid-column: 2;\\s+grid-row: 1 / span 2;`));
  assert.match(theme, new RegExp(`@container \\(max-width: 32rem\\)[\\s\\S]+${effectiveMultiline} \\.causeway-property-field \\{\\s+align-items: flex-start;\\s+grid-column: 1 / -1;\\s+grid-row: 3;`));
  assert.match(theme, new RegExp(`@media \\(max-width: 48rem\\)[\\s\\S]+${effectiveMultiline} \\.causeway-property-field \\{\\s+align-items: flex-start;\\s+grid-column: 1 / -1;\\s+grid-row: 3;`));
  assert.doesNotMatch(theme, /cw-property\[multiline\]/);
});
