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

import type {Component} from 'vue';

export type CausewayPageLoader = () => Promise<Component | {default: Component}>;
export type CausewayPageRegistration = Component | CausewayPageLoader;
export type CausewayPageRegistryInput = ReadonlyMap<string, CausewayPageRegistration>
  | ReadonlyArray<readonly [string, CausewayPageRegistration]>
  | Readonly<Record<string, CausewayPageRegistration>>;

const LOGICAL_TYPE = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/u;

function entries(input: CausewayPageRegistryInput | undefined): ReadonlyArray<readonly [string, CausewayPageRegistration]> {
  if (!input) return [];
  if (input instanceof Map) return [...input.entries()];
  if (Array.isArray(input)) return input;
  return Object.entries(input);
}

export function normalizePageRegistry(input?: CausewayPageRegistryInput): ReadonlyMap<string, CausewayPageRegistration> {
  const normalized = new Map<string, CausewayPageRegistration>();
  for (const [rawKey, registration] of entries(input)) {
    const key = String(rawKey ?? '').trim();
    if (!LOGICAL_TYPE.test(key)) throw new Error('A Vue page registration has an invalid logical type.');
    if ((!registration || (typeof registration !== 'object' && typeof registration !== 'function'))) {
      throw new Error(`The Vue page registration for ${key} is unsupported.`);
    }
    if (normalized.has(key)) throw new Error(`The Vue page registration for ${key} is duplicated.`);
    normalized.set(key, registration);
  }
  return normalized;
}

export function isPageLoader(registration: CausewayPageRegistration): registration is CausewayPageLoader {
  return typeof registration === 'function';
}
