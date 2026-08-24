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
  metadataSelectionForType,
  resultSelectionForType
} from '../src/interaction-operations.mjs';

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const named = (name, kind = 'OBJECT') => ({kind, name, ofType: null});
const field = (name, type) => ({name, description: null, args: [], type});
const type = (name, fields, kind = 'OBJECT') => ({name, kind, description: null, fields});

function typesWithMetadata(metadataFields) {
  return new Map([
    ['Example', type('Example', [field('_meta', named('ExampleMeta'))])],
    ['ExampleMeta', type('ExampleMeta', metadataFields.map(name => field(name, scalar('String'))))]
  ]);
}

test('metadata selection preserves all advertised versioned identity fields', () => {
  const types = typesWithMetadata(['id', 'logicalTypeName', 'title', 'version']);

  assert.deepEqual(metadataSelectionForType(named('Example'), types), {
    _meta: {id: true, logicalTypeName: true, title: true, version: true}
  });
  assert.deepEqual(resultSelectionForType(named('Example'), types), {
    _meta: {id: true, logicalTypeName: true, title: true, version: true}
  });
});

test('metadata selection omits absent version without weakening semantic identity', () => {
  const types = typesWithMetadata(['id', 'logicalTypeName', 'title']);

  assert.deepEqual(metadataSelectionForType(named('Example'), types), {
    _meta: {id: true, logicalTypeName: true, title: true}
  });
  assert.equal(JSON.stringify(resultSelectionForType(named('Example'), types)).includes('version'), false);
});

test('partial metadata remains bounded without inventing identity minimums', () => {
  const types = typesWithMetadata(['title']);

  assert.deepEqual(metadataSelectionForType(named('Example'), types), {_meta: {title: true}});
  assert.equal(JSON.stringify(resultSelectionForType(named('Example'), types)).includes('id'), false);
  assert.equal(JSON.stringify(resultSelectionForType(named('Example'), types)).includes('logicalTypeName'), false);
});

test('bounded described unions select concrete metadata through inline fragments', () => {
  const union = type('ExampleUnion', [], 'UNION');
  union.possibleTypes = [{kind: 'OBJECT', name: 'Versioned'}, {kind: 'OBJECT', name: 'Versionless'}];
  const types = new Map([
    ['ExampleUnion', union],
    ['Versioned', type('Versioned', [field('_meta', named('VersionedMeta'))])],
    ['VersionedMeta', type('VersionedMeta', ['id', 'logicalTypeName', 'version'].map(name => field(name, scalar('String'))))],
    ['Versionless', type('Versionless', [field('_meta', named('VersionlessMeta'))])],
    ['VersionlessMeta', type('VersionlessMeta', ['id', 'logicalTypeName', 'title'].map(name => field(name, scalar('String'))))]
  ]);

  assert.deepEqual(resultSelectionForType(named('ExampleUnion', 'UNION'), types), {
    __typename: true,
    __fragments: {
      Versioned: {_meta: {id: true, logicalTypeName: true, version: true}},
      Versionless: {_meta: {id: true, logicalTypeName: true, title: true}}
    }
  });
});

test('broad or incompletely described abstract types use typename-only projections', () => {
  const possibleTypes = Array.from({length: 9}, (_, index) => ({kind: 'OBJECT', name: `Type${index}`}));
  const broadType = type('BroadUnion', [], 'UNION');
  broadType.possibleTypes = possibleTypes;
  const broad = new Map([['BroadUnion', broadType]]);
  const incompleteType = type('IncompleteUnion', [], 'UNION');
  incompleteType.possibleTypes = [{kind: 'OBJECT', name: 'Missing'}];
  const incomplete = new Map([['IncompleteUnion', incompleteType]]);

  assert.deepEqual(resultSelectionForType(named('BroadUnion', 'UNION'), broad), {__typename: true});
  assert.deepEqual(resultSelectionForType(named('IncompleteUnion', 'UNION'), incomplete), {__typename: true});
});

test('missing descriptions and abstract types use typename-only projections', () => {
  const missing = new Map();
  const union = new Map([['ExampleUnion', type('ExampleUnion', [], 'UNION')]]);

  assert.equal(metadataSelectionForType(named('Missing'), missing), null);
  assert.deepEqual(resultSelectionForType(named('Missing'), missing), {__typename: true});
  assert.equal(metadataSelectionForType(named('ExampleUnion', 'UNION'), union), null);
  assert.deepEqual(resultSelectionForType(named('ExampleUnion', 'UNION'), union), {__typename: true});
  assert.equal(resultSelectionForType(scalar('String'), missing), null);
});
