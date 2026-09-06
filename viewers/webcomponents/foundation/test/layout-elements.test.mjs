/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';
import {installDomShim} from './dom-shim.mjs';
import {GRID_NAMESPACES, objectLayoutMembers} from './fixtures/object-layout-fixtures.mjs';
import {waitFor} from './fixtures/rich-schema-fixture.mjs';

const {document} = installDomShim();
const {
  CausewayColumnElement,
  CausewayElementName,
  CausewayFieldsetElement,
  CausewayMetadataElement,
  CausewayRowElement,
  CausewaySemanticEvent,
  CausewayTabElement,
  CausewayTabgroupElement,
  defineCausewayWebComponents
} = await import('../src/index.mjs');
defineCausewayWebComponents();

const tick = () => new Promise(resolve => queueMicrotask(resolve));

function element(name, attributes = {}) {
  const result = document.createElement(name);
  for (const [attribute, value] of Object.entries(attributes)) {
    result.setAttribute(attribute, value);
  }
  return result;
}

function connect(candidate) {
  document.body.appendChild(candidate);
  return tick();
}

test('registers every public declarative layout component', () => {
  assert.equal(globalThis.customElements.get(CausewayElementName.FIELDSET), CausewayFieldsetElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.ROW), CausewayRowElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.COLUMN), CausewayColumnElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.TABGROUP), CausewayTabgroupElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.TAB), CausewayTabElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.METADATA), CausewayMetadataElement);
});

test('fieldset preserves ordered properties and excludes unsupported direct children', async () => {
  const fieldset = element('cw-fieldset');
  fieldset.name = 'Identity';
  const first = element('cw-property', {id: 'name'});
  const invalid = element('cw-action', {id: 'rename'});
  const second = element('cw-property', {id: 'code'});
  const diagnostics = [];
  fieldset.appendChild(first);
  fieldset.appendChild(invalid);
  fieldset.appendChild(second);
  fieldset.addEventListener(CausewaySemanticEvent.LAYOUT_COMPONENT_DIAGNOSTIC, event => diagnostics.push(event.detail.diagnostic));

  await connect(fieldset);

  assert.equal(fieldset.getAttribute('role'), 'group');
  assert.equal(fieldset.childNodes[0].textContent, 'Identity');
  assert.deepEqual(fieldset.children.filter(child => child.localName === 'cw-property'), [first, second]);
  assert.equal(invalid.hidden, true);
  assert.equal(invalid.hasAttribute('data-causeway-layout-invalid'), true);
  assert.equal(diagnostics[0].code, 'UNSUPPORTED_LAYOUT_CHILD');
  document.body.removeChild(fieldset);
});

test('row and column enforce nesting and normalize spans', async () => {
  const row = element('cw-row');
  const valid = element('cw-column');
  valid.span = 5;
  const invalidSpan = element('cw-column', {span: '13'});
  const invalidChild = element('cw-property', {id: 'name'});
  const diagnostics = [];
  invalidSpan.addEventListener(CausewaySemanticEvent.LAYOUT_COMPONENT_DIAGNOSTIC, event => diagnostics.push(event.detail.diagnostic));
  row.appendChild(valid);
  row.appendChild(invalidSpan);
  row.appendChild(invalidChild);

  await connect(row);

  assert.equal(valid.span, 5);
  assert.equal(valid.getAttribute('data-causeway-layout-span'), '5');
  assert.equal(invalidSpan.span, 12);
  assert.equal(invalidSpan.getAttribute('data-causeway-layout-span'), '12');
  assert.equal(invalidChild.hasAttribute('data-causeway-layout-invalid'), true);
  assert.equal(diagnostics[0].code, 'INVALID_COLUMN_SPAN');

  const fieldset = element('cw-fieldset');
  const collection = element('cw-collection');
  const metadata = element('cw-metadata');
  const unsupported = element('cw-tabgroup');
  valid.appendChild(fieldset);
  valid.appendChild(collection);
  valid.appendChild(metadata);
  valid.appendChild(unsupported);
  await tick();
  assert.equal(fieldset.hidden, false);
  assert.equal(collection.hidden, false);
  assert.equal(metadata.hidden, false);
  assert.equal(unsupported.hidden, true);
  document.body.removeChild(row);
});

test('tabgroup synchronizes selection, keyboard focus, disabled tabs and dynamic children', async () => {
  const group = element('cw-tabgroup');
  group.name = 'Object sections';
  const first = element('cw-tab');
  first.name = 'Identity';
  const second = element('cw-tab', {name: 'Details', selected: ''});
  const third = element('cw-tab', {name: 'Metadata'});
  first.appendChild(element('cw-row'));
  second.appendChild(element('cw-row'));
  third.appendChild(element('cw-row'));
  group.appendChild(first);
  group.appendChild(second);
  group.appendChild(third);

  await connect(group);

  assert.equal(group.tablist.getAttribute('role'), 'tablist');
  assert.equal(group.tablist.getAttribute('aria-label'), 'Object sections');
  assert.equal(first.hidden, true);
  assert.equal(second.hidden, false);
  assert.equal(group.tabButtons.get(second).getAttribute('aria-selected'), 'true');
  assert.equal(second.getAttribute('role'), 'tabpanel');
  assert.equal(second.getAttribute('aria-labelledby'), group.tabButtons.get(second).id);

  const right = new Event('keydown', {cancelable: true});
  right.key = 'ArrowRight';
  group.tabButtons.get(second).dispatchEvent(right);
  assert.equal(third.hidden, false);
  assert.equal(document.activeElement, group.tabButtons.get(third));

  const home = new Event('keydown', {cancelable: true});
  home.key = 'Home';
  group.tabButtons.get(third).dispatchEvent(home);
  assert.equal(first.hidden, false);

  second.setAttribute('disabled', '');
  await tick();
  assert.equal(group.tabButtons.get(second).hasAttribute('disabled'), true);
  group.setAttribute('dir', 'rtl');
  const rtlForward = new Event('keydown', {cancelable: true});
  rtlForward.key = 'ArrowLeft';
  group.tabButtons.get(first).dispatchEvent(rtlForward);
  assert.equal(third.hidden, false);

  group.removeChild(third);
  await tick();
  assert.equal(first.hidden, false);
  assert.equal(group.tabButtons.size, 2);
  document.body.removeChild(group);
});

test('tab and tabgroup reject invalid direct children without disabling valid tabs', async () => {
  const group = element('cw-tabgroup');
  const tab = element('cw-tab', {name: 'Valid'});
  const invalidGroupChild = element('cw-row');
  const invalidTabChild = element('cw-property');
  tab.appendChild(element('cw-row'));
  tab.appendChild(invalidTabChild);
  group.appendChild(tab);
  group.appendChild(invalidGroupChild);

  await connect(group);

  assert.equal(invalidGroupChild.hidden, true);
  assert.equal(invalidTabChild.hasAttribute('data-causeway-layout-invalid'), true);
  assert.equal(tab.hidden, false);
  assert.equal(group.tabButtons.size, 1);
  document.body.removeChild(group);
});

test('metadata renders authoritative actions in an accessible heading menu with bounded dismissal', async () => {
  const members = objectLayoutMembers();
  members.set('version', {id: 'version', kind: 'property'});
  members.set('logicalTypeName', {id: 'logicalTypeName', kind: 'property'});
  members.set('rebuildMetamodel', {id: 'rebuildMetamodel', kind: 'action'});
  const xml = `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col><cpt:fieldSet id="metadata" name="Metadata"><cpt:property id="version"/><cpt:property id="logicalTypeName"/><cpt:action id="rebuildMetamodel"/></cpt:fieldSet><cpt:fieldSet id="details"><cpt:property id="name"/></cpt:fieldSet></bs:col></bs:row></bs:grid>`;
  const requirements = [];
  let requirementListener;
  let resourceLoads = 0;
  const metadata = element('cw-metadata');
  metadata.context = {
    registerRequirement(requirement, listener) {
      requirements.push(requirement);
      requirementListener = listener;
      listener({status: 'ready', generation: 4, data: {grid: '/graphql/object/example:1/_meta/grid'}});
      return () => {};
    },
    describeObject: async () => ({members}),
    loadStructuralResource: async () => {
      resourceLoads += 1;
      return {text: xml};
    }
  };

  await connect(metadata);
  await waitFor(() => metadata.getAttribute('data-causeway-metadata-state') === 'ready');

  assert.deepEqual(requirements, [{kind: 'layout'}]);
  assert.match(metadata.innerHTML, /<fieldset[^>]*causeway-metadata-fieldset/);
  assert.match(metadata.innerHTML, /<cw-property[^>]*id="version"/);
  assert.match(metadata.innerHTML, /<cw-property[^>]*id="logicalTypeName"/);
  assert.match(metadata.innerHTML, /<details[^>]*data-causeway-metadata-actions/);
  assert.match(metadata.innerHTML, /<summary[^>]*aria-label="Metadata actions"[^>]*aria-expanded="false"/);
  assert.match(metadata.innerHTML, /<span aria-hidden="true">&#8942;<\/span>/);
  assert.match(metadata.innerHTML, /class="causeway-metadata-actions-menu" role="menu" aria-label="Metadata actions"/);
  assert.match(metadata.innerHTML, /<cw-action[^>]*id="rebuildMetamodel"/);
  assert.doesNotMatch(metadata.innerHTML, /id="name"/);
  assert.doesNotMatch(metadata.innerHTML, /<details[^>]*\sopen/);

  const rendered = metadata.innerHTML;
  requirementListener({status: 'object-loading', generation: 5, data: null});
  assert.equal(metadata.innerHTML, rendered);
  requirementListener({status: 'ready', generation: 5, data: {grid: '/graphql/object/example:1/_meta/grid'}});
  await tick();
  assert.equal(resourceLoads, 1);
  assert.equal(metadata.innerHTML, rendered);

  // The bounded DOM shim intentionally does not parse innerHTML, so attach the
  // equivalent controls to exercise the component-owned interaction lifecycle.
  const menu = element('details', {'data-causeway-metadata-actions': ''});
  const trigger = element('summary', {'aria-expanded': 'false'});
  menu.appendChild(trigger);
  menu.querySelector = selector => selector === 'summary' ? trigger : null;
  menu.querySelectorAll = selector => selector === 'cw-action' ? [{id: 'rebuildMetamodel'}] : [];
  metadata.appendChild(menu);
  metadata.querySelector = selector => selector === '[data-causeway-metadata-actions]' ? menu : null;
  metadata.bindMetadataMenu();
  menu.setAttribute('open', '');
  menu.dispatchEvent(new Event('toggle'));
  assert.equal(trigger.getAttribute('aria-expanded'), 'true');
  const escape = new Event('keydown', {bubbles: true, cancelable: true});
  escape.key = 'Escape';
  trigger.dispatchEvent(escape);
  assert.equal(menu.hasAttribute('open'), false);
  assert.equal(trigger.getAttribute('aria-expanded'), 'false');
  assert.equal(document.activeElement, trigger);

  menu.setAttribute('open', '');
  menu.dispatchEvent(new Event('toggle'));
  metadata.metadataOutsideListener({target: {closest: () => true}});
  assert.equal(menu.hasAttribute('open'), true);
  document.dispatchEvent(new Event('pointerdown'));
  assert.equal(menu.hasAttribute('open'), false);
  menu.setAttribute('open', '');
  menu.dispatchEvent(new Event('toggle'));
  metadata.metadataResultListener({detail: {actionId: 'unrelated'}});
  assert.equal(menu.hasAttribute('open'), true);
  metadata.metadataResultListener({detail: {actionId: 'rebuildMetamodel'}});
  await tick();
  assert.equal(menu.hasAttribute('open'), false);
  assert.equal(document.activeElement, trigger);
  menu.setAttribute('open', '');
  menu.dispatchEvent(new Event('toggle'));
  requirementListener({status: 'unsupported', generation: 6, data: null});
  assert.equal(metadata.metadataMenu, null);
  assert.doesNotMatch(metadata.innerHTML, /<cw-property|<cw-action/);
  document.body.removeChild(metadata);
});

test('metadata omits an empty action panel and fails closed when authority is unavailable', async () => {
  const members = objectLayoutMembers();
  members.set('version', {id: 'version', kind: 'property'});
  const xml = `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col><cpt:fieldSet id="metadata" name="Metadata"><cpt:property id="version"/></cpt:fieldSet></bs:col></bs:row></bs:grid>`;
  const metadata = element('cw-metadata');
  metadata.context = contextFor({members, xml});
  await connect(metadata);
  await waitFor(() => metadata.getAttribute('data-causeway-metadata-state') === 'ready');
  assert.doesNotMatch(metadata.innerHTML, /data-causeway-metadata-actions|Metadata actions/);
  assert.equal(metadata.metadataMenu, null);
  document.body.removeChild(metadata);

  const unavailable = element('cw-metadata');
  const diagnostics = [];
  unavailable.context = contextFor({members, xml, grid: null});
  unavailable.addEventListener(CausewaySemanticEvent.LAYOUT_COMPONENT_DIAGNOSTIC, event => diagnostics.push(event.detail.diagnostic));
  await connect(unavailable);
  await waitFor(() => unavailable.getAttribute('data-causeway-metadata-state') === 'error');
  assert.equal(diagnostics[0].code, 'METADATA_GRID_UNAVAILABLE');
  assert.doesNotMatch(unavailable.innerHTML, /<cw-property|<cw-action/);
  document.body.removeChild(unavailable);

  const partial = element('cw-metadata');
  partial.context = contextFor({members, xml, status: 'partial-error'});
  await connect(partial);
  await waitFor(() => partial.getAttribute('data-causeway-metadata-state') === 'error');
  assert.doesNotMatch(partial.innerHTML, /<cw-property|<cw-action/);
  document.body.removeChild(partial);
});

test('metadata ignores structural work completed after disconnection', async () => {
  const members = objectLayoutMembers();
  members.set('version', {id: 'version', kind: 'property'});
  let resolveResource;
  const resource = new Promise(resolve => { resolveResource = resolve; });
  const metadata = element('cw-metadata');
  metadata.context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', generation: 1, data: {grid: '/graphql/object/example:1/_meta/grid'}});
      return () => {};
    },
    describeObject: async () => ({members}),
    loadStructuralResource: async () => resource
  };
  await connect(metadata);
  document.body.removeChild(metadata);
  resolveResource({text: `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col><cpt:fieldSet id="metadata"><cpt:property id="version"/></cpt:fieldSet></bs:col></bs:row></bs:grid>`});
  await tick();
  assert.doesNotMatch(metadata.innerHTML, /<cw-property/);
});

function contextFor({members, xml, grid = '/graphql/object/example:1/_meta/grid', status = 'ready'}) {
  return {
    registerRequirement(_requirement, listener) {
      listener({status, generation: 1, data: {grid}});
      return () => {};
    },
    describeObject: async () => ({members}),
    loadStructuralResource: async () => ({text: xml})
  };
}
