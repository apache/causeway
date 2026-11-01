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

import {createSemanticEvent, MENU_BARS_STATE_EVENT} from './context-events.mjs';
import {
  applyServiceActionStates,
  CAUSEWAY_MENU_BARS_NAMESPACE,
  menuPlanActionReferences,
  parseCausewayMenuBarsXml,
  serviceActionKey
} from './menu-layout.mjs';
import {ServiceActionContextController} from './service-action-context.mjs';
import {fetchStructuralResource} from './structural-resource.mjs';

export const MenuBarsStatus = Object.freeze({
  IDLE: 'idle',
  APPLICATION_LOADING: 'application-loading',
  RESOURCE_LOADING: 'resource-loading',
  SERVICE_LOADING: 'service-loading',
  READY: 'ready',
  PARTIAL_ERROR: 'partial-error',
  UNSUPPORTED: 'unsupported',
  TERMINAL_ERROR: 'terminal-error'
});

export class MenuBarsContextController extends EventTarget {
  constructor({
    client,
    fetchImpl = globalThis.fetch,
    schedule = callback => globalThis.queueMicrotask(callback)
  } = {}) {
    super();
    this.client = client ?? null;
    this.fetchImpl = fetchImpl;
    this.schedule = schedule;
    this.stateListeners = new Set();
    this.serviceContexts = new Map();
    this.generation = 0;
    this.abortController = null;
    this.mutationTail = Promise.resolve();
    this.closed = false;
    this.state = freezeState({
      status: MenuBarsStatus.IDLE,
      generation: 0,
      plan: null,
      diagnostics: [],
      error: null,
      resource: null
    });
  }

  subscribe(listener) {
    this.stateListeners.add(listener);
    listener(this.state);
    return () => this.stateListeners.delete(listener);
  }

  refresh() {
    if (this.closed) {
      return Promise.resolve(this.state);
    }
    const generation = ++this.generation;
    this.abortController?.abort();
    const abortController = new AbortController();
    this.abortController = abortController;
    if (!this.client) {
      this.#setState({
        status: MenuBarsStatus.TERMINAL_ERROR,
        generation,
        plan: null,
        diagnostics: [diagnostic('GRAPHQL_CLIENT_REQUIRED', 'A Causeway GraphQL client is required.')],
        error: new Error('A Causeway GraphQL client is required.'),
        resource: null
      });
      return Promise.resolve(this.state);
    }
    this.#setState({
      status: MenuBarsStatus.APPLICATION_LOADING,
      generation,
      plan: null,
      diagnostics: [],
      error: null,
      resource: null
    });
    return this.#load(generation, abortController.signal);
  }

  serviceContext(logicalTypeName) {
    if (!this.serviceContexts.has(logicalTypeName)) {
      this.serviceContexts.set(logicalTypeName, new ServiceActionContextController({
        client: this.client,
        logicalTypeName,
        serializeMutation: execute => this.#serializeMutation(execute),
        onChanged: () => this.schedule(() => this.refresh())
      }));
    }
    return this.serviceContexts.get(logicalTypeName);
  }

  disconnect() {
    this.closed = true;
    this.abortController?.abort();
    this.abortController = null;
    for (const context of this.serviceContexts.values()) {
      context.disconnect();
    }
    this.serviceContexts.clear();
    this.stateListeners.clear();
  }

  async #load(generation, signal) {
    try {
      const capability = await this.client.describeApplicationEntry({signal});
      if (!this.#isCurrent(generation)) {
        return this.state;
      }
      if (!capability.supported) {
        this.#setState({
          status: MenuBarsStatus.UNSUPPORTED,
          generation,
          plan: null,
          diagnostics: [diagnostic(capability.reason ?? 'MENU_BARS_UNAVAILABLE', 'The rich GraphQL application menu capability is unavailable.')],
          error: null,
          resource: null
        });
        return this.state;
      }
      const application = await this.client.readApplicationEntry({description: capability, signal});
      if (!this.#isCurrent(generation)) {
        return this.state;
      }
      if (application.errors?.length && !application.data) {
        throw new Error('Application menu metadata could not be read.');
      }
      const diagnostics = applicationDiagnostics(application.data?.issues);
      for (const ignored of (application.errors ?? []).slice(0, 20 - diagnostics.length)) {
        diagnostics.push(diagnostic('APPLICATION_ENTRY_PARTIAL_ERROR', 'Part of the application menu metadata could not be read.'));
      }
      if (!application.data?.menuBars) {
        this.#setState({
          status: MenuBarsStatus.UNSUPPORTED,
          generation,
          plan: null,
          diagnostics: [...diagnostics, diagnostic('MENU_BARS_UNAVAILABLE', 'The current application menu resource is unavailable.')],
          error: null,
          resource: null
        });
        return this.state;
      }
      const descriptor = application.data.menuBars;
      if (descriptor.mediaType && !String(descriptor.mediaType).toLowerCase().startsWith('application/xml')) {
        throw menuError('MENU_MEDIA_TYPE_UNSUPPORTED', 'The application menu resource has an unsupported media type.');
      }
      if (descriptor.formatVersion && descriptor.formatVersion !== CAUSEWAY_MENU_BARS_NAMESPACE) {
        throw menuError('MENU_FORMAT_UNSUPPORTED', 'The application menu resource uses an unsupported format.');
      }
      if (descriptor.cacheControl && !String(descriptor.cacheControl).toLowerCase().includes('no-store')) {
        diagnostics.push(diagnostic('MENU_CACHE_POLICY_UNEXPECTED', 'The application menu descriptor did not advertise no-store delivery.'));
      }
      this.#setState({
        status: MenuBarsStatus.RESOURCE_LOADING,
        generation,
        plan: null,
        diagnostics,
        error: null,
        resource: safeResourceDescriptor(descriptor)
      });
      const resource = await fetchStructuralResource(descriptor.href, {
        fetchImpl: this.fetchImpl,
        accept: 'application/xml',
        signal
      });
      if (!this.#isCurrent(generation)) {
        return this.state;
      }
      const parsed = parseCausewayMenuBarsXml(resource.text);
      diagnostics.push(...parsed.diagnostics);
      this.#setState({
        status: MenuBarsStatus.SERVICE_LOADING,
        generation,
        plan: parsed.plan,
        diagnostics,
        error: null,
        resource: safeResourceDescriptor(descriptor)
      });
      const actionStates = new Map();
      const references = menuPlanActionReferences(parsed.plan);
      const byService = new Map();
      for (const reference of references) {
        const actions = byService.get(reference.serviceLogicalTypeName) ?? [];
        actions.push(reference);
        byService.set(reference.serviceLogicalTypeName, actions);
      }
      await Promise.all([...byService].map(async ([logicalTypeName, actions]) => {
        try {
          const result = await this.serviceContext(logicalTypeName).loadActionStates(
            actions.map(action => action.actionId),
            {signal}
          );
          for (const [actionId, state] of result.states) {
            actionStates.set(serviceActionKey(logicalTypeName, actionId), state);
          }
          for (const error of result.errors ?? []) {
            const actionId = error.path?.find(segment => typeof segment === 'string' && actions.some(action => action.actionId === segment));
            diagnostics.push(diagnostic(
              'SERVICE_ACTION_STATE_FAILED',
              'Current state for a menu service action could not be read.',
              actionId ? `${logicalTypeName}#${actionId}` : logicalTypeName
            ));
          }
        } catch (error) {
          if (error?.name === 'AbortError') {
            throw error;
          }
          diagnostics.push(diagnostic('SERVICE_TYPE_UNAVAILABLE', 'A menu service type is unavailable.', logicalTypeName));
          for (const action of actions) {
            actionStates.set(serviceActionKey(logicalTypeName, action.actionId), Object.freeze({
              hidden: false,
              disabled: null,
              error: 'SERVICE_TYPE_UNAVAILABLE'
            }));
          }
        }
      }));
      if (!this.#isCurrent(generation)) {
        return this.state;
      }
      const plan = applyServiceActionStates(parsed.plan, actionStates, diagnostics);
      this.#setState({
        status: plan.diagnostics.length > 0 ? MenuBarsStatus.PARTIAL_ERROR : MenuBarsStatus.READY,
        generation,
        plan,
        diagnostics: plan.diagnostics,
        error: null,
        resource: safeResourceDescriptor(descriptor)
      });
    } catch (error) {
      if (error?.name === 'AbortError' || !this.#isCurrent(generation)) {
        return this.state;
      }
      const safe = safeError(error);
      this.#setState({
        status: MenuBarsStatus.TERMINAL_ERROR,
        generation,
        plan: null,
        diagnostics: [diagnostic(safe.code, safe.message)],
        error: safe,
        resource: null
      });
    }
    return this.state;
  }

  #serializeMutation(execute) {
    const result = this.mutationTail.then(execute, execute);
    this.mutationTail = result.catch(() => undefined);
    return result;
  }

  #isCurrent(generation) {
    return !this.closed && generation === this.generation;
  }

  #setState(changes) {
    this.state = freezeState({...this.state, ...changes});
    for (const listener of this.stateListeners) {
      listener(this.state);
    }
    this.dispatchEvent(createSemanticEvent(MENU_BARS_STATE_EVENT, this.state));
  }
}

function applicationDiagnostics(issues = []) {
  return issues.slice(0, 20).map(issue => diagnostic(
    /^[_A-Z][_A-Z0-9]{0,63}$/.test(issue?.code ?? '') ? issue.code : 'APPLICATION_MENU_ISSUE',
    'The application entry reported a menu issue.'
  ));
}

function safeResourceDescriptor(descriptor) {
  return Object.freeze({
    href: typeof descriptor?.href === 'string' ? descriptor.href : null,
    mediaType: typeof descriptor?.mediaType === 'string' ? descriptor.mediaType : null,
    formatVersion: typeof descriptor?.formatVersion === 'string' ? descriptor.formatVersion : null,
    generation: typeof descriptor?.generation === 'string' ? descriptor.generation.slice(0, 128) : null
  });
}

function diagnostic(code, message, path = null) {
  return Object.freeze({code, message, path});
}

function menuError(code, message) {
  const error = new Error(message);
  error.code = code;
  return error;
}

function safeError(error) {
  const code = /^[_A-Z][_A-Z0-9]{0,63}$/.test(error?.code ?? '') ? error.code : 'MENU_BARS_LOAD_FAILED';
  const messages = {
    INVALID_RESOURCE_PATH: 'The application menu resource path is invalid.',
    RESOURCE_TOO_LARGE: 'The application menu resource exceeds the supported size.',
    RESOURCE_REQUEST_FAILED: 'The application menu resource request failed.',
    FETCH_UNAVAILABLE: 'The application menu resource cannot be fetched in this environment.',
    MENU_MEDIA_TYPE_UNSUPPORTED: 'The application menu resource has an unsupported media type.',
    MENU_FORMAT_UNSUPPORTED: 'The application menu resource uses an unsupported format.'
  };
  const message = messages[code] ?? (code.startsWith('MENU_XML_') || code === 'MENU_ROOT_REQUIRED'
    ? 'The application menu resource is malformed or unsupported.'
    : 'The application menu could not be loaded.');
  const safe = new Error(message);
  safe.name = 'MenuBarsError';
  safe.code = code;
  return safe;
}

function freezeState(state) {
  return Object.freeze({
    status: state.status,
    generation: state.generation,
    plan: state.plan ?? null,
    diagnostics: Object.freeze([...(state.diagnostics ?? [])].slice(0, 20)),
    error: state.error ?? null,
    resource: state.resource ?? null
  });
}
