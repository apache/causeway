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

import test from 'node:test';
import assert from 'node:assert/strict';

import {
  applyAuthenticationMenuPolicy,
  csrfHeaders,
  isExcludedAction,
  isUnsafeMethod,
  loginDestination,
  readAuthenticationMetadata
} from '../src/main/resources/META-INF/resources/causeway-htmx/authentication-policy.mjs';

const metadata = {
  'causeway-auth-login': '/htmx/login',
  'causeway-auth-csrf-header': 'X-CSRF-TOKEN',
  'causeway-auth-csrf-parameter': '_csrf',
  'causeway-auth-csrf-token': 'bounded-token',
  'causeway-auth-excluded-actions': 'causeway.security.LogoutMenu#logout'
};

function documentWith(values = metadata, buttons = []) {
  return {
    querySelector(selector) {
      const name = selector.match(/meta\[name="(.+)"\]/)?.[1];
      return name && values[name] !== undefined ? {content: values[name]} : null;
    },
    querySelectorAll() {
      return buttons;
    }
  };
}

test('authentication metadata is activated only as one complete bounded contract', () => {
  const authentication = readAuthenticationMetadata(documentWith());
  assert.equal(authentication.loginPath, '/htmx/login');
  assert.equal(authentication.csrfParameterName, '_csrf');
  assert.deepEqual(csrfHeaders(authentication), {'X-CSRF-TOKEN': 'bounded-token'});
  assert.deepEqual(csrfHeaders(null), {});

  assert.equal(readAuthenticationMetadata(documentWith({...metadata, 'causeway-auth-csrf-token': ''})), null);
});

test('CSRF policy distinguishes every unsafe method including GraphQL POST', () => {
  assert.equal(isUnsafeMethod('GET'), false);
  assert.equal(isUnsafeMethod('head'), false);
  assert.equal(isUnsafeMethod('POST'), true);
  assert.equal(isUnsafeMethod('PATCH'), true);
  assert.equal(isUnsafeMethod('DELETE'), true);
});

test('reauthentication preserves only the current route as an encoded relative continuation', () => {
  const authentication = readAuthenticationMetadata(documentWith());
  assert.equal(
    loginDestination(authentication, {pathname: '/htmx/object/petclinic.Pet/1', search: '?tab=visits'}),
    '/htmx/login?continue=%2Fhtmx%2Fobject%2Fpetclinic.Pet%2F1%3Ftab%3Dvisits');
});

test('legacy exclusion matches only the exact framework service and member', () => {
  const authentication = readAuthenticationMetadata(documentWith());
  assert.equal(isExcludedAction(authentication, {
    serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout'
  }), true);
  assert.equal(isExcludedAction(authentication, {
    serviceLogicalTypeName: 'petclinic.LogoutMenu', actionId: 'logout'
  }), false);
  assert.equal(isExcludedAction(authentication, {
    serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logoutAll'
  }), false);
});

test('menu policy removes the exact legacy action and preserves similarly named domain actions', () => {
  const authentication = readAuthenticationMetadata(documentWith());
  const removed = [];
  const button = (serviceLogicalTypeName, actionId) => ({
    getAttribute(name) {
      return name === 'data-service-logical-type' ? serviceLogicalTypeName : actionId;
    },
    closest() {
      return {remove: () => removed.push(`${serviceLogicalTypeName}#${actionId}`)};
    }
  });
  const buttons = [
    button('causeway.security.LogoutMenu', 'logout'),
    button('petclinic.LogoutMenu', 'logout')
  ];

  applyAuthenticationMenuPolicy(authentication, documentWith(metadata, buttons));

  assert.deepEqual(removed, ['causeway.security.LogoutMenu#logout']);
});
