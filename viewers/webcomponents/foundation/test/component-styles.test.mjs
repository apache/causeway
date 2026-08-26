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
  const withoutLicense = external.slice(external.indexOf('causeway-object {')).trim();
  assert.equal(withoutLicense, CAUSEWAY_COMPONENT_STYLES.trim());
});

test('direct member associations wrap in semantic source and keyboard order', () => {
  assert.match(CAUSEWAY_COMPONENT_STYLES, /causeway-property\[data-causeway-action-group\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /causeway-collection\[data-causeway-action-group\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /> \.causeway-member-primary/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /> causeway-action\[data-causeway-associated-action\]/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /flex-wrap: wrap/);
  assert.match(CAUSEWAY_COMPONENT_STYLES, /--causeway-associated-action-gap/);
});

test('standard theme gives described multiline properties explicit responsive placement', async () => {
  const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
  assert.match(theme, /:where\(causeway-property\[multiline\]\) \.causeway-property-description \{\s+grid-column: 1;\s+grid-row: 2;/);
  assert.match(theme, /:where\(causeway-property\[multiline\]\) \.causeway-property-value \{\s+grid-column: 2;\s+grid-row: 1 \/ span 2;/);
  assert.match(theme, /:where\(causeway-property\[multiline\]\) \.causeway-property-edit \{\s+grid-column: 3;\s+grid-row: 1;\s+justify-self: start;/);
  assert.match(theme, /@container \(max-width: 32rem\)[\s\S]+:where\(causeway-property\[multiline\]\) \.causeway-property-value \{\s+grid-row: 3;/);
  assert.match(theme, /@media \(max-width: 48rem\)[\s\S]+:where\(causeway-property\[multiline\]\) \.causeway-property-edit \{\s+grid-column: 2;\s+grid-row: 3;/);
});
