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
import {readFile} from 'node:fs/promises';
import test from 'node:test';
import {
  canonicalObjectPath,
  encodeRouteSegment,
  homeObjectIdentity,
  resultObjectIdentity
} from '../src/main/resources/META-INF/resources/causeway-htmx/route-policy.mjs';

test('canonical route independently encodes public identity', () => {
  assert.equal(
    canonicalObjectPath('/app/htmx/', {logicalTypeName: 'petclinic.Pet Owner∕β', id: 'owner ?#42 café'}),
    '/app/htmx/object/petclinic.Pet%20Owner%E2%88%95%CE%B2/owner%20%3F%2342%20caf%C3%A9'
  );
});

test('shared cross-viewer route fixtures retain canonical HTMX meaning', async () => {
  const fixture = await readFile(new URL('../../canonical-route-fixtures.yaml', import.meta.url), 'utf8');
  const valid = [...fixture.matchAll(/logicalTypeName: (.+)\n\s+objectId: (.+)\n\s+path: (.+)/g)];
  assert.equal(valid.length, 3);
  for (const [, logicalTypeName, objectId, path] of valid) {
    assert.equal(canonicalObjectPath('/viewer', {logicalTypeName, id: objectId}), path);
  }
  assert.match(fixture, /customPagePrecedence:\n\s+exactLogicalType: petclinic.HomePage/);
  assert.match(fixture, /fallbackElement: causeway-object/);
});

test('route encoding rejects separators controls empties and dot segments', () => {
  for (const value of ['', '.', '..', 'one/two', 'one\\two', 'one\u0000two']) {
    assert.throws(() => encodeRouteSegment(value), /invalid/);
  }
});

test('object results and object homes expose route identities', () => {
  const metadata = {_meta: {logicalTypeName: 'petclinic.PetOwner', id: 'owner-1', title: 'Mary Smith'}};
  assert.deepEqual(resultObjectIdentity({kind: 'object', value: metadata}), {
    logicalTypeName: 'petclinic.PetOwner', id: 'owner-1', title: 'Mary Smith'
  });
  assert.deepEqual(homeObjectIdentity({home: {kind: 'OBJECT', logicalTypeName: 'petclinic.PetOwner', object: metadata}}), {
    logicalTypeName: 'petclinic.PetOwner', id: 'owner-1', title: 'Mary Smith'
  });
  assert.equal(resultObjectIdentity({kind: 'scalar', value: 'done'}), null);
  assert.equal(homeObjectIdentity({home: null}), null);
});
