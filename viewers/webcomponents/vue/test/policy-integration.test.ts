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

import {createMemoryHistory, createRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createCausewayVueViewer} from '../src/plugin';
import {
  ACTION_REQUEST_EVENT,
  ACTION_RESULT_EVENT,
  installSemanticBridge,
  NAVIGATION_REQUEST_EVENT,
  presentSemanticResult,
  resolveResultOutlet
} from '../src/policy';
import {createCausewayRouteRecords} from '../src/routes';

function authoredShell(): HTMLElement {
  const shell = document.createElement('div');
  shell.innerHTML = `<cw-graphql-client data-causeway-shell-client>
    <cw-menubars></cw-menubars>
    <div data-causeway-route-loading></div><div data-causeway-route-announcement></div>
    <cw-action-results data-causeway-shell-result></cw-action-results>
    <main data-causeway-router-view></main>
  </cw-graphql-client>`;
  document.body.append(shell);
  return shell;
}

async function runtime(policies = {}) {
  const router = createRouter({history: createMemoryHistory('/viewer'), routes: createCausewayRouteRecords()});
  await router.push('/');
  await router.isReady();
  return createCausewayVueViewer({router, endpoint: '/graphql', basePath: '/viewer', policies});
}

beforeEach(() => document.body.replaceChildren());

describe('semantic policy bridge', () => {
  it('routes complete object navigation through canonical router paths', async () => {
    const viewer = await runtime();
    const shell = authoredShell();
    const dispose = installSemanticBridge(viewer, shell);
    const source = document.createElement('cw-object-link');
    shell.append(source);
    source.dispatchEvent(new CustomEvent(NAVIGATION_REQUEST_EVENT, {
      bubbles: true,
      cancelable: true,
      detail: {target: {logicalTypeName: 'petclinic.Pet Owner', id: 's_1 ?'}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(viewer.router.currentRoute.value.path).toBe('/object/petclinic.Pet%20Owner/s_1%20%3F');
    dispose();
  });

  it('lets an application claim navigation exactly once', async () => {
    const navigate = vi.fn((_target, claim) => claim.claim());
    const viewer = await runtime({navigate});
    const shell = authoredShell();
    const dispose = installSemanticBridge(viewer, shell);
    shell.dispatchEvent(new CustomEvent(NAVIGATION_REQUEST_EVENT, {
      bubbles: true,
      cancelable: true,
      detail: {target: {logicalTypeName: 'petclinic.Pet', id: 's_1'}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(navigate).toHaveBeenCalledOnce();
    expect(viewer.router.currentRoute.value.path).toBe('/');
    dispose();
  });

  it('prefers exactly one active-page result outlet and fails closed for duplicates', () => {
    const shell = authoredShell();
    const route = shell.querySelector('[data-causeway-router-view]')!;
    const pageResult = document.createElement('cw-action-results');
    pageResult.setAttribute('data-causeway-page-result', '');
    route.append(pageResult);
    expect(resolveResultOutlet(shell)).toBe(pageResult);
    route.append(pageResult.cloneNode());
    expect(() => resolveResultOutlet(shell)).toThrow(/duplicate/);
  });

  it('assigns scalar and collection results to semantic outlets', () => {
    const outlet = document.createElement('cw-action-results');
    presentSemanticResult(outlet, {actionId: 'find', result: {kind: 'scalar', value: 'done'}});
    expect(outlet.querySelector('output')?.textContent).toBe('done');
    presentSemanticResult(outlet, {actionId: 'find', result: {kind: 'collection', value: [{_meta: {id: '1'}}]}});
    const collection = outlet.querySelector('cw-standalone-collection') as HTMLElement & {result?: unknown};
    expect(collection.result).toEqual({kind: 'collection', value: [{_meta: {id: '1'}}]});
  });

  it('routes object action results and removes listeners on dispose', async () => {
    const viewer = await runtime();
    const shell = authoredShell();
    const dispose = installSemanticBridge(viewer, shell);
    shell.dispatchEvent(new CustomEvent(ACTION_RESULT_EVENT, {
      bubbles: true,
      detail: {result: {kind: 'object', value: {_meta: {logicalTypeName: 'petclinic.Pet', id: 's_pet-1'}}}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(viewer.router.currentRoute.value.path).toBe('/object/petclinic.Pet/s_pet-1');
    dispose();
    await viewer.router.replace('/');
    shell.dispatchEvent(new CustomEvent(NAVIGATION_REQUEST_EVENT, {
      bubbles: true,
      detail: {target: {logicalTypeName: 'petclinic.Pet', id: 's_pet-2'}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(viewer.router.currentRoute.value.path).toBe('/');
  });

  it('fails framework Logout closed before invocation and removes its menu control', async () => {
    const viewer = await runtime();
    const shell = authoredShell();
    const region = document.createElement('div');
    region.setAttribute('data-causeway-service-action-region', '');
    region.innerHTML = '<button data-causeway-service-action data-service-logical-type="causeway.security.LogoutMenu" data-action-id="logout">Logout</button>';
    shell.append(region);
    const dispose = installSemanticBridge(viewer, shell);
    expect(region.isConnected).toBe(false);
    const menuBoundary = shell.querySelector('cw-menubars') as HTMLElement & {
      excludeAction?: (detail: object) => boolean;
      actionLabel?: (detail: object) => string | void;
    };
    expect(menuBoundary.excludeAction?.({serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout'})).toBe(true);
    expect(menuBoundary.excludeAction?.({serviceLogicalTypeName: 'example.LogoutMenu', actionId: 'logout'})).toBe(false);

    const request = new CustomEvent(ACTION_REQUEST_EVENT, {
      bubbles: true,
      composed: true,
      cancelable: true,
      detail: {serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout', context: {}}
    });
    shell.dispatchEvent(request);
    expect(request.defaultPrevented).toBe(true);
    expect(shell.dataset.causewayLogoutUnavailable).toBe('true');
    expect(shell.querySelector('[data-causeway-route-announcement]')?.textContent).toContain('host authentication');
    dispose();
    expect(menuBoundary.excludeAction).toBeUndefined();
    expect(menuBoundary.actionLabel).toBeUndefined();
  });

  it('retains and relabels exact framework Logout only with an explicit host menu policy', async () => {
    const menuActionLabel = vi.fn((detail: {serviceLogicalTypeName?: string; actionId: string}) => detail.serviceLogicalTypeName === 'causeway.security.LogoutMenu'
      && detail.actionId === 'logout' ? 'Sign out' : undefined);
    const viewer = await runtime({menuActionLabel});
    const shell = authoredShell();
    const dispose = installSemanticBridge(viewer, shell);
    const menuBoundary = shell.querySelector('cw-menubars') as HTMLElement & {
      excludeAction?: (detail: object) => boolean;
      actionLabel?: (detail: object) => string | void;
    };
    const logout = {serviceLogicalTypeName: 'causeway.security.LogoutMenu', actionId: 'logout'};
    const similar = {serviceLogicalTypeName: 'example.LogoutMenu', actionId: 'logout'};
    expect(menuBoundary.excludeAction?.(logout)).toBe(false);
    expect(menuBoundary.actionLabel?.(logout)).toBe('Sign out');
    expect(menuBoundary.actionLabel?.(similar)).toBeUndefined();
    expect(menuActionLabel).toHaveBeenCalled();
    dispose();
  });

  it('lets synchronous and asynchronous action policies claim or resume exactly once', async () => {
    const claimed = vi.fn((_detail, claim) => claim.claim());
    const claimedViewer = await runtime({action: claimed});
    const claimedShell = authoredShell();
    const disposeClaimed = installSemanticBridge(claimedViewer, claimedShell);
    const claimedRequest = new CustomEvent(ACTION_REQUEST_EVENT, {
      bubbles: true, cancelable: true, detail: {actionId: 'run', serviceLogicalTypeName: 'example.Service', context: {}}
    });
    claimedShell.dispatchEvent(claimedRequest);
    expect(claimedRequest.defaultPrevented).toBe(true);
    expect(claimed).toHaveBeenCalledOnce();
    disposeClaimed();

    const asyncViewer = await runtime({action: async () => false});
    const asyncShell = authoredShell();
    const source = document.createElement('button');
    asyncShell.append(source);
    const seen: Event[] = [];
    source.addEventListener(ACTION_REQUEST_EVENT, event => seen.push(event));
    const disposeAsync = installSemanticBridge(asyncViewer, asyncShell);
    source.dispatchEvent(new CustomEvent(ACTION_REQUEST_EVENT, {
      bubbles: true, cancelable: true, detail: {actionId: 'run', serviceLogicalTypeName: 'example.Service', context: {}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(seen).toHaveLength(2);
    expect(seen[0].defaultPrevented).toBe(true);
    expect(seen[1].defaultPrevented).toBe(false);
    disposeAsync();
  });

  it('lets the result policy claim local-resource navigation', async () => {
    const result = vi.fn((_detail, claim) => claim.claim());
    const viewer = await runtime({result});
    const shell = authoredShell();
    const dispose = installSemanticBridge(viewer, shell);
    shell.dispatchEvent(new CustomEvent(ACTION_RESULT_EVENT, {
      bubbles: true,
      detail: {result: {kind: 'local-resource', value: {path: '/guide', openUrlStrategy: 'SAME_WINDOW'}}}
    }));
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(result).toHaveBeenCalledOnce();
    expect(viewer.router.currentRoute.value.path).toBe('/');
    dispose();
  });
});
