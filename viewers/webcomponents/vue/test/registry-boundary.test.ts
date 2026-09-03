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

import {defineComponent} from 'vue';
import {describe, expect, it} from 'vitest';
import {validateRouteBoundary, validateShellBoundary} from '../src/boundary';
import {normalizePageRegistry} from '../src/registry';

const Page = defineComponent({template: '<div />'});

describe('page registry', () => {
  it('normalizes exact registrations immutably', () => {
    const registry = normalizePageRegistry({'petclinic.PetOwner': Page});
    expect(registry.get('petclinic.PetOwner')).toBe(Page);
    expect(registry.has('petclinic.Pet')).toBe(false);
  });

  it('rejects malformed duplicate and unsupported registrations', () => {
    expect(() => normalizePageRegistry({'not a type': Page})).toThrow(/invalid logical type/);
    expect(() => normalizePageRegistry({'petclinic.Pet': null as never})).toThrow(/unsupported/);
    expect(() => normalizePageRegistry([
      ['petclinic.Pet', Page],
      ['petclinic.Pet', Page]
    ])).toThrow(/duplicated/);
  });
});

describe('authored semantic boundaries', () => {
  it('accepts one bound context containing one interaction controller', () => {
    const root = document.createElement('div');
    root.innerHTML = `<cw-object-context data-causeway-route-context logical-type="petclinic.Pet" object-id="s_pet-1">
      <cw-object></cw-object><cw-interaction-controller data-causeway-route-interactions></cw-interaction-controller>
    </cw-object-context>`;
    expect(validateRouteBoundary(root, {logicalTypeName: 'petclinic.Pet', objectId: 's_pet-1'}).valid).toBe(true);
  });

  it('rejects missing duplicate mismatched and misplaced boundaries without repair', () => {
    const missing = document.createElement('div');
    expect(validateRouteBoundary(missing, {logicalTypeName: 'x.Type', objectId: '1'}).classification).toBe('missing-context');
    expect(missing.children).toHaveLength(0);

    const duplicate = document.createElement('div');
    duplicate.innerHTML = '<cw-object-context data-causeway-route-context></cw-object-context><cw-object-context data-causeway-route-context></cw-object-context>';
    expect(validateRouteBoundary(duplicate, {logicalTypeName: 'x.Type', objectId: '1'}).classification).toBe('duplicate-context');

    const mismatch = document.createElement('div');
    mismatch.innerHTML = '<cw-object-context data-causeway-route-context logical-type="x.Other" object-id="1"><cw-interaction-controller data-causeway-route-interactions></cw-interaction-controller></cw-object-context>';
    expect(validateRouteBoundary(mismatch, {logicalTypeName: 'x.Type', objectId: '1'}).classification).toBe('identity');

    const noController = document.createElement('div');
    noController.innerHTML = '<cw-object-context data-causeway-route-context logical-type="x.Type" object-id="1"></cw-object-context>';
    expect(validateRouteBoundary(noController, {logicalTypeName: 'x.Type', objectId: '1'}).classification).toBe('interactions');
  });

  it('validates application-owned shell landmarks without manufacturing them', () => {
    const shell = document.createElement('div');
    shell.innerHTML = `<cw-graphql-client data-causeway-shell-client>
      <div data-causeway-route-loading></div><div data-causeway-route-announcement></div>
      <cw-action-results data-causeway-shell-result></cw-action-results><main data-causeway-router-view></main>
    </cw-graphql-client>`;
    expect(validateShellBoundary(shell).route.localName).toBe('main');
    shell.querySelector('[data-causeway-route-loading]')?.remove();
    expect(() => validateShellBoundary(shell)).toThrow(/shell is invalid/);
    expect(shell.querySelector('[data-causeway-route-loading]')).toBeNull();
  });
});
