/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createVaadinMenuItems, resolveCausewayMenuAction} from './menubar-projection.mjs';

export const CAUSEWAY_MENUBAR_CONTROL = 'cw-menubar-control';
export const CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT = 'causeway-menubar-widget-policy';
const DEFAULT_MODULE_URL = new URL('./vaadin-menubar/vaadin-menubar.js', import.meta.url).href;
const DEFAULT_DEFINITION_TIMEOUT_MS = 5000;
const MAX_DEFINITION_TIMEOUT_MS = 60000;
const APPROVED_PROTOCOLS = new Set(['http:', 'https:', 'file:']);
let configuration = Object.freeze({enabled: true, moduleUrl: DEFAULT_MODULE_URL, definitionTimeoutMs: DEFAULT_DEFINITION_TIMEOUT_MS, excludeAction: null});
let modulePromise = null;
let failed = false;
let failure = null;
let configurationRevision = 0;

const documentPolicy = globalThis.document?.documentElement?.dataset;
if (documentPolicy && Object.hasOwn(documentPolicy, 'causewayApplicationMenubar')) {
  configureCausewayMenubarWidgets({
    enabled: documentPolicy.causewayApplicationMenubar === 'vaadin',
    moduleUrl: documentPolicy.causewayApplicationMenubarUrl
  });
}

export function configureCausewayMenubarWidgets(options = {}) {
  configuration = Object.freeze({
    enabled: options.enabled !== false,
    moduleUrl: options.moduleUrl ? safeModuleUrl(options.moduleUrl) : DEFAULT_MODULE_URL,
    definitionTimeoutMs: boundedTimeout(options.definitionTimeoutMs),
    excludeAction: typeof options.excludeAction === 'function' ? options.excludeAction : null
  });
  modulePromise = null;
  failed = false;
  failure = null;
  configurationRevision += 1;
  announcePolicyChange({family: 'menubar', reason: 'configuration', revision: configurationRevision});
  return configuration;
}

export function causewayMenubarWidgetConfiguration() {
  return Object.freeze({...configuration, failed, failure, revision: configurationRevision});
}

export function useCausewayMenubarWidget() {
  return configuration.enabled && !failed;
}

export function failCausewayMenubarWidget({phase = 'adapter', classification = 'MENUBAR_ADAPTER_UNAVAILABLE'} = {}) {
  if (failed) return false;
  failed = true;
  modulePromise = null;
  failure = Object.freeze({phase: boundedToken(phase), classification: boundedToken(classification)});
  announcePolicyChange({family: 'menubar', reason: 'failure', revision: configurationRevision, ...failure});
  return true;
}

export class CausewayMenubarControlElement extends (globalThis.HTMLElement ?? class extends EventTarget {}) {
  constructor() {
    super();
    this._control = null;
    this._projection = null;
    this._activate = null;
    this._overflowLabel = 'More options';
    this._generation = 0;
    this._presentationRevision = 0;
    this._focusRequest = null;
    this._selectionListener = event => this.#selected(event);
  }

  connectedCallback() {
    this.#startUpgrade();
  }

  disconnectedCallback() {
    this._generation += 1;
    this.#releaseControl();
  }

  set presentation(value) {
    this._projection = value?.projection ?? null;
    this._activate = typeof value?.activate === 'function' ? value.activate : null;
    this._overflowLabel = String(value?.overflowLabel || 'More options').slice(0, 80);
    this._presentationRevision += 1;
    if (this._control) {
      this.#applyPresentation();
    } else if (this.isConnected) {
      this.#startUpgrade();
    }
  }

  get presentation() {
    return this._projection ? Object.freeze({projection: this._projection}) : null;
  }

  focus(options) {
    if (this._control) {
      this._control.focus?.(options);
      return;
    }
    this._focusRequest = options ?? Object.freeze({preventScroll: true});
  }

  #startUpgrade() {
    const generation = ++this._generation;
    const revision = configurationRevision;
    if (!configuration.enabled || failed || !this._projection?.accepted) {
      this.dataset.widgetState = 'native';
      return;
    }
    this.dataset.widgetState = 'loading';
    void this.#upgrade(generation, revision);
  }

  async #upgrade(generation, revision) {
    let phase = 'module';
    try {
      enableAccessibleDisabledItems();
      modulePromise ??= import(configuration.moduleUrl);
      await modulePromise;
      if (!this.#isCurrent(generation, revision)) return;
      phase = 'definition';
      await waitForDefinition(configuration.definitionTimeoutMs);
      if (!this.#isCurrent(generation, revision)) return;
      phase = 'adapter';
      const control = document.createElement('vaadin-menu-bar');
      control.setAttribute('data-causeway-menubar-control', '');
      control.setAttribute('aria-label', `${humanize(this._projection.role)} application menu`);
      control.addEventListener('item-selected', this._selectionListener);
      this._control = control;
      this.replaceChildren(control);
      this.#applyPresentation();
      await control.updateComplete;
      if (!this.#isCurrent(generation, revision) || control !== this._control) return;
      if (this._focusRequest) {
        const focusOptions = this._focusRequest;
        this._focusRequest = null;
        control.focus?.(focusOptions);
      }
      this.dataset.widgetState = 'ready';
      this.dispatchEvent(new CustomEvent('causeway-menubar-ready', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family: 'menubar', role: this._projection.role, revision})
      }));
    } catch (_error) {
      if (!this.#isCurrent(generation, revision)) return;
      this.#releaseControl();
      this.dataset.widgetState = 'fallback';
      this.dataset.widgetError = 'The configured application Menu Bar could not be loaded.';
      const classification = phase === 'module'
        ? 'MENUBAR_MODULE_UNAVAILABLE'
        : phase === 'definition' ? 'MENUBAR_DEFINITION_UNAVAILABLE' : 'MENUBAR_ADAPTER_UNAVAILABLE';
      this.dispatchEvent(new CustomEvent('causeway-menubar-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family: 'menubar', phase, classification, revision})
      }));
      failCausewayMenubarWidget({phase, classification});
    }
  }

  #applyPresentation() {
    if (!this._control || !this._projection?.accepted) return;
    this._control.i18n = {...(this._control.i18n ?? {}), moreOptions: this._overflowLabel};
    this._control.items = materializeMenuItems(createVaadinMenuItems(this._projection));
  }

  #selected(event) {
    if (!this.isConnected || !this._control || event.currentTarget !== this._control) return;
    const key = event.detail?.value?.causewayKey;
    const descriptor = resolveCausewayMenuAction(this._projection, key);
    if (!descriptor) return;
    try {
      this._activate?.(descriptor);
    } catch (_error) {
      const classification = 'MENUBAR_EVENT_UNAVAILABLE';
      this.dataset.widgetState = 'fallback';
      this.dispatchEvent(new CustomEvent('causeway-menubar-load-failed', {
        bubbles: true,
        composed: true,
        detail: Object.freeze({family: 'menubar', phase: 'event', classification, revision: configurationRevision})
      }));
      failCausewayMenubarWidget({phase: 'event', classification});
    }
  }

  #isCurrent(generation, revision) {
    return this.isConnected
      && generation === this._generation
      && revision === configurationRevision
      && configuration.enabled
      && !failed
      && this._projection?.accepted;
  }

  #releaseControl() {
    if (this._control) {
      this._control.removeEventListener('item-selected', this._selectionListener);
      this._control.items = [];
      this._control.parentNode?.removeChild?.(this._control);
    }
    this._control = null;
    if (!this.isConnected) this._focusRequest = null;
  }
}

function enableAccessibleDisabledItems() {
  const vaadin = globalThis.Vaadin ??= {};
  const featureFlags = vaadin.featureFlags ??= {};
  featureFlags.accessibleDisabledButtons = true;
  featureFlags.accessibleDisabledMenuItems = true;
}

function materializeMenuItems(items) {
  return items.map(item => {
    const children = item.children ? materializeMenuItems(item.children) : undefined;
    const materialized = {...item, ...(children ? {children} : {})};
    if (!globalThis.document?.createElement) return materialized;
    if (item.causewaySectionLabel) {
      const sectionLabel = document.createElement('div');
      sectionLabel.className = 'causeway-menubar-section-label';
      sectionLabel.setAttribute('role', 'separator');
      sectionLabel.setAttribute('aria-label', item.text);
      sectionLabel.textContent = item.text;
      materialized.component = sectionLabel;
      return materialized;
    }
    if (item.causewayKind === 'action' || item.title || item.causewayIconHint || item.causewayDisabledReason) {
      const menuItem = document.createElement('vaadin-menu-bar-item');
      if (item.causewayKey) menuItem.dataset.causewayKey = item.causewayKey;
      const label = document.createElement('span');
      label.className = 'causeway-menubar-item-label';
      label.textContent = item.text;
      if (item.title) label.title = item.title;
      if (item.causewayIconHint) label.dataset.iconHint = item.causewayIconHint;
      if (item.causewayDisabledReason) {
        label.setAttribute('aria-label', `${item.text}. Unavailable: ${item.causewayDisabledReason}`);
        label.dataset.disabledReason = item.causewayDisabledReason;
      }
      menuItem.appendChild(label);
      materialized.component = menuItem;
    }
    return materialized;
  });
}

function announcePolicyChange(detail) {
  globalThis.document?.dispatchEvent?.(new CustomEvent(CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT, {
    detail: Object.freeze(detail)
  }));
}

function safeModuleUrl(value) {
  const url = new URL(value, globalThis.document?.baseURI ?? import.meta.url);
  if (!APPROVED_PROTOCOLS.has(url.protocol)) throw new Error('Menu Bar module URL must use an approved module protocol.');
  return url.href;
}

function boundedTimeout(value) {
  if (value == null) return DEFAULT_DEFINITION_TIMEOUT_MS;
  if (!Number.isSafeInteger(value) || value < 1 || value > MAX_DEFINITION_TIMEOUT_MS) {
    throw new Error(`Menu Bar definition timeout must be an integer from 1 through ${MAX_DEFINITION_TIMEOUT_MS}.`);
  }
  return value;
}

function boundedToken(value) {
  return String(value ?? '').replace(/[^A-Za-z0-9_.-]/g, '_').slice(0, 80);
}

async function waitForDefinition(timeoutMs) {
  let timeoutId;
  try {
    await Promise.race([
      globalThis.customElements.whenDefined('vaadin-menu-bar'),
      new Promise((_, reject) => {
        timeoutId = globalThis.setTimeout(() => reject(new Error('Menu Bar custom-element definition timed out.')), timeoutMs);
      })
    ]);
  } finally {
    globalThis.clearTimeout(timeoutId);
  }
}

function humanize(value) {
  return String(value ?? '').replace(/^./, character => character.toUpperCase());
}
