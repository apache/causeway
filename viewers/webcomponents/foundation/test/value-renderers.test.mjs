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
  CausewayValueRendererRegistry,
  renderCausewayValue
} from '../src/value-renderers.mjs';

const scalar = {kind: 'SCALAR', name: 'String', ofType: null};
const enumeration = {kind: 'ENUM', name: 'DepartmentStatus', ofType: null};
const object = {kind: 'OBJECT', name: 'rich__university_staff_StaffMember', ofType: null};

function descriptor(typeRef) {
  return {value: {typeRef, typeDescription: null}};
}

test('standard value renderers cover scalars, enums, nulls and object references', () => {
  const scalarResult = renderCausewayValue({value: '<Classics>', descriptor: descriptor(scalar)});
  assert.equal(scalarResult.rendererId, 'scalar');
  assert.match(scalarResult.html, /&lt;Classics&gt;/);

  const enumResult = renderCausewayValue({value: 'ACTIVE', descriptor: descriptor(enumeration)});
  assert.equal(enumResult.rendererId, 'enum');
  assert.match(enumResult.html, /ACTIVE/);

  const nullResult = renderCausewayValue({value: null, descriptor: descriptor(scalar)});
  assert.equal(nullResult.rendererId, 'null');
  assert.match(nullResult.html, /No value/);

  const objectResult = renderCausewayValue({
    value: {_meta: {id: 'staff-1', logicalTypeName: 'university.staff.StaffMember', title: 'Dr Ada'}},
    descriptor: descriptor(object)
  });
  assert.equal(objectResult.rendererId, 'object-reference');
  assert.match(objectResult.html, /causeway-object-link/);
  assert.match(objectResult.html, /staff-1/);
});

test('standard value renderers cover Blob and Clob resource representations', () => {
  const blob = renderCausewayValue({
    value: {name: 'prospectus.pdf', mimeType: 'application/pdf', bytes: '/blobBytes'}
  });
  assert.equal(blob.rendererId, 'blob');
  assert.match(blob.html, /href="\/blobBytes"/);
  assert.match(blob.html, /application\/pdf/);

  const clob = renderCausewayValue({
    value: {name: 'history.txt', mimeType: 'text\/plain', chars: '/clobChars'}
  });
  assert.equal(clob.rendererId, 'clob');
  assert.match(clob.html, /href="\/clobChars"/);
});

test('application renderers override standards deterministically and can be released', () => {
  const registry = new CausewayValueRendererRegistry();
  const release = registry.register({
    id: 'department-code',
    test: state => state.descriptor?.id === 'code',
    render: state => ({kind: 'application', html: `<strong>${state.value}</strong>`})
  });
  const state = {value: 'CLA', descriptor: {...descriptor(scalar), id: 'code'}};
  assert.equal(registry.render(state).rendererId, 'department-code');
  release();
  assert.equal(registry.render(state).rendererId, 'scalar');
});

test('unsupported values expose an introspected diagnostic type', () => {
  const result = renderCausewayValue({
    value: {nested: {value: 'unknown'}},
    descriptor: descriptor({kind: 'OBJECT', name: 'ArbitraryValue', ofType: null})
  });
  assert.equal(result.rendererId, 'unsupported');
  assert.match(result.html, /ArbitraryValue/);
});
