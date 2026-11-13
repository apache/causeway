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

import {CausewayGraphQLClient} from '../src/graphql-client.mjs';
import {MenuBarsContextController, MenuBarsStatus} from '../src/menu-context-controller.mjs';
import {MENU_BARS_XML} from './fixtures/menu-layout-fixtures.mjs';
import {
  applicationEntryResponse,
  createMenuGraphQLExecutor,
  createMenuGraphQLTypes
} from './fixtures/menu-graphql-fixture.mjs';
import {waitFor} from './fixtures/rich-schema-fixture.mjs';

const ONE_SERVICE_XML = MENU_BARS_XML.replaceAll('causeway.webcomponents.sample.AdminMenu', 'causeway.webcomponents.sample.SampleMenu');

test('application-menu generation loads one no-store resource and groups action state by logical service', async () => {
  const executor = createMenuGraphQLExecutor();
  const client = new CausewayGraphQLClient({executor});
  const fetchCalls = [];
  const context = new MenuBarsContextController({
    client,
    fetchImpl: async (path, options) => {
      fetchCalls.push({path, options});
      return xmlResponse(ONE_SERVICE_XML);
    }
  });
  const states = [];
  context.subscribe(state => states.push(state.status));
  const state = await context.refresh();

  assert.equal(state.status, MenuBarsStatus.READY);
  assert.equal(fetchCalls.length, 1);
  assert.equal(fetchCalls[0].path, '/graphql/application/menu-bars');
  assert.equal(fetchCalls[0].options.credentials, 'same-origin');
  assert.equal(fetchCalls[0].options.cache, 'no-store');
  assert.equal(fetchCalls[0].options.redirect, 'error');
  assert.equal(executor.applicationCalls.length, 1);
  assert.equal(executor.serviceCalls.filter(call => call.operationName === 'CausewayReadServiceActionStates').length, 1);
  assert.deepEqual(state.plan.bars.primary.menus[0].sections[0].actions.map(action => action.actionId), [
    'welcomeMessage',
    'disabledAction'
  ]);
  assert.equal(state.plan.bars.primary.menus[0].sections[0].actions[1].disabled, 'Available to administrators only.');
  assert.ok(states.includes(MenuBarsStatus.APPLICATION_LOADING));
  assert.ok(states.includes(MenuBarsStatus.RESOURCE_LOADING));
  assert.ok(states.includes(MenuBarsStatus.SERVICE_LOADING));
  context.disconnect();
});

test('missing service types remain local partial errors and preserve unrelated bars', async () => {
  const executor = createMenuGraphQLExecutor();
  const context = new MenuBarsContextController({
    client: new CausewayGraphQLClient({executor}),
    fetchImpl: async () => xmlResponse(MENU_BARS_XML)
  });
  const state = await context.refresh();

  assert.equal(state.status, MenuBarsStatus.PARTIAL_ERROR);
  assert.equal(state.plan.bars.primary.menus.length, 1);
  assert.equal(state.plan.bars.secondary.menus.length, 1);
  assert.equal(state.plan.bars.tertiary.menus.length, 0);
  assert.ok(state.diagnostics.some(entry => entry.code === 'SERVICE_TYPE_UNAVAILABLE'));
  assert.equal(JSON.stringify(state.diagnostics).includes('Rich GraphQL object type'), false);
  context.disconnect();
});

test('omitted application menu capability produces unsupported state without fetching', async () => {
  const executor = createMenuGraphQLExecutor({types: createMenuGraphQLTypes({menuBarsAvailable: false})});
  let fetched = false;
  const context = new MenuBarsContextController({
    client: new CausewayGraphQLClient({executor}),
    fetchImpl: async () => {
      fetched = true;
      return xmlResponse(ONE_SERVICE_XML);
    }
  });
  const state = await context.refresh();

  assert.equal(state.status, MenuBarsStatus.UNSUPPORTED);
  assert.equal(state.diagnostics[0].code, 'MENU_BARS_UNAVAILABLE');
  assert.equal(fetched, false);
  context.disconnect();
});

test('explicit refresh rejects a superseded application response', async () => {
  let releaseFirst;
  let applicationNumber = 0;
  const firstGate = new Promise(resolve => releaseFirst = resolve);
  const executor = createMenuGraphQLExecutor({
    applicationEntries: [
      applicationEntryResponse({href: '/old-menu.xml'}),
      applicationEntryResponse({href: '/new-menu.xml'})
    ],
    delayApplication: async () => {
      applicationNumber += 1;
      if (applicationNumber === 1) {
        await firstGate;
      }
    }
  });
  const fetched = [];
  const context = new MenuBarsContextController({
    client: new CausewayGraphQLClient({executor}),
    fetchImpl: async path => {
      fetched.push(path);
      return xmlResponse(ONE_SERVICE_XML);
    }
  });

  const first = context.refresh();
  await waitFor(() => executor.applicationCalls.length === 1);
  const second = context.refresh();
  await second;
  assert.equal(context.state.resource.href, '/new-menu.xml');
  releaseFirst();
  await first;
  assert.equal(context.state.resource.href, '/new-menu.xml');
  assert.deepEqual(fetched, ['/new-menu.xml']);
  context.disconnect();
});

test('malformed resource failures are bounded and do not disclose response content', async () => {
  const context = new MenuBarsContextController({
    client: new CausewayGraphQLClient({executor: createMenuGraphQLExecutor()}),
    fetchImpl: async () => xmlResponse('<!DOCTYPE x [<!ENTITY secret SYSTEM "file:///secret">]><x>&secret;</x>')
  });
  const state = await context.refresh();

  assert.equal(state.status, MenuBarsStatus.TERMINAL_ERROR);
  assert.equal(state.diagnostics[0].code, 'MENU_XML_DECLARATION_FORBIDDEN');
  assert.equal(JSON.stringify(state).includes('file:///secret'), false);
  context.disconnect();
});

function xmlResponse(text) {
  return {
    ok: true,
    status: 200,
    headers: {get: name => name === 'content-type' ? 'application/xml' : null},
    text: async () => text
  };
}
