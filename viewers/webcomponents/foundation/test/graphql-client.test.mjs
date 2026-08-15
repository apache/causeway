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
import {CausewayGraphQLClient, CausewaySchemaError} from '../src/graphql-client.mjs';
import {RichSchemaNames} from '../src/schema-names.mjs';
import {
  createRichSchemaFixtureExecutor,
  createRichSchemaTypes,
  DEPARTMENT_LOGICAL_TYPE,
  departmentObjectData
} from './fixtures/rich-schema-fixture.mjs';

test('targeted introspection classifies object members and caches the description', async () => {
  const executor = createRichSchemaFixtureExecutor();
  const client = new CausewayGraphQLClient({executor});
  const description = await client.describeObject(DEPARTMENT_LOGICAL_TYPE);
  assert.equal(description.generatedTypeName, 'rich__university_dept_Department');
  assert.equal(description.metadata.kind, 'metadata');
  assert.equal(description.members.get('name').kind, 'property');
  assert.equal(description.members.get('staffMembers').kind, 'collection');
  assert.equal(description.members.get('changeName').kind, 'action');
  assert.equal(description.members.get('name').fields.has('get'), true);
  assert.equal(description.types.has('rich__university_dept_StaffMember'), false);
  assert.ok(executor.introspectionCalls.length > 1);
  assert.equal(executor.introspectionCalls.every(call => Object.keys(call.variables).length === 1), true);
  const callCount = executor.introspectionCalls.length;
  assert.equal(await client.describeObject(DEPARTMENT_LOGICAL_TYPE), description);
  assert.equal(executor.introspectionCalls.length, callCount);
});

test('reports missing object and support type diagnostics', async () => {
  const missingObjectTypes = createRichSchemaTypes();
  missingObjectTypes.delete('rich__university_dept_Department');
  const missingObjectClient = new CausewayGraphQLClient({
    executor: createRichSchemaFixtureExecutor({types: missingObjectTypes})
  });
  await assert.rejects(
    () => missingObjectClient.describeObject(DEPARTMENT_LOGICAL_TYPE),
    error => error instanceof CausewaySchemaError && error.code === 'OBJECT_TYPE_NOT_FOUND'
  );

  const missingSupportTypes = createRichSchemaTypes();
  missingSupportTypes.delete('rich__university_dept_Department__name__gqlv_property');
  const missingSupportClient = new CausewayGraphQLClient({
    executor: createRichSchemaFixtureExecutor({types: missingSupportTypes})
  });
  await assert.rejects(
    () => missingSupportClient.describeObject(DEPARTMENT_LOGICAL_TYPE),
    error => error instanceof CausewaySchemaError && error.code === 'SUPPORT_TYPE_NOT_FOUND'
  );
});

test('reports an unsupported generated member wrapper', async () => {
  const types = createRichSchemaTypes();
  const objectType = types.get('rich__university_dept_Department');
  const unsupportedType = 'rich__university_dept_Department__mystery__gqlv_unknown';
  objectType.fields.push({name: 'mystery', description: null, args: [], type: {kind: 'OBJECT', name: unsupportedType}});
  types.set(unsupportedType, {kind: 'OBJECT', name: unsupportedType, description: null, fields: []});
  const client = new CausewayGraphQLClient({executor: createRichSchemaFixtureExecutor({types})});
  await assert.rejects(
    () => client.describeObject(DEPARTMENT_LOGICAL_TYPE),
    error => error instanceof CausewaySchemaError && error.code === 'UNRECOGNIZED_MEMBER_TYPE'
  );
});

test('reads an object and normalizes member-relative error paths', async () => {
  const executor = createRichSchemaFixtureExecutor({readResponses: [{
    data: {rich: {university_dept_Department: departmentObjectData()}},
    errors: [{message: 'Name warning', path: ['rich', 'university_dept_Department', 'name', 'get']}]
  }]});
  const client = new CausewayGraphQLClient({executor, schemaNames: new RichSchemaNames()});
  const description = await client.describeObject(DEPARTMENT_LOGICAL_TYPE);
  const result = await client.readObject({
    description,
    identity: {logicalTypeName: DEPARTMENT_LOGICAL_TYPE, id: '42'},
    selection: {name: {get: true}}
  });
  assert.equal(result.data.name.get, 'Classics');
  assert.deepEqual(result.errors[0].path, ['name', 'get']);
  assert.match(executor.readCalls[0].document, /name \{/);
});
