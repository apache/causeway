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

const MAX_UTF8_BYTES = 4096;
const MAX_ENCODED_LENGTH = 4096;
export const INVALID_ROUTE_MESSAGE = 'The requested application route is invalid.';

export interface ObjectRouteIdentity {
  readonly logicalTypeName: string;
  readonly objectId: string;
}

function invalidRoute(): Error {
  return new Error(INVALID_ROUTE_MESSAGE);
}

function hasUnpairedSurrogate(text: string): boolean {
  for (let index = 0; index < text.length; index += 1) {
    const current = text.charCodeAt(index);
    if (current >= 0xd800 && current <= 0xdbff) {
      const next = text.charCodeAt(index + 1);
      if (!(next >= 0xdc00 && next <= 0xdfff)) return true;
      index += 1;
    } else if (current >= 0xdc00 && current <= 0xdfff) {
      return true;
    }
  }
  return false;
}

export function encodeRouteSegment(value: unknown): string {
  const text = String(value ?? '');
  if (!text || text.length > MAX_UTF8_BYTES || text === '.' || text === '..'
      || /[\\/\u0000-\u001f\u007f-\u009f]/u.test(text) || hasUnpairedSurrogate(text)) {
    throw invalidRoute();
  }
  if (new TextEncoder().encode(text).length > MAX_UTF8_BYTES) throw invalidRoute();
  let encoded: string;
  try {
    encoded = encodeURIComponent(text).replace(/[!'()*]/g, character =>
      `%${character.charCodeAt(0).toString(16).toUpperCase()}`);
  } catch {
    throw invalidRoute();
  }
  if (encoded.length > MAX_ENCODED_LENGTH) throw invalidRoute();
  return encoded;
}

export function decodeRouteSegment(encoded: string): string {
  if (!encoded || encoded.length > MAX_ENCODED_LENGTH || !/^(?:[^%]|%[0-9A-F]{2})+$/u.test(encoded)) {
    throw invalidRoute();
  }
  let decoded: string;
  try {
    decoded = decodeURIComponent(encoded);
  } catch {
    throw invalidRoute();
  }
  if (encodeRouteSegment(decoded) !== encoded) throw invalidRoute();
  return decoded;
}

export function normalizeBasePath(basePath: unknown): string {
  let value = String(basePath ?? '').trim();
  if (!value.startsWith('/') || value.startsWith('//') || /[?#\\\u0000-\u001f]/u.test(value)) {
    throw new Error('The Vue viewer base path is invalid.');
  }
  while (value.length > 1 && value.endsWith('/')) value = value.slice(0, -1);
  return value;
}

export function canonicalObjectPath(basePath: unknown, target: {
  readonly logicalTypeName?: unknown;
  readonly id?: unknown;
  readonly objectId?: unknown;
}): string {
  const base = normalizeBasePath(basePath);
  const suffix = `/object/${encodeRouteSegment(target?.logicalTypeName)}/${encodeRouteSegment(target?.id ?? target?.objectId)}`;
  return base === '/' ? suffix : `${base}${suffix}`;
}

export function canonicalRouterObjectPath(target: {
  readonly logicalTypeName?: unknown;
  readonly id?: unknown;
  readonly objectId?: unknown;
}): string {
  return canonicalObjectPath('/', target);
}

export function parseCanonicalObjectPath(pathname: unknown, basePath: unknown = '/'): Readonly<ObjectRouteIdentity> {
  const path = String(pathname ?? '');
  const base = normalizeBasePath(basePath);
  const prefix = base === '/' ? '/object/' : `${base}/object/`;
  if (!path.startsWith(prefix)) throw invalidRoute();
  const tail = path.slice(prefix.length);
  const segments = tail.split('/');
  if (segments.length !== 2) throw invalidRoute();
  return Object.freeze({
    logicalTypeName: decodeRouteSegment(segments[0]),
    objectId: decodeRouteSegment(segments[1])
  });
}

export function canonicalRouteKey(identity: ObjectRouteIdentity): string {
  return canonicalRouterObjectPath(identity);
}
