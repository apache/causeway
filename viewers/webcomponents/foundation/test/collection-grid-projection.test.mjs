/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const {
  buildCausewayGridProjection,
  CausewayValueRendererRegistry,
  renderCausewayGridCell
} = await import('../src/index.mjs');

const scalar = {kind: 'SCALAR', name: 'String', ofType: null};
const object = {kind: 'OBJECT', name: 'example.Person', ofType: null};
const custom = {kind: 'OBJECT', name: 'example.Custom', ofType: null};
const descriptor = (id, typeRef) => ({id, value: {typeRef, typeDescription: null}});

function projection(overrides = {}) {
  const rows = [{
    _meta: {logicalTypeName: 'example.Person', id: '1', title: 'Ada'},
    name: {get: 'Ada', hidden: false, disabled: null},
    manager: {get: {_meta: {logicalTypeName: 'example.Person', id: '2', title: 'Grace'}}, hidden: false},
    nullable: {get: null, hidden: false},
    resource: {get: {name: 'cv.txt', mimeType: 'text/plain', chars: '/cv'}, hidden: false},
    unusual: {get: {nested: true}, hidden: false}
  }];
  const columns = [
    {member: 'name', label: 'Name', testId: 'name'},
    {member: 'manager', label: 'Manager'},
    {member: 'nullable', label: 'Nullable'},
    {member: 'resource', label: 'Resource'},
    {member: 'unusual', label: 'Unusual'}
  ];
  const members = new Map([
    ['name', descriptor('name', scalar)],
    ['manager', descriptor('manager', object)],
    ['nullable', descriptor('nullable', scalar)],
    ['resource', descriptor('resource', null)],
    ['unusual', descriptor('unusual', custom)]
  ]);
  return buildCausewayGridProjection({rows, columns, rowDescription: {members}, errors: [], ...overrides});
}

test('projection preserves identity first declarative order labels test identity and immutable rows', () => {
  const result = projection();
  assert.equal(result.supported, true);
  assert.deepEqual(result.columns.map(column => column.member), ['_meta', 'name', 'manager', 'nullable', 'resource', 'unusual']);
  assert.deepEqual(result.columns.map(column => column.label), ['Item', 'Name', 'Manager', 'Nullable', 'Resource', 'Unusual']);
  assert.equal(result.columns[1].testId, 'name');
  assert.equal(result.rows[0].key, 'example.Person:1');
  assert.equal(Object.isFrozen(result), true);
  assert.equal(Object.isFrozen(result.columns), true);
  assert.equal(Object.isFrozen(result.rows[0].cells), true);
  assert.equal(result.rows[0].cells[1].testId, 'name-row-0');
});

test('projection carries immutable preview payload without changing authoritative cells', () => {
  const preview = {row: {id: 'hydrated'}, presentation: {html: '<p>Preview</p>'}};
  const result = projection({previewForRow: () => preview});
  assert.deepEqual(result.rows[0].preview, preview);
  assert.equal(Object.isFrozen(result.rows[0].preview), true);
  assert.deepEqual(result.columns.map(column => column.member), ['_meta', 'name', 'manager', 'nullable', 'resource', 'unusual']);
});

test('projection retains scalar reference null resource and unsupported Causeway renderer output', () => {
  const result = projection();
  const cells = result.rows[0].cells;
  assert.equal(cells[1].rendererId, 'scalar');
  assert.match(cells[2].html, /cw-object-link/);
  assert.equal(cells[3].rendererId, 'null');
  assert.equal(cells[4].rendererId, 'clob');
  assert.equal(cells[5].rendererId, 'unsupported');
  assert.match(cells[0].html, /object-id="1"/);
});

test('explicit application renderer authority survives a standard-looking identifier', () => {
  const registry = new CausewayValueRendererRegistry();
  registry.register({
    id: 'scalar',
    test: state => state.descriptor?.id === 'name',
    render: state => ({kind: 'application', html: `<strong>${state.value}</strong>`})
  });
  const result = projection({
    rows: [{_meta: {logicalTypeName: 'example.Person', id: '1'}, name: {get: 'Ada'}}],
    columns: [{member: 'name', label: 'Name'}],
    rowDescription: {members: new Map([['name', descriptor('name', scalar)]])},
    rendererRegistry: registry
  });
  assert.equal(result.rows[0].cells[1].rendererId, 'scalar');
  assert.equal(result.rows[0].cells[1].standard, false);
  assert.equal(result.rows[0].cells[1].kind, 'application');
});

test('row-relative error hidden and disabled cells preserve bounded semantics', () => {
  const rows = [{
    _meta: {logicalTypeName: 'example.Person', id: '1'},
    name: {get: 'secret', hidden: true},
    manager: {get: null, hidden: false, disabled: 'Not authorized'}
  }];
  const columns = [{member: 'name'}, {member: 'manager', testId: 'manager'}];
  const members = new Map([
    ['name', descriptor('name', scalar)],
    ['manager', descriptor('manager', object)]
  ]);
  const result = buildCausewayGridProjection({
    rows,
    columns,
    rowDescription: {members},
    errors: [{message: 'Manager unavailable', path: ['staff', 0, 'manager']}]
  });
  assert.equal(result.rows[0].cells[1].hidden, true);
  assert.equal(result.rows[0].cells[1].html, '');
  assert.equal(result.rows[0].cells[2].error, true);
  assert.match(result.rows[0].cells[2].html, /Manager unavailable/);
  const disabled = buildCausewayGridProjection({rows, columns, rowDescription: {members}, errors: []});
  assert.equal(disabled.rows[0].cells[2].disabledReason, 'Not authorized');
  const root = document.createElement('div');
  renderCausewayGridCell(root, disabled.rows[0].cells[2]);
  assert.equal(root.getAttribute('aria-disabled'), 'true');
  assert.equal(root.getAttribute('title'), 'Not authorized');
});

test('recycled containers are cleaned before each current semantic cell', () => {
  const root = document.createElement('div');
  root.setAttribute('aria-disabled', 'true');
  root.setAttribute('aria-describedby', 'stale');
  root.setAttribute('role', 'alert');
  root.setAttribute('title', 'stale');
  root.className = 'causeway-error';
  root.hidden = true;
  root.innerHTML = 'stale value';
  const current = projection().rows[0].cells[1];
  assert.equal(renderCausewayGridCell(root, current), true);
  assert.equal(root.hidden, false);
  assert.equal(root.getAttribute('aria-disabled'), null);
  assert.equal(root.getAttribute('aria-describedby'), null);
  assert.equal(root.getAttribute('role'), null);
  assert.equal(root.getAttribute('title'), null);
  assert.equal(root.className, '');
  assert.equal(root.innerHTML, current.html);
});

test('hidden configured columns are absent and any unsupported descriptor rejects the whole projection', () => {
  const hidden = projection({columns: [{member: 'name', hidden: true}, {member: 'manager'}]});
  assert.deepEqual(hidden.columns.map(column => column.member), ['_meta', 'manager']);
  const missingDescriptor = projection({rowDescription: {members: new Map()}});
  assert.equal(missingDescriptor.supported, false);
  assert.equal(missingDescriptor.reason, 'projection-unavailable');
  assert.equal(missingDescriptor.columns.length, 0);
  const missingIdentity = projection({rows: [{name: {get: 'Ada'}}]});
  assert.equal(missingIdentity.supported, false);
});
