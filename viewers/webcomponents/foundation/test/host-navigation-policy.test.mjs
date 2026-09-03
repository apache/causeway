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
import {normalizeActionResult} from '../src/action-result.mjs';
import {isFrameworkLogoutAction} from '../src/host-operation-policy.mjs';
import {navigateLocalResource, resolveLocalResourceTarget} from '../src/local-resource-policy.mjs';

const object = name => ({kind: 'OBJECT', name, ofType: null});
const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const nonNull = type => ({kind: 'NON_NULL', name: null, ofType: type});

const localResource = (path = '/guide', openUrlStrategy = 'SAME_WINDOW') => ({path, openUrlStrategy});
const browserLocation = (href = 'https://example.test/viewer/object/Thing/1') => ({href, assign() {}});

test('framework Logout matching requires the exact canonical identity', () => {
  assert.equal(isFrameworkLogoutAction({serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout'}), true);
  assert.equal(isFrameworkLogoutAction({serviceLogicalTypeName: 'example.LogoutMenu', actionId: 'logout'}), false);
  assert.equal(isFrameworkLogoutAction({serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'signOut'}), false);
  assert.equal(isFrameworkLogoutAction({serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout', path: '/other'}), true);
  assert.equal(isFrameworkLogoutAction({kind: 'local-resource', value: localResource('/logout')}), false);
});

test('typed local-resource results are distinct, immutable, and exact-type only', () => {
  const result = normalizeActionResult(localResource('/guide', 'NEW_WINDOW'), nonNull(object('LocalResourcePathValue')));
  assert.deepEqual(result, {kind: 'local-resource', value: {path: '/guide', openUrlStrategy: 'NEW_WINDOW'}});
  assert.equal(Object.isFrozen(result), true);
  assert.equal(Object.isFrozen(result.value), true);
  assert.deepEqual(normalizeActionResult(localResource(), object('OtherValue')), {kind: 'object', value: localResource()});
  assert.deepEqual(normalizeActionResult('done', scalar('String')), {kind: 'scalar', value: 'done'});
  assert.deepEqual(normalizeActionResult(null, object('LocalResourcePathValue')), {kind: 'void', value: null});
});

test('malformed typed local-resource results fail closed', () => {
  for (const value of [{}, {path: '/guide'}, {path: '', openUrlStrategy: 'SAME_WINDOW'}, {path: '/guide', openUrlStrategy: 'OTHER'}]) {
    assert.deepEqual(normalizeActionResult(value, object('LocalResourcePathValue')), {
      kind: 'unsupported', value: null, reason: 'LOCAL_RESOURCE_RESULT_INVALID'
    });
  }
});

test('local-resource resolution preserves root and nested application bases exactly once', () => {
  const location = browserLocation();
  assert.equal(resolveLocalResourceTarget(localResource('/guide'), {location}).url.href, 'https://example.test/guide');
  assert.equal(resolveLocalResourceTarget(localResource('/logout'), {location}).url.href, 'https://example.test/logout');
  assert.equal(resolveLocalResourceTarget(localResource('/guide'), {location, applicationBase: '/app'}).url.href, 'https://example.test/app/guide');
  assert.equal(resolveLocalResourceTarget(localResource('/app/guide'), {location, applicationBase: '/app'}).url.href, 'https://example.test/app/guide');
  assert.equal(resolveLocalResourceTarget(localResource('guide?mode=full#top'), {location, applicationBase: '/app/'}).url.href,
    'https://example.test/app/guide?mode=full#top');
});

test('local-resource resolution rejects unsafe and escaping targets', () => {
  const location = browserLocation();
  for (const path of ['https://evil.test/x', '//evil.test/x', 'javascript:alert(1)', '..\\secret', '../secret', '/app/../secret']) {
    assert.throws(() => resolveLocalResourceTarget(localResource(path), {location, applicationBase: '/app'}), /application-local|escapes/);
  }
  assert.throws(() => resolveLocalResourceTarget(localResource('/guide', 'OTHER'), {location}), /incomplete/);
  assert.throws(() => resolveLocalResourceTarget(localResource('/guide'), {location, applicationBase: 'https://evil.test/app'}), /same-origin/);
});

test('local-resource navigation uses full-document and opener-isolated browser operations', () => {
  const assigned = [];
  const location = {href: 'https://example.test/viewer', assign: value => assigned.push(value)};
  navigateLocalResource(localResource('/guide'), {location, applicationBase: '/app'});
  assert.deepEqual(assigned, ['https://example.test/app/guide']);

  const openedWindow = {opener: {}};
  const opened = [];
  navigateLocalResource(localResource('/guide', 'NEW_WINDOW'), {
    location,
    applicationBase: '/app',
    open: (...args) => { opened.push(args); return openedWindow; }
  });
  assert.deepEqual(opened, [['https://example.test/app/guide', '_blank', 'noopener,noreferrer']]);
  assert.equal(openedWindow.opener, null);
  assert.deepEqual(assigned, ['https://example.test/app/guide']);
  assert.doesNotThrow(() => navigateLocalResource(localResource('/guide', 'NEW_WINDOW'), {
    location,
    applicationBase: '/app',
    open: () => null
  }));
});
