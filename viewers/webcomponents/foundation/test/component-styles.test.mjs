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

test('disabled reasons use label-owned tooltips and string values align to logical start', async () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-disabled-tooltip \{[\s\S]*?cursor: help;[\s\S]*?position: relative;/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-disabled-tooltip::after \{[\s\S]*?content: attr\(data-tooltip\);/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-disabled-tooltip:hover::after,[\s\S]*?\.causeway-property-disabled-tooltip:focus-visible::after/);
  assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /causeway-property-disabled-indicator/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-value-string \{\s+text-align: start;/);
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-property-value-string \{\s+text-align: start;/);
});

test('collection descriptions remain quiet text directly below their labels', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /\.causeway-collection-description \{\s+margin: 0;\s+padding: var\(--causeway-space-2\) var\(--causeway-space-4\);\s+color: var\(--causeway-muted\);\s+font-size: var\(--causeway-font-size-sm\);/);
  assert.doesNotMatch(theme, /\.causeway-property-description,\s+\.causeway-collection-description/);
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

test('standard theme gives described multiline properties explicit responsive placement', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /:where\(cw-property\[multiline\]\) \.causeway-property-description \{\s+grid-column: 1;\s+grid-row: 2;/);
  assert.match(theme, /:where\(cw-property\[multiline\]\) \.causeway-property-value \{\s+grid-column: 2;\s+grid-row: 1 \/ span 2;/);
  assert.match(theme, /:where\(cw-property\[multiline\]\) \.causeway-property-edit \{\s+grid-column: 3;\s+grid-row: 1;\s+justify-self: start;/);
  assert.match(theme, /@container \(max-width: 32rem\)[\s\S]+:where\(cw-property\[multiline\]\) \.causeway-property-value \{\s+grid-row: 3;/);
  assert.match(theme, /@media \(max-width: 48rem\)[\s\S]+:where\(cw-property\[multiline\]\) \.causeway-property-edit \{\s+grid-column: 2;\s+grid-row: 3;/);
});
