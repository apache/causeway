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

const {document} = installDomShim();
const {
  CausewayElementName,
  CausewayUnreferencedActionsElement,
  CausewayUnreferencedCollectionsElement,
  CausewayUnreferencedMemberCoordinator,
  CausewayUnreferencedPropertiesElement,
  defineCausewayWebComponents
} = await import('../src/index.mjs');
defineCausewayWebComponents();

const tick = () => new Promise(resolve => queueMicrotask(resolve));
const settle = async () => {
  await tick();
  await tick();
  await tick();
};

function descriptor(id, kind, label = id) {
  return Object.freeze({id, kind, label});
}

function fakeContext(members) {
  const references = [];
  const listeners = new Set();
  return {
    state: Object.freeze({status: 'ready', data: {}}),
    describeObject: async () => ({members}),
    subscribe: listener => {
      listener(this?.state);
      return () => {};
    },
    subscribeMemberReferences(listener) {
      listeners.add(listener);
      listener(Object.freeze([...references]));
      return () => listeners.delete(listener);
    },
    registerRequirement(requirement, listener, {consumer = null} = {}) {
      const entry = {requirement, consumer};
      references.push(entry);
      for (const observer of listeners) observer(Object.freeze([...references]));
      listener?.({status: 'ready', data: {}});
      return () => {
        references.splice(references.indexOf(entry), 1);
        for (const observer of listeners) observer(Object.freeze([...references]));
      };
    },
    publish(revised) {
      references.splice(0, references.length, ...revised);
      for (const observer of listeners) observer(Object.freeze([...references]));
    },
    disconnect() {}
  };
}

function connect(candidate) {
  document.body.appendChild(candidate);
  return settle();
}

test('registers and exports every unreferenced member component', () => {
  assert.equal(globalThis.customElements.get(CausewayElementName.UNREFERENCED_PROPERTIES), CausewayUnreferencedPropertiesElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.UNREFERENCED_COLLECTIONS), CausewayUnreferencedCollectionsElement);
  assert.equal(globalThis.customElements.get(CausewayElementName.UNREFERENCED_ACTIONS), CausewayUnreferencedActionsElement);
});

test('components render authoritative remaining members in kind-specific owned structures', async () => {
  const members = new Map([
    ['name', descriptor('name', 'property', 'Name')],
    ['notes', descriptor('notes', 'property', 'Notes')],
    ['pets', descriptor('pets', 'collection', 'Pets')],
    ['visits', descriptor('visits', 'collection', 'Visits')],
    ['delete', descriptor('delete', 'action', 'Delete')]
  ]);
  const context = document.createElement('cw-object-context');
  context.context = fakeContext(members);
  const claimed = document.createElement('cw-property');
  claimed.setAttribute('id', 'name');
  claimed.hidden = true;
  const properties = document.createElement('cw-unreferenced-properties');
  properties.setAttribute('name', 'Remaining details');
  const collections = document.createElement('cw-unreferenced-collections');
  const actions = document.createElement('cw-unreferenced-actions');
  context.appendChild(claimed);
  context.appendChild(properties);
  context.appendChild(collections);
  context.appendChild(actions);

  await connect(context);

  assert.equal(properties.getAttribute('data-causeway-unreferenced-state'), 'ready');
  assert.match(properties.innerHTML, /cw-fieldset[^>]+name="Remaining details"/);
  assert.doesNotMatch(properties.innerHTML, /id="name"/);
  assert.match(properties.innerHTML, /id="notes"/);
  assert.doesNotMatch(properties.innerHTML, / editable/);
  assert.match(collections.innerHTML, /cw-tabgroup/);
  assert.match(collections.innerHTML, /<cw-tab name="Pets" selected>/);
  assert.match(collections.innerHTML, /<cw-column span="12">/);
  assert.match(collections.innerHTML, /id="visits"/);
  assert.match(actions.innerHTML, /role="group"/);
  assert.match(actions.innerHTML, /aria-label="Other actions"/);
  assert.match(actions.innerHTML, /<cw-action id="delete"/);

  properties.setAttribute('editable', '');
  assert.match(properties.innerHTML, /<cw-property id="notes" editable>/);
  document.body.removeChild(context);
});

test('a direct property catch-all contributes a conditional tab in authored order', async () => {
  const context = document.createElement('cw-object-context');
  context.context = fakeContext(new Map([['id', descriptor('id', 'property', 'Id')]]));
  const group = document.createElement('cw-tabgroup');
  group.setAttribute('name', 'Owner information');
  const identity = document.createElement('cw-tab');
  identity.setAttribute('name', 'Identity');
  identity.setAttribute('selected', '');
  identity.appendChild(document.createElement('cw-row'));
  const remaining = document.createElement('cw-unreferenced-properties');
  const metadata = document.createElement('cw-tab');
  metadata.setAttribute('name', 'Metadata');
  metadata.appendChild(document.createElement('cw-row'));
  group.appendChild(identity);
  group.appendChild(remaining);
  group.appendChild(metadata);
  context.appendChild(group);

  await connect(context);

  assert.equal(remaining.getAttribute('data-causeway-unreferenced-state'), 'ready');
  assert.deepEqual([...group.tablist.children].map(button => button.textContent), ['Identity', 'Other', 'Metadata']);
  assert.equal(remaining.getAttribute('role'), 'tabpanel');
  assert.equal(remaining.hidden, true);
  group.selectTab(remaining, {focus: true});
  assert.equal(group.selectedTab, remaining);
  assert.equal(document.activeElement, group.tabButtons.get(remaining));

  const explicit = document.createElement('cw-property');
  explicit.setAttribute('id', 'id');
  explicit.hidden = true;
  context.insertBefore(explicit, group);
  await settle();

  assert.equal(remaining.getAttribute('data-causeway-unreferenced-state'), 'empty');
  assert.deepEqual([...group.tablist.children].map(button => button.textContent), ['Identity', 'Metadata']);
  assert.equal(group.selectedTab, identity);
  assert.equal(document.activeElement, group.tabButtons.get(identity));
  assert.equal(remaining.getAttribute('role'), null);

  context.removeChild(explicit);
  await settle();
  assert.deepEqual([...group.tablist.children].map(button => button.textContent), ['Identity', 'Other', 'Metadata']);
  assert.equal(group.selectedTab, identity);
  document.body.removeChild(context);
});

test('coordinator subtracts exact kind and id, blocks reserved kinds, and settles deterministically', async () => {
  const members = new Map([
    ['shared', descriptor('shared', 'property')],
    ['delete', descriptor('delete', 'action')],
    ['later', descriptor('later', 'property')]
  ]);
  const context = fakeContext(members);
  const boundary = document.createElement('section');
  const explicit = document.createElement('span');
  const propertySink = document.createElement('span');
  const actionSink = document.createElement('span');
  boundary.appendChild(explicit);
  boundary.appendChild(propertySink);
  boundary.appendChild(actionSink);
  document.body.appendChild(boundary);
  const coordinator = new CausewayUnreferencedMemberCoordinator({boundary, context});
  const propertyStates = [];
  const actionStates = [];
  coordinator.registerSink('property', propertySink, state => propertyStates.push(state));
  coordinator.registerSink('action', actionSink, state => actionStates.push(state));
  context.publish([{requirement: {kind: 'property', member: 'shared'}, consumer: explicit}]);
  const source = coordinator.registerClaimSource(explicit, ['action']);

  await settle();
  assert.equal(propertyStates.at(-1).status, 'ready');
  assert.deepEqual(propertyStates.at(-1).members.map(member => member.id), ['later']);
  assert.equal(actionStates.at(-1).status, 'loading');

  source.resolve([{kind: 'action', id: 'not-authoritative'}]);
  await settle();
  assert.equal(actionStates.at(-1).status, 'ready');
  assert.deepEqual(actionStates.at(-1).members.map(member => member.id), ['delete']);

  source.resolve([{kind: 'action', id: 'delete'}]);
  await settle();
  assert.equal(actionStates.at(-1).status, 'empty');
  source.release();
  coordinator.disconnect();
  document.body.removeChild(boundary);
});

test('first document-order destination wins and generated and nested references do not consume parent allocation', async () => {
  const members = new Map([['notes', descriptor('notes', 'property')]]);
  const context = fakeContext(members);
  const boundary = document.createElement('section');
  const first = document.createElement('span');
  const second = document.createElement('span');
  const generatedDestination = document.createElement('cw-unreferenced-properties');
  const generated = document.createElement('cw-property');
  generated.setAttribute('id', 'notes');
  generatedDestination.appendChild(generated);
  const nested = document.createElement('cw-object-context');
  nested.context = fakeContext(new Map());
  const nestedProperty = document.createElement('cw-property');
  nestedProperty.setAttribute('id', 'notes');
  nested.appendChild(nestedProperty);
  boundary.appendChild(first);
  boundary.appendChild(second);
  boundary.appendChild(generatedDestination);
  boundary.appendChild(nested);
  document.body.appendChild(boundary);
  context.publish([
    {requirement: {kind: 'property', member: 'notes'}, consumer: generated},
    {requirement: {kind: 'property', member: 'notes'}, consumer: nestedProperty}
  ]);
  const coordinator = new CausewayUnreferencedMemberCoordinator({boundary, context});
  const firstStates = [];
  const secondStates = [];
  coordinator.registerSink('property', second, state => secondStates.push(state));
  coordinator.registerSink('property', first, state => firstStates.push(state));

  await settle();
  assert.equal(firstStates.at(-1).status, 'ready');
  assert.deepEqual(firstStates.at(-1).members.map(member => member.id), ['notes']);
  assert.equal(secondStates.at(-1).status, 'error');
  assert.equal(secondStates.at(-1).diagnostic.code, 'DUPLICATE_UNREFERENCED_DESTINATION');

  coordinator.disconnect();
  document.body.removeChild(boundary);
});

test('invokes an injected allocation scheduler without binding the coordinator', async () => {
  const context = fakeContext(new Map());
  const boundary = document.createElement('section');
  const sink = document.createElement('span');
  boundary.appendChild(sink);
  document.body.appendChild(boundary);
  let scheduled = 0;
  const schedule = function(callback) {
    assert.equal(this, undefined);
    scheduled += 1;
    queueMicrotask(callback);
  };
  const coordinator = new CausewayUnreferencedMemberCoordinator({boundary, context, schedule});
  coordinator.registerSink('property', sink, () => {});
  await settle();
  assert.ok(scheduled > 0);
  coordinator.disconnect();
  document.body.removeChild(boundary);
});

test('inventory failure and sink retirement fail closed without retaining ownership', async () => {
  const context = fakeContext(new Map());
  context.describeObject = async () => { throw new Error('unavailable'); };
  const boundary = document.createElement('section');
  const sink = document.createElement('span');
  boundary.appendChild(sink);
  document.body.appendChild(boundary);
  const coordinator = new CausewayUnreferencedMemberCoordinator({boundary, context});
  const states = [];
  const release = coordinator.registerSink('collection', sink, state => states.push(state));
  await settle();
  assert.equal(states.at(-1).status, 'error');
  release();
  coordinator.disconnect();
  assert.equal(coordinator.sinks.size, 0);
  document.body.removeChild(boundary);
});
