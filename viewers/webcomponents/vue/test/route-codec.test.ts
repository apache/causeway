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

import {readFile} from 'node:fs/promises';
import {resolve} from 'node:path';
import {describe, expect, it} from 'vitest';
import {
  canonicalObjectPath,
  decodeRouteSegment,
  encodeRouteSegment,
  parseCanonicalObjectPath
} from '../src/route-codec';

describe('canonical Vue routes', () => {
  it('independently encodes and decodes identity', () => {
    const path = canonicalObjectPath('/viewer/', {
      logicalTypeName: 'petclinic.Pet Owner∕β',
      objectId: 'owner ?#42 café'
    });
    expect(path).toBe('/viewer/object/petclinic.Pet%20Owner%E2%88%95%CE%B2/owner%20%3F%2342%20caf%C3%A9');
    expect(parseCanonicalObjectPath(path, '/viewer')).toEqual({
      logicalTypeName: 'petclinic.Pet Owner∕β',
      objectId: 'owner ?#42 café'
    });
    expect(encodeRouteSegment("!'()*~")).toBe('%21%27%28%29%2A~');
  });

  it('honors UTF-8 and encoded bounds', () => {
    expect(encodeRouteSegment('a'.repeat(4096))).toHaveLength(4096);
    expect(() => encodeRouteSegment('a'.repeat(4097))).toThrow(/invalid/i);
    expect(() => encodeRouteSegment('é'.repeat(2049))).toThrow(/invalid/i);
    expect(() => encodeRouteSegment(' '.repeat(1366))).toThrow(/invalid/i);
  });

  it('rejects noncanonical and unsafe segments', () => {
    for (const value of ['', '.', '..', 'one/two', 'one\\two', 'one\u0000two', 'one\u0080two', '\ud800', '\udc00']) {
      expect(() => encodeRouteSegment(value)).toThrow(/invalid/i);
    }
    for (const encoded of ['%', '%2f', '%2F', '%5C', '%00', '%2E%2E', '%41', 'a/b']) {
      expect(() => decodeRouteSegment(encoded)).toThrow(/invalid/i);
    }
  });

  it('matches every shared route fixture', async () => {
    const fixture = await readFile(resolve(process.cwd(), '../canonical-route-fixtures.yaml'), 'utf8');
    const valid = [...fixture.matchAll(/logicalTypeName: (.+)\n\s+objectId: (.+)\n\s+path: (.+)/g)];
    expect(valid).toHaveLength(3);
    for (const [, logicalTypeName, objectId, path] of valid) {
      expect(canonicalObjectPath('/viewer', {logicalTypeName, objectId})).toBe(path);
      expect(parseCanonicalObjectPath(path, '/viewer')).toEqual({logicalTypeName, objectId});
    }
    const invalidBlock = fixture.split('invalid:')[1].split('customPagePrecedence:')[0];
    const invalid = [...invalidBlock.matchAll(/^\s+- (.+)$/gm)].map(match => match[1]);
    expect(invalid.length).toBeGreaterThan(0);
    for (const path of invalid) expect(() => parseCanonicalObjectPath(path, '/viewer')).toThrow(/invalid/i);
  });

  it('round trips generated Unicode examples', () => {
    for (const identity of [
      {logicalTypeName: 'example.Alpha', objectId: 's_1'},
      {logicalTypeName: 'example.Δοκιμή', objectId: '日本語'},
      {logicalTypeName: 'example.Space Type', objectId: 'a b?#c'}
    ]) {
      expect(parseCanonicalObjectPath(canonicalObjectPath('/viewer', identity), '/viewer')).toEqual(identity);
    }
  });
});
