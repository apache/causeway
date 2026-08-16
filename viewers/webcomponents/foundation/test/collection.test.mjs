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
import {
  createRichSchemaFixtureExecutor,
  DEPARTMENT_LOGICAL_TYPE,
  DEPARTMENT_OBJECT_FIELD,
  graphQLObjectResponse,
  STAFF_LOGICAL_TYPE,
  STAFF_OBJECT_FIELD,
  waitFor
} from './fixtures/rich-schema-fixture.mjs';

const {document} = installDomShim();
const {
  captureDeclarativeCollectionColumns,
  CausewayCollectionColumnElement,
  CausewayCollectionElement,
  CausewayGraphQLClient,
  ObjectContextController
} = await import('../src/index.mjs');

function row({id = 'staff-1', name = 'Dr Ada', code = 'ADA'} = {}) {
  return {
    _meta: {id, logicalTypeName: STAFF_LOGICAL_TYPE, version: '3', title: name},
    name: {hidden: false, disabled: null, datatype: 'String', get: name},
    code: {hidden: false, disabled: null, datatype: 'String', get: code}
  };
}

function collectionResponse(rows) {
  return {
    data: {
      rich: {
        [DEPARTMENT_OBJECT_FIELD]: {
          staffMembers: {get: rows}
        }
      }
    }
  };
}

function staffDeltaResponse() {
  return {
    data: {
      rich: {
        [STAFF_OBJECT_FIELD]: {
          code: {hidden: false, disabled: null, datatype: 'String', get: 'ADA'}
        }
      }
    }
  };
}

test('captures declarative columns before browser upgrade reactions replace source children', () => {
  const collection = new CausewayCollectionElement();
  const column = new CausewayCollectionColumnElement();
  column.member = 'name';
  column.label = 'Name';
  column.setAttribute('data-testid', 'column-name');
  collection.appendChild(column);
  captureDeclarativeCollectionColumns({querySelectorAll: () => [collection]});
  collection.removeChild(column);
  document.body.appendChild(collection);
  assert.deepEqual(collection.columns, [{member: 'name', label: 'Name', testId: 'column-name'}]);
  document.body.removeChild(collection);
});

test('object context keeps action and inactive collection state in the primary projection', async () => {
  const executor = createRichSchemaFixtureExecutor({readResponses: [graphQLObjectResponse()]});
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  let actionState;
  let collectionState;
  context.registerRequirement({kind: 'action', member: 'changeName'}, state => { actionState = state; });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'}, state => { collectionState = state; });
  await waitFor(() => context.state.status === 'ready');
  assert.equal(actionState.data.hidden, false);
  assert.equal(collectionState.data.hidden, false);
  assert.equal(executor.readCalls.length, 1);
  assert.doesNotMatch(executor.readCalls[0].document, /staffMembers\s*\{\s*get/);
});

test('collection secondary reads are lazy, cached and hydrate row contexts', async () => {
  const staff = row();
  const executor = createRichSchemaFixtureExecutor({
    readResponses: [graphQLObjectResponse(), collectionResponse([staff]), staffDeltaResponse()]
  });
  const context = new ObjectContextController({
    client: new CausewayGraphQLClient({executor}),
    logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
    objectId: '42'
  });
  context.registerRequirement({kind: 'collection', member: 'staffMembers'});
  await waitFor(() => context.state.status === 'ready');
  assert.equal(executor.readCalls.length, 1);

  const loaded = await context.loadCollection({member: 'staffMembers', columns: ['name']});
  assert.equal(executor.readCalls.length, 2);
  assert.equal(loaded.data.get[0]._meta.id, 'staff-1');
  assert.equal(loaded.rowDescription.logicalTypeName, STAFF_LOGICAL_TYPE);
  assert.match(executor.readCalls[1].document, /staffMembers/);
  assert.match(executor.readCalls[1].document, /name\s*\{/);
  await context.loadCollection({member: 'staffMembers', columns: ['name']});
  assert.equal(executor.readCalls.length, 2);

  const rowContext = context.createHydratedRowContext(loaded.data.get[0], loaded.rowSelection);
  let nameState;
  rowContext.registerRequirement({kind: 'property', member: 'name'}, state => { nameState = state; });
  await waitFor(() => rowContext.state.status === 'ready');
  assert.equal(nameState.data.get, 'Dr Ada');
  assert.equal(executor.readCalls.length, 2, 'hydrated name must not repeat an object read');

  let codeState;
  rowContext.registerRequirement({kind: 'property', member: 'code'}, state => { codeState = state; });
  await waitFor(() => executor.readCalls.length === 3 && codeState?.status === 'ready');
  assert.equal(codeState.data.get, 'ADA');
});

test('collection component does not read until activated and renders declared columns', async () => {
  let requirementListener;
  let loadCount = 0;
  const context = {
    identity: {logicalTypeName: DEPARTMENT_LOGICAL_TYPE, id: '42'},
    registerRequirement(requirement, listener) {
      assert.deepEqual(requirement, {kind: 'collection', member: 'staffMembers'});
      requirementListener = listener;
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers', description: 'Staff members'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    async loadCollection() {
      loadCount += 1;
      return {
        descriptor: {id: 'staffMembers'},
        data: {get: [row()]},
        errors: [],
        rowSelection: {_meta: {id: true}, name: {get: true}}
      };
    },
    createHydratedRowContext() {
      return {disconnect() {}};
    }
  };
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.columns = [{member: 'name', label: 'Name'}, {member: 'code', label: 'Code'}];
  collection.context = context;
  document.body.appendChild(collection);
  assert.equal(loadCount, 0);
  assert.match(collection.innerHTML, /Load Staff Members/);

  assert.equal(collection.activate(), true);
  await waitFor(() => collection.collectionState.status === 'ready');
  assert.equal(loadCount, 1);
  assert.match(collection.innerHTML, /causeway-collection-table/);
  assert.match(collection.innerHTML, /Dr Ada/);
  assert.match(collection.innerHTML, /ADA/);

  requirementListener({
    status: 'ready',
    descriptor: {id: 'staffMembers', description: 'Staff members'},
    data: {hidden: true, disabled: null},
    errors: [],
    generation: 2
  });
  assert.equal(collection.hidden, true);
});

test('collection component cancels an in-flight secondary read when disconnected', async () => {
  let capturedSignal;
  const context = {
    registerRequirement(requirement, listener) {
      listener({
        status: 'ready',
        descriptor: {id: 'staffMembers', description: 'Staff members'},
        data: {hidden: false, disabled: null},
        errors: [],
        generation: 1
      });
      return () => {};
    },
    loadCollection({signal}) {
      capturedSignal = signal;
      return new Promise((resolve, reject) => signal.addEventListener('abort', () => {
        const error = new Error('Aborted');
        error.name = 'AbortError';
        reject(error);
      }, {once: true}));
    }
  };
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.active = true;
  collection.context = context;
  document.body.appendChild(collection);
  await waitFor(() => capturedSignal);
  document.body.removeChild(collection);
  assert.equal(capturedSignal.aborted, true);
});

test('collection component renders default object links, empty and partial-error states', () => {
  const collection = new CausewayCollectionElement();
  collection.member = 'staffMembers';
  collection.active = true;
  const baseState = {
    status: 'ready',
    descriptor: {id: 'staffMembers', description: 'Staff members'},
    data: {hidden: false, disabled: null},
    errors: [],
    generation: 1
  };
  collection.collectionState = {status: 'ready', data: {get: [row()]}, errors: []};
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /causeway-object-link/);

  collection.collectionState = {status: 'ready', data: {get: []}, errors: []};
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /No items/);

  collection.collectionState = {
    status: 'partial-error',
    data: {get: [row()]},
    errors: [{message: 'One row failed', path: ['staffMembers', 'get', 0]}]
  };
  collection.renderComponentState(baseState);
  assert.match(collection.innerHTML, /One row failed/);
});
