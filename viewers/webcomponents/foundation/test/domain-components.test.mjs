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

const {document, customElements} = installDomShim();
const {
  ACTION_REQUEST_EVENT,
  CAUSEWAY_ACTION_CONTROL,
  CAUSEWAY_FIELD_EDITOR,
  CausewayActionElement,
  CausewayElementName,
  CausewayObjectLinkElement,
  CausewayPropertyElement,
  CausewayValueElement,
  NAVIGATION_REQUEST_EVENT,
  configureCausewayActionWidgets,
  defineCausewayWebComponents
} = await import('../src/index.mjs');

defineCausewayWebComponents();

function readyState({descriptor, data}) {
  return Object.freeze({
    status: 'ready',
    descriptor,
    data,
    errors: Object.freeze([]),
    generation: 1
  });
}

test('registers the complete compact custom-element vocabulary without old aliases', () => {
  const publicNames = [
    'cw-graphql-client',
    'cw-object-context',
    'cw-object',
    'cw-object-header',
    'cw-breadcrumbs',
    'cw-property',
    'cw-value',
    'cw-object-link',
    'cw-action',
    'cw-action-results',
    'cw-parameter',
    'cw-interaction-controller',
    'cw-reference-editor',
    'cw-collection',
    'cw-peek',
    'cw-standalone-collection',
    'cw-collection-column',
    'cw-menubars',
    'cw-menubar-primary',
    'cw-menubar-secondary',
    'cw-menubar-tertiary'
  ];
  assert.deepEqual(Object.values(CausewayElementName), publicNames);
  assert.equal(CAUSEWAY_FIELD_EDITOR, 'cw-field-editor');
  assert.equal(CAUSEWAY_ACTION_CONTROL, 'cw-action-control');
  for (const name of [...publicNames, CAUSEWAY_FIELD_EDITOR, CAUSEWAY_ACTION_CONTROL]) {
    assert.equal(typeof customElements.get(name), 'function', name);
    assert.equal(customElements.get(name.replace(/^cw-/, 'causeway-')), undefined, name);
  }
});

test('object links publish cancelable bubbling and composed semantic navigation', () => {
  const link = new CausewayObjectLinkElement();
  link.target = {logicalTypeName: 'university.staff.StaffMember', id: 'staff-1', title: 'Dr Ada'};
  link.icon = '/graphql/object/university.staff.StaffMember:staff-1/_meta/icon';
  document.body.appendChild(link);
  let received;
  document.body.addEventListener(NAVIGATION_REQUEST_EVENT, event => {
    received = event;
  });
  assert.match(link.innerHTML, /<button type="button"[^>]*role="link"/);
  assert.match(link.innerHTML, /<img class="causeway-object-link-icon"[^>]*alt=""[^>]*aria-hidden="true"/);
  assert.match(link.innerHTML, /src="\/graphql\/object\/university\.staff\.StaffMember:staff-1\/_meta\/icon"/);
  assert.equal(link.activate(), true);
  assert.equal(received.bubbles, true);
  assert.equal(received.composed, true);
  assert.deepEqual(received.detail.target, {
    logicalTypeName: 'university.staff.StaffMember',
    id: 'staff-1',
    title: 'Dr Ada'
  });
  link.icon = null;
  assert.doesNotMatch(link.innerHTML, /causeway-object-link-icon/);
  link.disabled = true;
  assert.equal(link.activate(), false);
});

test('object links hide failed decorative icons without changing navigation', () => {
  const failedImage = document.createElement('img');
  const link = new CausewayObjectLinkElement();
  link.target = {logicalTypeName: 'petclinic.Pet', id: 'pet-1', title: 'Basil'};
  link.icon = '/missing/icon';
  link.querySelector = selector => selector === '.causeway-object-link-icon' ? failedImage : null;
  link.render();

  failedImage.dispatchEvent(new Event('error'));
  assert.equal(failedImage.hidden, true);
  assert.equal(failedImage.getAttribute('aria-hidden'), 'true');
  assert.equal(link.activate(), true);
});

test('actions render semantic states and publish requests only while enabled', async () => {
  let listener;
  const context = {
    identity: Object.freeze({logicalTypeName: 'university.dept.Department', id: '42'}),
    registerRequirement(requirement, candidate) {
      assert.deepEqual(requirement, {kind: 'action', member: 'changeName'});
      listener = candidate;
      return () => {};
    }
  };
  const action = new CausewayActionElement();
  action.id = 'changeName';
  action.context = context;
  document.body.appendChild(action);
  await Promise.resolve();
  let request;
  let requestCount = 0;
  action.addEventListener(ACTION_REQUEST_EVENT, event => {
    request = event;
    requestCount += 1;
  });
  listener({
    status: 'schema-loading',
    descriptor: {id: 'changeName', description: 'Changes the department name.'},
    data: null,
    errors: [],
    generation: 0
  });
  assert.match(action.innerHTML, /role="status"/);
  listener({
    status: 'terminal-error',
    descriptor: {id: 'changeName', description: 'Changes the department name.'},
    data: null,
    errors: [{message: 'Action state failed'}],
    generation: 0
  });
  assert.match(action.innerHTML, /role="alert"/);
  assert.match(action.innerHTML, /Action state failed/);
  listener(readyState({
    descriptor: {id: 'changeName', description: 'Changes the department name.'},
    data: {hidden: false, disabled: null}
  }));
  assert.match(action.innerHTML, /<cw-action-control/);
  assert.match(action.innerHTML, /<button type="button"/);
  assert.match(action.innerHTML, /causeway-action-description/);
  configureCausewayActionWidgets({enabled: false});
  assert.doesNotMatch(action.innerHTML, /<cw-action-control/);
  assert.match(action.innerHTML, /<button type="button"/);
  configureCausewayActionWidgets({enabled: true});
  assert.match(action.innerHTML, /<cw-action-control/);
  assert.equal(action.activate(), true);
  assert.equal(requestCount, 1);
  const internalControl = document.createElement('vaadin-button');
  action.appendChild(internalControl);
  internalControl.dispatchEvent(new Event('click', {bubbles: true, composed: true}));
  assert.equal(requestCount, 2);
  const descriptionControl = document.createElement('span');
  action.appendChild(descriptionControl);
  descriptionControl.dispatchEvent(new Event('click', {bubbles: true, composed: true}));
  assert.equal(requestCount, 2);
  assert.equal(request.bubbles, true);
  assert.equal(request.composed, true);
  assert.equal(request.cancelable, true);
  assert.equal(request.detail.actionId, 'changeName');
  assert.deepEqual(request.detail.identity, context.identity);

  listener(readyState({
    descriptor: {id: 'changeName', description: 'Change name'},
    data: {hidden: false, disabled: 'Approval required'}
  }));
  assert.match(action.innerHTML, /Approval required/);
  assert.match(action.innerHTML, /aria-disabled="true"/);
  assert.match(action.innerHTML, /aria-describedby=/);
  assert.equal(action.activate(), false);
  const disabledControl = document.createElement('vaadin-button');
  action.appendChild(disabledControl);
  disabledControl.dispatchEvent(new Event('click', {bubbles: true, composed: true}));
  assert.equal(requestCount, 2);

  listener(readyState({
    descriptor: {id: 'changeName', description: 'Change name'},
    data: {hidden: true, disabled: null}
  }));
  assert.equal(action.hidden, true);
  assert.equal(action.innerHTML, '');
});

test('properties delegate null, enum and object values to semantic renderers', () => {
  const property = new CausewayPropertyElement();
  property.id = 'chair';
  property.renderComponentState(readyState({
    descriptor: {
      id: 'chair',
      description: 'Department chair',
      value: {typeRef: {kind: 'OBJECT', name: 'rich__university_staff_StaffMember', ofType: null}}
    },
    data: {
      hidden: false,
      disabled: null,
      get: {_meta: {id: 'staff-1', logicalTypeName: 'university.staff.StaffMember', title: 'Dr Ada'}}
    }
  }));
  assert.equal(property.getAttribute('data-renderer'), 'object-reference');
  assert.match(property.innerHTML, /cw-object-link/);

  property.renderComponentState(readyState({
    descriptor: {
      id: 'notes',
      description: 'Notes',
      value: {typeRef: {kind: 'SCALAR', name: 'String', ofType: null}}
    },
    data: {hidden: false, disabled: null, get: null}
  }));
  assert.equal(property.getAttribute('data-renderer'), 'null');
  assert.match(property.innerHTML, /No value/);
});

test('the value element accepts structured state and reports its selected renderer', () => {
  const value = new CausewayValueElement();
  value.valueState = {
    value: 'ACTIVE',
    typeRef: {kind: 'ENUM', name: 'DepartmentStatus', ofType: null}
  };
  document.body.appendChild(value);
  assert.equal(value.getAttribute('data-renderer'), 'enum');
  assert.match(value.innerHTML, /ACTIVE/);
});
