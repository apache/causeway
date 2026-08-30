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

import {
  applyServiceActionStates,
  CAUSEWAY_MENU_BARS_NAMESPACE,
  MAX_MENU_DIAGNOSTICS,
  menuPlanActionReferences,
  parseCausewayMenuBarsXml
} from '../src/menu-layout.mjs';
import {MAX_STRUCTURAL_XML_CHARACTERS} from '../src/structural-xml.mjs';
import {
  MENU_ACTION_STATES,
  MENU_BARS_WITH_INVALID_REFERENCE_XML,
  MENU_BARS_WITH_UNSUPPORTED_CONTENT_XML,
  MENU_BARS_XML
} from './fixtures/menu-layout-fixtures.mjs';

test('effective menu parser preserves semantic bars, ordering, text, and inert hints', () => {
  const parsed = parseCausewayMenuBarsXml(MENU_BARS_XML);

  assert.equal(parsed.usable, true);
  assert.deepEqual(Object.keys(parsed.plan.bars), ['primary', 'secondary', 'tertiary']);
  assert.equal(parsed.plan.bars.primary.menus[0].label, 'Samples & Objects');
  assert.equal(parsed.plan.bars.primary.menus[0].iconHint, 'fa-building');
  assert.equal(parsed.plan.bars.primary.menus[0].sections[0].label, 'Explore');
  const first = parsed.plan.bars.primary.menus[0].sections[0].actions[0];
  assert.equal(first.serviceLogicalTypeName, 'causeway.webcomponents.sample.SampleMenu');
  assert.equal(first.actionId, 'welcomeMessage');
  assert.equal(first.label, 'Welcome Message');
  assert.equal(first.description, 'Return a friendly greeting.');
  assert.equal(first.iconHint, 'fa-message');
  assert.equal(parsed.diagnostics.length, 0);
  assert.ok(Object.isFrozen(parsed.plan));
});

test('current action states omit hidden actions, retain disabled reasons, and collapse empty groups', () => {
  const parsed = parseCausewayMenuBarsXml(MENU_BARS_XML);
  const plan = applyServiceActionStates(parsed.plan, MENU_ACTION_STATES);
  const primaryActions = plan.bars.primary.menus[0].sections[0].actions;

  assert.deepEqual(primaryActions.map(action => action.actionId), ['welcomeMessage', 'disabledAction']);
  assert.equal(primaryActions[1].disabled, 'Available to administrators only.');
  assert.equal(plan.bars.secondary.menus[0].sections[0].actions[0].actionId, 'greet');
  assert.equal(plan.bars.secondary.menus[0].sections[0].actions[0].promptStyle, 'DIALOG_SIDEBAR');
  assert.equal(plan.bars.tertiary.menus[0].sections[0].actions[0].actionId, 'clearNotes');

  const emptyStates = new Map([...MENU_ACTION_STATES].map(([key, value]) => [key, {...value, hidden: true}]));
  const empty = applyServiceActionStates(parsed.plan, emptyStates);
  assert.equal(empty.bars.primary.menus.length, 0);
  assert.equal(empty.bars.secondary.menus.length, 0);
  assert.equal(empty.bars.tertiary.menus.length, 0);
});

test('references are extracted in effective document order', () => {
  const references = menuPlanActionReferences(parseCausewayMenuBarsXml(MENU_BARS_XML).plan);
  assert.deepEqual(references.map(action => `${action.serviceLogicalTypeName}#${action.actionId}`), [
    'causeway.webcomponents.sample.SampleMenu#welcomeMessage',
    'causeway.webcomponents.sample.SampleMenu#disabledAction',
    'causeway.webcomponents.sample.SampleMenu#hiddenAction',
    'causeway.webcomponents.sample.SampleMenu#greet',
    'causeway.webcomponents.sample.AdminMenu#clearNotes'
  ]);
});

test('unsupported local content and malformed references are bounded without losing valid menus', () => {
  const unsupported = parseCausewayMenuBarsXml(MENU_BARS_WITH_UNSUPPORTED_CONTENT_XML);
  assert.ok(unsupported.diagnostics.some(entry => entry.code === 'UNSUPPORTED_SECTION_CONTENT'));
  assert.equal(unsupported.plan.bars.primary.menus.length, 1);

  const invalid = parseCausewayMenuBarsXml(MENU_BARS_WITH_INVALID_REFERENCE_XML);
  assert.ok(invalid.diagnostics.some(entry => entry.code === 'INVALID_SERVICE_ACTION_REFERENCE'));
  assert.equal(menuPlanActionReferences(invalid.plan).length, 4);

  const noisy = MENU_BARS_XML.replace(
    '</mb:primary>',
    `${'<mb:unsupported/>'.repeat(MAX_MENU_DIAGNOSTICS + 10)}</mb:primary>`
  );
  const bounded = parseCausewayMenuBarsXml(noisy);
  assert.equal(bounded.diagnostics.length, MAX_MENU_DIAGNOSTICS);
  assert.equal(bounded.diagnostics.at(-1).code, 'MENU_DIAGNOSTICS_TRUNCATED');
});

test('shared structural parser rejects unsafe and oversized menu resources', () => {
  const attacks = [
    ['<!DOCTYPE menuBars [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><mb:menuBars xmlns:mb="' + CAUSEWAY_MENU_BARS_NAMESPACE + '">&xxe;</mb:menuBars>', 'MENU_XML_DECLARATION_FORBIDDEN'],
    ['<mb:menuBars xmlns:mb="' + CAUSEWAY_MENU_BARS_NAMESPACE + '"><script>alert(1)</script></mb:menuBars>', 'MENU_XML_EXECUTABLE_ELEMENT'],
    ['<mb:menuBars xmlns:mb="' + CAUSEWAY_MENU_BARS_NAMESPACE + '" onclick="alert(1)"/>', 'MENU_XML_EXECUTABLE_ATTRIBUTE'],
    ['<mb:menuBars xmlns:mb="' + CAUSEWAY_MENU_BARS_NAMESPACE + '"><mb:primary></mb:secondary></mb:menuBars>', 'MENU_XML_MISMATCHED_ELEMENT']
  ];
  for (const [xml, code] of attacks) {
    assert.throws(() => parseCausewayMenuBarsXml(xml), error => error.code === code);
  }
  assert.throws(
    () => parseCausewayMenuBarsXml(`<mb:menuBars xmlns:mb="${CAUSEWAY_MENU_BARS_NAMESPACE}">${' '.repeat(MAX_STRUCTURAL_XML_CHARACTERS)}</mb:menuBars>`),
    error => error.code === 'MENU_XML_TOO_LARGE'
  );
});
