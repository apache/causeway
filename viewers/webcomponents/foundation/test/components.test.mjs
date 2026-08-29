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
  CausewayBreadcrumbsElement,
  CausewayCollectionColumnElement,
  CausewayCollectionElement,
  CausewayObjectContextElement,
  CausewayPropertyElement,
  CausewayValueRendererRegistry,
  configureCausewayFieldWidgets,
  defineCausewayWebComponents
} = await import('../src/index.mjs');

defineCausewayWebComponents();

test('breadcrumbs render accessible ancestor links, escaped current state and local errors', () => {
  const breadcrumbs = new CausewayBreadcrumbsElement();
  breadcrumbs.renderComponentState(state({
    data: {
      id: 'visit-1',
      logicalTypeName: 'petclinic.Visit',
      title: 'Checkup <today>',
      breadcrumbs: [
        {logicalTypeName: 'petclinic.PetOwner', id: 'owner-1', title: 'Mary & family'},
        {logicalTypeName: 'petclinic.Pet', id: 'pet-1', title: 'Basil'},
        {logicalTypeName: '', id: 'invalid', title: 'Malformed'}
      ]
    }
  }));

  assert.match(breadcrumbs.innerHTML, /<nav class="causeway-breadcrumbs" aria-label="Breadcrumb"/);
  assert.match(breadcrumbs.innerHTML, /<ol class="causeway-breadcrumbs-list">/);
  assert.match(breadcrumbs.innerHTML, /logical-type="petclinic\.PetOwner"/);
  assert.match(breadcrumbs.innerHTML, /object-id="owner-1"/);
  assert.match(breadcrumbs.innerHTML, /Mary &amp; family/);
  assert.match(breadcrumbs.innerHTML, /Basil/);
  assert.match(breadcrumbs.innerHTML, /aria-current="page">Checkup &lt;today&gt;/);
  assert.doesNotMatch(breadcrumbs.innerHTML, /Malformed/);

  breadcrumbs.renderComponentState(state({status: 'partial-error', errors: [{message: 'Breadcrumb cycle'}]}));
  assert.match(breadcrumbs.innerHTML, /role="alert"/);
  assert.match(breadcrumbs.innerHTML, /Breadcrumb cycle/);
  assert.doesNotMatch(breadcrumbs.innerHTML, /object-id=/);
});

test('breadcrumbs render a current-only landmark for a root object', () => {
  const breadcrumbs = new CausewayBreadcrumbsElement();
  breadcrumbs.renderComponentState(state({
    data: {id: 'owner-1', logicalTypeName: 'petclinic.PetOwner', title: 'Mary', breadcrumbs: []}
  }));
  assert.match(breadcrumbs.innerHTML, /aria-current="page">Mary/);
  assert.doesNotMatch(breadcrumbs.innerHTML, /<cw-object-link/);
});

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

test('property presentation attributes override canonical metadata with bounded values', () => {
  const property = new CausewayPropertyElement();
  property.id = 'firstName';
  property.setAttribute('label', 'Compatibility name');
  property.setAttribute('multiline', '3');
  property.named = 'Given name';
  property.describedAs = 'The given or first name of this customer';
  property.multiLine = 5;
  property.labelPosition = 'top';
  property.renderComponentState(state({
    descriptor: {description: 'Legacy field description', value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {
      hidden: false,
      disabled: null,
      datatype: 'String',
      metadata: {friendlyName: 'First name', description: 'Metadata description', multiLine: 4, labelPosition: 'LEFT'},
      get: 'Mary'
    }
  }));

  assert.equal(property.named, 'Given name');
  assert.equal(property.describedAs, 'The given or first name of this customer');
  assert.equal(property.multiLine, 5);
  assert.equal(property.labelPosition, 'TOP');
  assert.match(property.innerHTML, /data-label-position="TOP"/);
  assert.match(property.innerHTML, />Given name<\/span>/);
  assert.match(property.innerHTML, /causeway-property-description[^>]*>The given or first name of this customer<\/span>/);
  assert.match(property.innerHTML, /data-rows="5"/);
  assert.match(property.innerHTML, /data-multi-line="5"/);
  assert.doesNotMatch(property.innerHTML, /Metadata description|Compatibility name/);
});

test('effective multiline state is reflected on every rendered property shell', () => {
  const property = new CausewayPropertyElement();
  property.id = 'notes';
  property.setAttribute('multi-line', '5');

  property.renderComponentState(state({status: 'object-loading'}));
  assert.match(property.innerHTML, /data-multi-line="5"/);
  property.renderComponentState(state({status: 'partial-error', errors: [{message: 'Unavailable'}]}));
  assert.match(property.innerHTML, /data-multi-line="5"/);

  property.removeAttribute('multi-line');
  property.setAttribute('multiline', '3');
  property.renderComponentState(state({data: {
    hidden: false,
    disabled: null,
    datatype: 'String',
    metadata: {friendlyName: 'Notes', description: null, multiLine: 4, labelPosition: 'LEFT'},
    get: 'Details'
  }}));
  assert.match(property.innerHTML, /data-multi-line="3"/);

  property.removeAttribute('multiline');
  property.renderComponentState(state({data: {
    hidden: false,
    disabled: null,
    datatype: 'String',
    metadata: {friendlyName: 'Notes', description: null, multiLine: 4, labelPosition: 'LEFT'},
    get: 'Details'
  }}));
  assert.match(property.innerHTML, /data-multi-line="4"/);

  property.renderComponentState(state({data: {hidden: false, disabled: null, datatype: 'String', get: 'Details'}}));
  assert.doesNotMatch(property.innerHTML, /data-multi-line=/);
});

test('property metadata supplies descriptions and label positions while NONE suppresses visible presentation', () => {
  const property = new CausewayPropertyElement();
  property.id = 'emailAddress';
  const ready = state({
    descriptor: {description: 'Legacy description', value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {
      hidden: false,
      disabled: null,
      datatype: 'String',
      metadata: {friendlyName: 'Email address', description: 'Used for appointment reminders.', multiLine: null, labelPosition: 'TOP'},
      get: 'mary@example.com'
    }
  });
  property.renderComponentState(ready);
  assert.match(property.innerHTML, /data-label-position="TOP"/);
  assert.match(property.innerHTML, />Email address<\/span>/);
  assert.match(property.innerHTML, /causeway-property-description[^>]*>Used for appointment reminders\.<\/span>/);
  assert.match(property.innerHTML, /aria-describedby=/);

  property.labelPosition = 'NONE';
  property.renderComponentState(ready);
  assert.match(property.innerHTML, /data-label-position="NONE"/);
  assert.match(property.innerHTML, /causeway-visually-hidden">Email address<\/span>/);
  assert.doesNotMatch(property.innerHTML, /causeway-property-description/);
  assert.match(property.innerHTML, /aria-label="Email address"/);
});

test('invalid authored presentation values fall back to metadata and compatibility aliases', () => {
  const property = new CausewayPropertyElement();
  property.id = 'notes';
  property.setAttribute('multi-line', 'invalid');
  property.setAttribute('multiline', '500');
  property.setAttribute('label-position', 'sideways');
  property.renderComponentState(state({
    descriptor: {description: null, value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {
      hidden: false,
      disabled: null,
      datatype: 'String',
      metadata: {friendlyName: 'Notes', description: null, multiLine: 4, labelPosition: 'TOP'},
      get: 'Details'
    }
  }));
  assert.equal(property.multiLine, 50);
  assert.equal(property.labelPosition, '');
  assert.match(property.innerHTML, /data-label-position="TOP"/);
  assert.match(property.innerHTML, /data-rows="50"/);
  assert.match(property.innerHTML, /data-multi-line="50"/);
});

test('qualified standard values use a read-only field while native policy preserves the standard renderer', () => {
  const property = new CausewayPropertyElement();
  property.id = 'name';
  const ready = state({
    descriptor: {
      description: 'Department name',
      value: {typeRef: {kind: 'SCALAR', name: 'String'}}
    },
    data: {hidden: false, disabled: null, datatype: 'String', get: 'Classics'}
  });
  configureCausewayFieldWidgets({families: ['basic'], presentation: true});
  property.renderComponentState(ready);
  assert.equal(property.getAttribute('data-renderer'), 'vaadin-field-view');
  assert.match(property.innerHTML, /<cw-field-editor[^>]+data-mode="view"/);
  assert.match(property.innerHTML, /data-value="Classics"/);

  configureCausewayFieldWidgets({families: ['basic'], presentation: false});
  property.renderComponentState(ready);
  assert.equal(property.getAttribute('data-renderer'), 'scalar');
  assert.doesNotMatch(property.innerHTML, /data-mode="view"/);
  assert.match(property.innerHTML, /Classics/);
  configureCausewayFieldWidgets({families: ['basic', 'numeric', 'local-temporal'], presentation: true});
});

test('connected properties rerender immediately when field presentation policy changes', () => {
  configureCausewayFieldWidgets({families: ['basic'], presentation: true});
  const ready = state({
    descriptor: {description: 'Department name', value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {hidden: false, disabled: null, datatype: 'String', get: 'Classics'}
  });
  const property = new CausewayPropertyElement();
  property.id = 'name';
  property.context = {registerRequirement(requirement, listener) { listener(ready); return () => {}; }};
  document.body.appendChild(property);
  assert.match(property.innerHTML, /data-mode="view"/);
  configureCausewayFieldWidgets({families: [], presentation: false});
  assert.doesNotMatch(property.innerHTML, /data-mode="view"/);
  assert.equal(property.getAttribute('data-renderer'), 'scalar');
  document.body.removeChild(property);
  configureCausewayFieldWidgets({families: ['basic', 'numeric', 'local-temporal'], presentation: true});
});

test('application value renderer precedence remains authoritative even when its id resembles a standard renderer', () => {
  configureCausewayFieldWidgets({families: ['basic'], presentation: true});
  const registry = new CausewayValueRendererRegistry();
  registry.register({
    id: 'scalar',
    test: () => true,
    render: ({value}) => ({kind: 'application-code', html: `<strong data-application-renderer>${value}</strong>`})
  });
  const property = new CausewayPropertyElement();
  property.id = 'code';
  property.rendererRegistry = registry;
  property.renderComponentState(state({
    descriptor: {description: 'Department code', value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {hidden: false, disabled: null, datatype: 'String', get: 'SCI'}
  }));
  assert.equal(property.getAttribute('data-renderer'), 'scalar');
  assert.match(property.innerHTML, /data-application-renderer/);
  assert.doesNotMatch(property.innerHTML, /data-mode="view"/);
});

test('property presentation selects every qualified field family and preserves descriptions', () => {
  configureCausewayFieldWidgets({families: ['basic', 'numeric', 'local-temporal'], presentation: true});
  const cases = [
    ['text', 'String', {kind: 'SCALAR', name: 'String'}, 'Ada', 'basic', 'text-field'],
    ['boolean', 'Boolean', {kind: 'SCALAR', name: 'Boolean'}, true, 'basic', 'checkbox'],
    ['enum', 'DepartmentStatus', {kind: 'ENUM', name: 'DepartmentStatus'}, 'ACTIVE', 'basic', 'select'],
    ['decimal', 'rich__java_math_BigDecimal', {kind: 'SCALAR', name: 'rich__java_math_BigDecimal'}, '1.2300', 'numeric', 'text-field'],
    ['integer', 'Int', {kind: 'SCALAR', name: 'Int'}, 42, 'numeric', 'integer-field'],
    ['date', 'LocalDate', {kind: 'SCALAR', name: 'LocalDate'}, '2026-08-24', 'local-temporal', 'date-picker'],
    ['time', 'LocalTime', {kind: 'SCALAR', name: 'LocalTime'}, '13:14:15.123', 'local-temporal', 'time-picker'],
    ['dateTime', 'LocalDateTime', {kind: 'SCALAR', name: 'LocalDateTime'}, '2026-08-24T13:14:15.123', 'local-temporal', 'date-time-picker']
  ];
  for (const [id, datatype, typeRef, value, family, control] of cases) {
    const property = new CausewayPropertyElement();
    property.id = id;
    property.renderComponentState(state({
      descriptor: {description: `${id} description`, value: {typeRef}},
      data: {hidden: false, disabled: 'Read only', datatype, get: value}
    }));
    assert.equal(property.getAttribute('data-renderer'), 'vaadin-field-view', id);
    assert.match(property.innerHTML, new RegExp(`data-family="${family}"`), id);
    assert.match(property.innerHTML, new RegExp(`data-control="${control}"`), id);
    assert.match(property.innerHTML, /data-describedby=/, id);
  }
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

function state({status = 'ready', data = null, errors = [], descriptor = {description: 'Department name'}} = {}) {
  return Object.freeze({
    status,
    requirement: {kind: 'property', member: 'name'},
    descriptor,
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
