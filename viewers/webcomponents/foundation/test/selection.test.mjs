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
import {RichSchemaNames} from '../src/schema-names.mjs';
import {
  buildCollectionWindowReadOperation,
  buildObjectReadOperation,
  deepMerge,
  differenceSelection,
  isSelectionEmpty,
  mergeSelections
} from '../src/selection.mjs';

const description = {
  generatedFieldName: 'university_dept_Department',
  generatedInputTypeName: 'rich__university_dept_Department__gqlv_input'
};
const identity = {logicalTypeName: 'university.dept.Department', id: '42'};

test('merges, deduplicates and differences structural selections', () => {
  const merged = mergeSelections(
    {_meta: {id: true, title: true}, name: {hidden: true}},
    {_meta: {id: true, version: true}, name: {get: true}}
  );
  assert.deepEqual(merged, {
    _meta: {id: true, title: true, version: true},
    name: {hidden: true, get: true}
  });
  assert.deepEqual(differenceSelection(merged, {_meta: {id: true}, name: {hidden: true}}), {
    _meta: {title: true, version: true},
    name: {get: true}
  });
  assert.equal(isSelectionEmpty(differenceSelection(merged, merged)), true);
});

test('builds a combined-rich object lookup operation', () => {
  const operation = buildObjectReadOperation({
    description,
    identity,
    selection: {_meta: {id: true, title: true}, name: {get: true}},
    schemaNames: new RichSchemaNames()
  });
  assert.match(operation.document, /query CausewayReadObject/);
  assert.match(operation.document, /rich \{/);
  assert.match(operation.document, /university_dept_Department\(object: \$object\)/);
  assert.deepEqual(operation.variables, {object: {id: '42'}});
  assert.deepEqual(operation.objectPath, ['rich', 'university_dept_Department']);
});

test('builds a bounded collection-window operation with semantic metadata', () => {
  const operation = buildCollectionWindowReadOperation({
    description,
    identity,
    member: 'staffMembers',
    rowSelection: {_meta: {id: true, logicalTypeName: true}, name: {get: true}},
    offset: 20,
    size: 10,
    schemaNames: new RichSchemaNames()
  });
  assert.match(operation.document, /query CausewayReadCollectionWindow/);
  assert.match(operation.document, /window\(offset: \$offset, size: \$size\)/);
  assert.match(operation.document, /returnedCount/);
  assert.match(operation.document, /totalCount/);
  assert.match(operation.document, /rows \{/);
  assert.deepEqual(operation.variables, {object: {id: '42'}, offset: 20, size: 10});
  assert.deepEqual(operation.objectPath, ['rich', 'university_dept_Department']);
});

test('builds a rich-only object lookup operation', () => {
  const operation = buildObjectReadOperation({
    description,
    identity,
    selection: {name: {get: true}},
    schemaNames: new RichSchemaNames({richRootField: ''})
  });
  assert.doesNotMatch(operation.document, /\n  rich \{/);
  assert.deepEqual(operation.objectPath, ['university_dept_Department']);
});

test('merges nested GraphQL snapshots without mutating inputs', () => {
  const previous = {_meta: {id: '42', title: 'Old'}, name: {get: 'Old'}};
  const next = {_meta: {title: 'New'}, code: {get: 'NEW'}};
  assert.deepEqual(deepMerge(previous, next), {
    _meta: {id: '42', title: 'New'},
    name: {get: 'Old'},
    code: {get: 'NEW'}
  });
  assert.equal(previous._meta.title, 'Old');
});
