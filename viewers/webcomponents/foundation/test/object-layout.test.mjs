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
  CausewayGridError,
  createFallbackLayoutPlan,
  MAX_GRID_XML_CHARACTERS,
  MAX_GRID_XML_DEPTH,
  MAX_MULTI_LINE_ROWS,
  parseCausewayGridXml,
  renderObjectLayoutPlan
} from '../src/object-layout.mjs';
import {
  COMPLETE_OBJECT_GRID,
  GRID_NAMESPACES,
  MALFORMED_GRIDS,
  objectLayoutMembers,
  PARTIAL_OBJECT_GRID,
  UNREFERENCED_BEFORE_EXPLICIT_GRID,
  UNSUPPORTED_ONLY_GRID
} from './fixtures/object-layout-fixtures.mjs';

test('maps the supported Causeway grid subset into an immutable complete layout plan', () => {
  const result = parseCausewayGridXml(COMPLETE_OBJECT_GRID, {members: objectLayoutMembers()});
  assert.equal(result.usable, true);
  assert.equal(result.plan.source, 'grid');
  assert.deepEqual(memberIds(result.plan), [
    'changeName',
    'name', 'code', 'status', 'notes',
    'staffMembers', 'formerStaff'
  ]);
  assert.equal(semanticNodes(result.plan).filter(node => node.kind === 'header').length, 1);
  assert.equal(new Set(memberIds(result.plan)).size, memberIds(result.plan).length);
  assert.equal(result.diagnostics.length, 0);
  assert.equal(Object.isFrozen(result), true);
  assert.equal(Object.isFrozen(result.plan.regions), true);
});

test('reserves explicit references before earlier unreferenced markers allocate members', () => {
  const result = parseCausewayGridXml(UNREFERENCED_BEFORE_EXPLICIT_GRID, {members: objectLayoutMembers()});
  const columns = result.plan.regions[0].children;
  assert.deepEqual(memberIds({regions: columns[0].children}), ['code', 'status', 'notes']);
  assert.deepEqual(memberIds({regions: columns[1].children}), ['name']);
});

test('preserves recognized descendants and bounds diagnostics for partial layouts', () => {
  const result = parseCausewayGridXml(PARTIAL_OBJECT_GRID, {
    members: objectLayoutMembers(),
    maxDiagnostics: 5
  });
  assert.equal(result.usable, true);
  assert.deepEqual(memberIds(result.plan), ['name']);
  assert.equal(result.plan.regions[0].children[0].span, 12);
  assert.equal(result.diagnostics.length, 5);
  const codes = result.diagnostics.map(diagnostic => diagnostic.code);
  assert.ok(codes.includes('STALE_MEMBER_REFERENCE'));
  assert.ok(codes.includes('WRONG_MEMBER_KIND'));
  assert.ok(codes.includes('DUPLICATE_MEMBER_REFERENCE'));
  assert.ok(codes.includes('INVALID_COLUMN_SPAN') || codes.includes('UNSUPPORTED_LAYOUT_ATTRIBUTE'));
});

test('reports an unsupported-only grid as unusable without inventing member placement', () => {
  const result = parseCausewayGridXml(UNSUPPORTED_ONLY_GRID, {members: objectLayoutMembers()});
  assert.equal(result.usable, false);
  assert.deepEqual(memberIds(result.plan), []);
  assert.equal(result.diagnostics[0].code, 'UNSUPPORTED_LAYOUT_NODE');
});

test('rejects malformed, entity-bearing, executable, and unsafe processing markup', () => {
  for (const xml of MALFORMED_GRIDS) {
    assert.throws(
      () => parseCausewayGridXml(xml, {members: objectLayoutMembers()}),
      error => error instanceof CausewayGridError && error.code.startsWith('GRID_XML_'),
      xml
    );
  }
  assert.throws(
    () => parseCausewayGridXml(`<bs:grid ${GRID_NAMESPACES}>${' '.repeat(MAX_GRID_XML_CHARACTERS)}</bs:grid>`),
    error => error.code === 'GRID_XML_TOO_LARGE'
  );
  const nested = '<bs:row>'.repeat(MAX_GRID_XML_DEPTH + 1) + '</bs:row>'.repeat(MAX_GRID_XML_DEPTH + 1);
  assert.throws(
    () => parseCausewayGridXml(`<bs:grid ${GRID_NAMESPACES}>${nested}</bs:grid>`),
    error => error.code === 'GRID_XML_TOO_DEEP'
  );
});

test('accepts standard XML entities while rejecting unknown references', () => {
  const xml = `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col><cpt:fieldSet name="Identity &amp; details"><cpt:property id="name"/></cpt:fieldSet></bs:col></bs:row></bs:grid>`;
  const result = parseCausewayGridXml(xml, {members: objectLayoutMembers()});
  const group = semanticNodes(result.plan).find(node => node.kind === 'group');
  assert.equal(group.label, 'Identity & details');
  assert.throws(
    () => parseCausewayGridXml(`<bs:grid ${GRID_NAMESPACES}>&application;</bs:grid>`),
    error => error.code === 'GRID_XML_UNKNOWN_ENTITY'
  );
});

test('creates a deterministic semantic fallback modeled on the canonical Causeway grid', () => {
  const plan = createFallbackLayoutPlan(objectLayoutMembers());
  assert.equal(plan.source, 'fallback');
  assert.equal(semanticNodes(plan).filter(node => node.kind === 'header').length, 1);
  assert.deepEqual(memberIds(plan), [
    'changeName',
    'name', 'code', 'status', 'notes',
    'staffMembers', 'formerStaff'
  ]);
  const columns = semanticNodes(plan).filter(node => node.kind === 'column');
  assert.deepEqual(columns.map(column => column.span), [12, 4, 8]);
  assert.equal(semanticNodes(plan).find(node => node.memberId === 'formerStaff').label, 'Former Staff');
  assert.ok(semanticNodes(plan).filter(node => node.kind === 'member').every(node => typeof node.label === 'string'));
});

test('preserves associated actions and bounded multiline hints in semantic member composition', () => {
  const members = objectLayoutMembers();
  members.set('addStaff', {id: 'addStaff', kind: 'action'});
  const xml = `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col unreferencedActions="true"><cpt:property id="notes" multiLine="5" labelPosition="TOP"><cpt:named>Case notes</cpt:named><cpt:describedAs>Extended notes.</cpt:describedAs><cpt:action id="changeName"/></cpt:property><cpt:collection id="staffMembers"><cpt:action id="addStaff"/></cpt:collection></bs:col></bs:row></bs:grid>`;
  const result = parseCausewayGridXml(xml, {members});
  const notes = semanticNodes(result.plan).find(node => node.memberId === 'notes');
  const staff = semanticNodes(result.plan).find(node => node.memberId === 'staffMembers');
  assert.deepEqual(notes.presentation, {
    named: 'Case notes',
    describedAs: 'Extended notes.',
    labelPosition: 'TOP',
    multiLine: 5
  });
  assert.deepEqual(notes.children.map(node => node.memberId), ['changeName']);
  assert.deepEqual(staff.children.map(node => node.memberId), ['addStaff']);
  assert.equal(memberIds(result.plan).filter(id => id === 'changeName').length, 1);
  assert.equal(memberIds(result.plan).filter(id => id === 'addStaff').length, 1);

  const html = renderObjectLayoutPlan(result.plan, {editable: true});
  assert.match(html, /data-causeway-associated-member="notes"/);
  assert.match(html, /id="notes"[^>]*named="Case notes"[^>]*described-as="Extended notes\."[^>]*multi-line="5"[^>]*label-position="TOP"/);
  assert.match(html, /class="causeway-object-associated-actions"/);
  assert.match(html, /data-causeway-associated-member="staffMembers"/);
});

test('diagnoses invalid multiline hints and caps excessive row counts', () => {
  const invalid = parseCausewayGridXml(
    `<bs:grid ${GRID_NAMESPACES}><cpt:property id="notes" multiLine="many"/></bs:grid>`,
    {members: objectLayoutMembers()}
  );
  assert.ok(invalid.diagnostics.some(diagnostic => diagnostic.code === 'INVALID_MULTI_LINE'));
  assert.deepEqual(semanticNodes(invalid.plan).find(node => node.memberId === 'notes').presentation, {});

  const excessive = parseCausewayGridXml(
    `<bs:grid ${GRID_NAMESPACES}><cpt:property id="notes" multiLine="500"/></bs:grid>`,
    {members: objectLayoutMembers()}
  );
  assert.ok(excessive.diagnostics.some(diagnostic => diagnostic.code === 'MULTI_LINE_CAPPED'));
  assert.equal(semanticNodes(excessive.plan).find(node => node.memberId === 'notes').presentation.multiLine, MAX_MULTI_LINE_ROWS);
});

test('groups consecutive fallback actions with a responsive semantic wrapper', () => {
  const members = objectLayoutMembers();
  members.set('delete', {id: 'delete', kind: 'action'});
  const html = renderObjectLayoutPlan(createFallbackLayoutPlan(members));
  assert.match(html, /class="causeway-object-actions" data-causeway-action-group/);
  assert.match(html, /id="changeName"/);
  assert.match(html, /id="delete"/);
});

test('renders escaped light-DOM semantic children, accessible tabs, and editable properties', () => {
  const members = objectLayoutMembers();
  members.set('unsafe', {id: 'unsafe', kind: 'property'});
  const xml = `<bs:grid ${GRID_NAMESPACES}><bs:tabGroup><bs:tab name="A &amp; B"><cpt:property id="unsafe"><cpt:named>&lt;Unsafe&gt;</cpt:named></cpt:property></bs:tab></bs:tabGroup></bs:grid>`;
  const result = parseCausewayGridXml(xml, {members});
  const html = renderObjectLayoutPlan(result.plan, {idPrefix: 'test-object', editable: true});
  assert.match(html, /role="tablist"/);
  assert.match(html, /role="tab"/);
  assert.match(html, /role="tabpanel"/);
  assert.match(html, /<cw-property[^>]*id="unsafe"[^>]*named="&lt;Unsafe&gt;"[^>]* editable/);
  assert.doesNotMatch(html, /<Unsafe>/);
});

function semanticNodes(plan) {
  const result = [];
  const visit = nodes => {
    for (const node of nodes ?? []) {
      result.push(node);
      visit(node.children);
      visit(node.tabs);
    }
  };
  visit(plan.regions);
  return result;
}

function memberIds(plan) {
  return semanticNodes(plan)
    .filter(node => node.kind === 'member')
    .map(node => node.memberId);
}
