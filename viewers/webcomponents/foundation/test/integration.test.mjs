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
  graphQLObjectResponse,
  waitFor
} from './fixtures/rich-schema-fixture.mjs';

const {document} = installDomShim();
await import('../src/index.mjs');

test('plain custom-element tree introspects once and coordinates one read per object context', async () => {
  const executor = createRichSchemaFixtureExecutor({
    readResponses: [graphQLObjectResponse({codeHidden: true}), graphQLObjectResponse({name: 'History'})]
  });
  const provider = document.createElement('cw-graphql-client');
  provider.executor = executor;

  const first = objectTree('42', ['name', 'code']);
  provider.appendChild(first.context);
  document.body.appendChild(provider);

  await waitFor(() => first.context.state?.status === 'ready');
  assert.equal(executor.readCalls.length, 1);
  assert.match(first.header.innerHTML, /Classics Department/);
  assert.match(first.properties[0].innerHTML, /Classics/);
  assert.equal(first.properties[1].innerHTML, '');
  assert.match(executor.readCalls[0].document, /_meta \{/);
  assert.match(executor.readCalls[0].document, /name \{/);
  assert.match(executor.readCalls[0].document, /code \{/);

  const introspectionCount = executor.introspectionCalls.length;
  const second = objectTree('43', ['name']);
  provider.appendChild(second.context);
  await waitFor(() => second.context.state?.status === 'ready');
  assert.equal(executor.readCalls.length, 2);
  assert.equal(executor.introspectionCalls.length, introspectionCount);
  assert.notEqual(first.context.context, second.context.context);
  assert.equal(first.context.context.client, second.context.context.client);

  document.body.removeChild(provider);
});

function objectTree(id, members) {
  const context = document.createElement('cw-object-context');
  context.setAttribute('logical-type', DEPARTMENT_LOGICAL_TYPE);
  context.setAttribute('object-id', id);
  const header = document.createElement('cw-object-header');
  context.appendChild(header);
  const properties = members.map(member => {
    const property = document.createElement('cw-property');
    property.setAttribute('member', member);
    context.appendChild(property);
    return property;
  });
  return {context, header, properties};
}
