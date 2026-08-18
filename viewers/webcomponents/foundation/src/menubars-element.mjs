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

import {CausewayElementName} from './component-contracts.mjs';
import {
  createSemanticEvent,
  MENU_BARS_CONTEXT_REQUEST_EVENT,
  MENU_BARS_DIAGNOSTIC_EVENT,
  MENU_BARS_STATE_EVENT,
  requestGraphQLClient
} from './context-events.mjs';
import {MENU_BAR_ROLES} from './menu-layout.mjs';
import {MenuBarsContextController, MenuBarsStatus} from './menu-context-controller.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
const ROLE_ELEMENTS = Object.freeze({
  primary: CausewayElementName.MENUBAR_PRIMARY,
  secondary: CausewayElementName.MENUBAR_SECONDARY,
  tertiary: CausewayElementName.MENUBAR_TERTIARY
});

export class CausewayMenubarsElement extends HTMLElementBase {
  constructor() {
    super();
    this._client = null;
    this._fetchImpl = null;
    this._context = null;
    this._release = null;
    this.lastDiagnosticGeneration = -1;
    this.addEventListener(MENU_BARS_CONTEXT_REQUEST_EVENT, event => {
      if (event.detail?.provide && this._context) {
        event.detail.provide(this._context);
        event.stopPropagation();
      }
    });
  }

  get client() {
    return this._client;
  }

  set client(value) {
    this._client = value ?? null;
    if (this.isConnected) {
      this.#connectContext();
    }
  }

  get fetchImpl() {
    return this._fetchImpl;
  }

  set fetchImpl(value) {
    this._fetchImpl = value ?? null;
  }

  get context() {
    return this._context;
  }

  connectedCallback() {
    this.#connectContext();
  }

  disconnectedCallback() {
    this._release?.();
    this._release = null;
    this._context?.disconnect();
    this._context = null;
  }

  refresh() {
    return this._context?.refresh() ?? Promise.resolve(null);
  }

  #connectContext() {
    this._release?.();
    this._release = null;
    this._context?.disconnect();
    const client = this._client ?? requestGraphQLClient(this);
    this._context = new MenuBarsContextController({
      client,
      fetchImpl: this._fetchImpl ?? globalThis.fetch
    });
    this.#provideContextToDeclarativeBars();
    this._release = this._context.subscribe(state => this.#acceptState(state));
    void this._context.refresh();
  }

  #acceptState(state) {
    this.setAttribute('data-menu-state', state.status);
    if (state.plan) {
      this.#ensureEffectiveBars(state.plan);
      this.#ensureInteractionController();
    }
    const status = this.#statusRoot();
    if (isLoading(state.status)) {
      status.hidden = false;
      status.setAttribute('role', 'status');
      status.textContent = 'Loading application menus…';
    } else if (state.status === MenuBarsStatus.TERMINAL_ERROR || state.status === MenuBarsStatus.UNSUPPORTED) {
      status.hidden = false;
      status.setAttribute('role', 'alert');
      status.textContent = state.status === MenuBarsStatus.UNSUPPORTED
        ? 'Application menus are unavailable.'
        : 'The application menus could not be loaded.';
    } else {
      status.hidden = true;
      status.removeAttribute?.('role');
      status.textContent = '';
    }
    this.dispatchEvent(createSemanticEvent(MENU_BARS_STATE_EVENT, Object.freeze({element: this, state})));
    if (state.generation !== this.lastDiagnosticGeneration && state.diagnostics.length > 0) {
      this.lastDiagnosticGeneration = state.generation;
      for (const diagnostic of state.diagnostics) {
        this.dispatchEvent(createSemanticEvent(MENU_BARS_DIAGNOSTIC_EVENT, Object.freeze({
          element: this,
          generation: state.generation,
          diagnostic
        })));
      }
    }
  }

  #provideContextToDeclarativeBars() {
    for (const child of this.#directBarChildren()) {
      child.context = this._context;
    }
  }

  #ensureEffectiveBars(plan) {
    const byRole = new Map(this.#directBarChildren().map(child => [roleForElement(child.localName), child]));
    for (const [index, role] of MENU_BAR_ROLES.entries()) {
      let bar = byRole.get(role) ?? null;
      const present = (plan.bars?.[role]?.menus?.length ?? 0) > 0;
      if (!bar && present && globalThis.document?.createElement) {
        bar = document.createElement(ROLE_ELEMENTS[role]);
        bar.setAttribute('data-causeway-generated', 'menubar');
        this.appendChild(bar);
        byRole.set(role, bar);
      }
      if (bar) {
        bar.setAttribute('data-causeway-bar-order', String(index));
        bar.context = this._context;
        if (!present && bar.getAttribute('data-causeway-generated') === 'menubar') {
          this.removeChild(bar);
          byRole.delete(role);
        }
      }
    }
  }

  #directBarChildren() {
    return [...(this.childNodes ?? [])]
      .filter(child => Object.values(ROLE_ELEMENTS).includes(child.localName));
  }

  #statusRoot() {
    let status = [...(this.childNodes ?? [])]
      .find(child => child.getAttribute?.('data-causeway-menubars-status') === 'true');
    if (!status && globalThis.document?.createElement) {
      status = document.createElement('div');
      status.setAttribute('data-causeway-menubars-status', 'true');
      status.setAttribute('class', 'causeway-menubars-status causeway-loading');
      this.appendChild(status);
    }
    return status ?? this;
  }

  #ensureInteractionController() {
    const existing = [...(this.childNodes ?? [])]
      .find(child => child.localName === CausewayElementName.INTERACTION_CONTROLLER);
    if (!existing && globalThis.document?.createElement) {
      const controller = document.createElement(CausewayElementName.INTERACTION_CONTROLLER);
      controller.setAttribute('data-causeway-generated', 'menubars-interaction-controller');
      this.appendChild(controller);
    }
  }
}

function roleForElement(name) {
  return Object.entries(ROLE_ELEMENTS).find(([, elementName]) => elementName === name)?.[0] ?? null;
}

function isLoading(status) {
  return [
    MenuBarsStatus.IDLE,
    MenuBarsStatus.APPLICATION_LOADING,
    MenuBarsStatus.RESOURCE_LOADING,
    MenuBarsStatus.SERVICE_LOADING
  ].includes(status);
}
