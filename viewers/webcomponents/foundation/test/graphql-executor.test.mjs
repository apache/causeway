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
import {createFetchGraphQLExecutor, GraphQLTransportError, normalizeExecutor} from '../src/graphql-executor.mjs';

test('default executor sends the standard GraphQL HTTP body and cancellation signal', async () => {
  const requests = [];
  const fetchImpl = async (endpoint, options) => {
    requests.push({endpoint, options});
    return response({data: {value: 3}});
  };
  const signal = new AbortController().signal;
  const executor = createFetchGraphQLExecutor({endpoint: '/graphql-test', fetchImpl, headers: {'x-test': 'yes'}});
  const result = await executor({
    document: 'query Example { value }',
    variables: {id: 7},
    operationName: 'Example',
    signal
  });
  assert.deepEqual(result, {data: {value: 3}});
  assert.equal(requests[0].endpoint, '/graphql-test');
  assert.equal(requests[0].options.method, 'POST');
  assert.equal(requests[0].options.signal, signal);
  assert.equal(requests[0].options.headers['content-type'], 'application/json');
  assert.equal(requests[0].options.headers['x-test'], 'yes');
  assert.deepEqual(JSON.parse(requests[0].options.body), {
    query: 'query Example { value }', variables: {id: 7}, operationName: 'Example'
  });
});

test('executor reports HTTP and invalid JSON failures', async () => {
  const httpExecutor = createFetchGraphQLExecutor({fetchImpl: async () => response({errors: []}, 500)});
  await assert.rejects(() => httpExecutor({document: 'query X { x }'}), error => {
    assert.equal(error instanceof GraphQLTransportError, true);
    assert.equal(error.status, 500);
    return true;
  });
  const invalidExecutor = createFetchGraphQLExecutor({fetchImpl: async () => ({
    ok: true, status: 200, async text() { return '<html>'; }
  })});
  await assert.rejects(() => invalidExecutor({document: 'query X { x }'}), GraphQLTransportError);
});

test('normalizes function and object executors', async () => {
  const functionExecutor = normalizeExecutor(async request => ({data: {request}}));
  assert.equal((await functionExecutor({document: 'x'})).data.request.document, 'x');
  const objectExecutor = normalizeExecutor({execute: async request => ({data: {request}})});
  assert.equal((await objectExecutor({document: 'y'})).data.request.document, 'y');
});

function response(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    async text() { return JSON.stringify(body); }
  };
}
