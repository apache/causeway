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
  COMPONENT_STATE_EVENT,
  CausewayActionElement,
  CausewayCollectionColumnElement,
  CausewayCollectionElement,
  CausewayObjectContextElement,
  CausewayPropertyElement,
  defineCausewayWebComponents
} = await import('../src/index.mjs');

defineCausewayWebComponents();

test('property renders accessible ready, disabled, hidden and error states', () => {
  const property = new CausewayPropertyElement();
  property.setAttribute('id', 'name');
  property.renderComponentState(state({data: {hidden: false, disabled: null, get: '<Classics>'}}));
  assert.match(property.innerHTML, /causeway-property-label/);
  assert.match(property.innerHTML, /&lt;Classics&gt;/);
  assert.match(property.innerHTML, /<output/);

  property.renderComponentState(state({data: {hidden: false, disabled: 'Locked', get: 'Classics'}}));
  assert.match(property.innerHTML, /data-disabled="true"/);
  assert.match(property.innerHTML, /aria-describedby=/);
  assert.match(property.innerHTML, /Locked/);

  property.renderComponentState(state({data: {hidden: true, disabled: null, get: 'Secret'}}));
  assert.equal(property.innerHTML, '');
  assert.equal(property.hidden, true);

  property.renderComponentState(state({status: 'partial-error', errors: [{message: 'Unreadable'}]}));
  assert.match(property.innerHTML, /role="alert"/);
  assert.match(property.innerHTML, /Unreadable/);
});

test('member-bearing elements use native identifiers without a member compatibility API', () => {
  const property = new CausewayPropertyElement();
  const action = new CausewayActionElement();
  const collection = new CausewayCollectionElement();
  const column = new CausewayCollectionColumnElement();

  property.id = 'firstName';
  action.id = 'updateName';
  collection.id = 'staffMembers';
  column.id = 'name';

  assert.equal(property.getAttribute('id'), 'firstName');
  assert.deepEqual(property.createRequirement(), {kind: 'property', member: 'firstName'});
  assert.deepEqual(action.createRequirement(), {kind: 'action', member: 'updateName'});
  assert.deepEqual(collection.createRequirement(), {kind: 'collection', member: 'staffMembers'});
  assert.equal(column.configuration.member, 'name');
  assert.equal(property.member, undefined); // intentional-obsolete-member-api

  const obsolete = new CausewayPropertyElement();
  obsolete.setAttribute('member', 'firstName'); // intentional-obsolete-member-api
  assert.equal(obsolete.id, '');
  assert.equal(obsolete.member, undefined); // intentional-obsolete-member-api
  assert.deepEqual(obsolete.createRequirement(), {kind: 'property', member: ''});
});

test('component lifecycle registers, changes and releases semantic requirements', () => {
  const registrations = [];
  const releases = [];
  const context = {
    registerRequirement(requirement, listener) {
      registrations.push(requirement);
      listener(state({data: {hidden: false, disabled: null, get: requirement.member}}));
      return () => releases.push(requirement);
    }
  };
  const property = new CausewayPropertyElement();
  property.context = context;
  property.setAttribute('id', 'name');
  document.body.appendChild(property);
  assert.deepEqual(registrations, [{kind: 'property', member: 'name'}]);
  property.setAttribute('id', 'code');
  assert.deepEqual(registrations.at(-1), {kind: 'property', member: 'code'});
  assert.equal(releases.length, 1);
  document.body.removeChild(property);
  assert.equal(releases.length, 2);
});

test('nearest nested object context answers the bubbling context request', () => {
  const outerContext = fakeContext('outer');
  const innerContext = fakeContext('inner');
  const outer = new CausewayObjectContextElement();
  const inner = new CausewayObjectContextElement();
  outer.context = outerContext;
  inner.context = innerContext;
  const property = new CausewayPropertyElement();
  property.setAttribute('id', 'name');
  outer.appendChild(inner);
  inner.appendChild(property);
  document.body.appendChild(outer);
  assert.equal(innerContext.registrations.length, 1);
  assert.equal(outerContext.registrations.length, 0);
  document.body.removeChild(outer);
});

test('components publish framework-neutral semantic state events', () => {
  const parent = document.createElement('div');
  const property = new CausewayPropertyElement();
  let eventDetail;
  parent.addEventListener(COMPONENT_STATE_EVENT, event => { eventDetail = event.detail; });
  parent.appendChild(property);
  document.body.appendChild(parent);
  property.acceptComponentState(state({data: {hidden: false, disabled: null, get: 'Classics'}}));
  assert.equal(eventDetail.element, property);
  assert.equal(eventDetail.state.status, 'ready');
  document.body.removeChild(parent);
});

function state({status = 'ready', data = null, errors = []} = {}) {
  return Object.freeze({
    status,
    requirement: {kind: 'property', member: 'name'},
    descriptor: {description: 'Department name'},
    data,
    errors,
    generation: 1
  });
}

function fakeContext(value) {
  return {
    state: {status: 'ready'},
    registrations: [],
    subscribe(listener) {
      listener(this.state);
      return () => {};
    },
    registerRequirement(requirement, listener) {
      this.registrations.push(requirement);
      listener(state({data: {hidden: false, disabled: null, get: value}}));
      return () => {};
    }
  };
}
