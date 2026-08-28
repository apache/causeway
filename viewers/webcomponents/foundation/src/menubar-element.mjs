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

import {CausewayElementName, CausewaySemanticEvent} from './component-contracts.mjs';
import {
  createSemanticEvent,
  MENU_BARS_DIAGNOSTIC_EVENT,
  MENU_BARS_STATE_EVENT,
  requestGraphQLClient,
  requestMenuBarsContext
} from './context-events.mjs';
import {MenuBarsContextController, MenuBarsStatus} from './menu-context-controller.mjs';
import {projectCausewayMenuBar} from './menubar-projection.mjs';
import {qualifyCausewayMenuBar} from './menubar-qualification.mjs';
import {
  CAUSEWAY_MENUBAR_CONTROL,
  CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT,
  causewayMenubarWidgetConfiguration
} from './menubar-widget.mjs';
import {escapeHtml} from './rendering.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
let barSequence = 0;

export class CausewayMenubarElement extends HTMLElementBase {
  constructor() {
    super();
    this.sequence = ++barSequence;
    this._injectedContext = null;
    this._context = null;
    this._privateContext = null;
    this._release = null;
    this._client = null;
    this._fetchImpl = null;
    this.lastDiagnosticGeneration = -1;
    this._currentState = null;
    this._currentBar = null;
    this._projection = null;
    this._presentation = 'native';
    this._qualificationReason = null;
    this._responsiveRevision = 0;
    this._resizeObserver = null;
    this._resizeScheduled = false;
    this._semanticFocusIntent = null;
    this.onPolicyChange = event => {
      if (event.detail?.family === 'menubar' && this.isConnected) this.#renderCurrentReadyState();
    };
    this.onOutsideClick = event => {
      if (!this.contains?.(event.target)) {
        this.#closeExpandedMenus();
      }
    };
    this.addEventListener('click', event => this.#handleClick(event));
    this.addEventListener('keydown', event => this.#handleKeydown(event));
    this.addEventListener('focusout', event => this.#handleFocusout(event));
  }

  get role() {
    return this.constructor.barRole;
  }

  get context() {
    return this._injectedContext;
  }

  set context(value) {
    this._injectedContext = value ?? null;
    if (this.isConnected) {
      this.#connectContext();
    }
  }

  get client() {
    return this._client;
  }

  set client(value) {
    this._client = value ?? null;
    if (this.isConnected && !this._injectedContext) {
      this.#connectContext();
    }
  }

  get fetchImpl() {
    return this._fetchImpl;
  }

  set fetchImpl(value) {
    this._fetchImpl = value ?? null;
  }

  connectedCallback() {
    globalThis.document?.addEventListener?.('click', this.onOutsideClick);
    globalThis.document?.addEventListener?.(CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT, this.onPolicyChange);
    if (globalThis.ResizeObserver) {
      this._resizeObserver = new ResizeObserver(() => this.#scheduleResponsiveRender());
      this._resizeObserver.observe(this);
    }
    this.#connectContext();
  }

  disconnectedCallback() {
    globalThis.document?.removeEventListener?.('click', this.onOutsideClick);
    globalThis.document?.removeEventListener?.(CAUSEWAY_MENUBAR_WIDGET_POLICY_EVENT, this.onPolicyChange);
    this._resizeObserver?.disconnect();
    this._resizeObserver = null;
    this._responsiveRevision += 1;
    this._currentState = null;
    this._currentBar = null;
    this._projection = null;
    this._release?.();
    this._release = null;
    this._privateContext?.disconnect();
    this._privateContext = null;
    this._context = null;
  }

  refresh() {
    return this._context?.refresh() ?? Promise.resolve(null);
  }

  #connectContext() {
    this._release?.();
    this._release = null;
    this._privateContext?.disconnect();
    this._privateContext = null;
    this._context = this._injectedContext ?? requestMenuBarsContext(this);
    if (!this._context) {
      const client = this._client ?? requestGraphQLClient(this);
      this._privateContext = new MenuBarsContextController({
        client,
        fetchImpl: this._fetchImpl ?? globalThis.fetch
      });
      this._context = this._privateContext;
    }
    this._release = this._context.subscribe(state => this.#renderState(state));
    if (this._privateContext) {
      void this._privateContext.refresh();
    }
  }

  #renderState(state) {
    const renderRoot = this.#renderRoot();
    this._currentState = state;
    this.setAttribute('data-menu-state', state.status);
    this.setAttribute('data-causeway-bar', this.role);
    const bar = state.plan?.bars?.[this.role] ?? null;
    this._currentBar = bar;
    if (isLoading(state.status)) {
      this.hidden = false;
      renderRoot.innerHTML = `<div class="causeway-menubar-status causeway-loading" role="status">Loading ${escapeHtml(this.role)} application menu…</div>`;
    } else if (state.status === MenuBarsStatus.TERMINAL_ERROR || state.status === MenuBarsStatus.UNSUPPORTED) {
      this.hidden = false;
      const message = state.status === MenuBarsStatus.UNSUPPORTED
        ? 'Application menus are unavailable.'
        : 'The application menu could not be loaded.';
      renderRoot.innerHTML = `<div class="causeway-menubar-status causeway-error" role="alert">${message}</div>`;
      this.#restoreSemanticFocus(renderRoot);
    } else if (!bar || bar.menus.length === 0) {
      this.hidden = true;
      renderRoot.innerHTML = '';
      this.#restoreSemanticFocus(renderRoot);
    } else {
      this.hidden = false;
      this.#renderReadyBar(renderRoot, bar, state);
    }
    this.dispatchEvent(createSemanticEvent(MENU_BARS_STATE_EVENT, Object.freeze({
      element: this,
      role: this.role,
      state
    })));
    if (this._privateContext && state.generation !== this.lastDiagnosticGeneration && state.diagnostics.length > 0) {
      this.lastDiagnosticGeneration = state.generation;
      for (const diagnostic of state.diagnostics) {
        this.dispatchEvent(createSemanticEvent(MENU_BARS_DIAGNOSTIC_EVENT, Object.freeze({
          element: this,
          role: this.role,
          generation: state.generation,
          diagnostic
        })));
      }
    }
    if (this._privateContext) {
      this.#ensureInteractionController();
    }
  }

  #renderReadyBar(renderRoot, bar, state) {
    const widgetPolicy = causewayMenubarWidgetConfiguration();
    const projection = projectCausewayMenuBar(bar, {
      generation: state.generation,
      excludeAction: widgetPolicy.excludeAction
    });
    const qualification = qualifyCausewayMenuBar({
      role: this.role,
      generation: state.generation,
      policy: widgetPolicy.enabled ? 'vaadin' : 'native',
      familyAvailable: widgetPolicy.failed !== true,
      connected: this.isConnected,
      visible: true,
      current: state === this._currentState,
      projection,
      width: this.getBoundingClientRect?.().width ?? 0
    });
    this._projection = projection;
    if (!projection.accepted && projection.reason === 'empty') {
      this.hidden = true;
      renderRoot.innerHTML = '';
      this._presentation = 'native';
      this._qualificationReason = 'empty';
      this.dataset.causewayMenubarPresentation = 'native';
      this.dataset.causewayMenubarFallback = 'empty';
      this.#restoreSemanticFocus(renderRoot);
      return;
    }
    this._presentation = qualification.presentation;
    this._qualificationReason = qualification.reason;
    this.dataset.causewayMenubarPresentation = qualification.presentation;
    this.dataset.causewayMenubarResponsive = qualification.presentation === 'vaadin-overflow' ? 'narrow' : 'wide';
    this.dataset.causewayMenubarFallback = qualification.reason ?? '';
    if (!qualification.accepted) {
      renderRoot.innerHTML = renderBar(bar, this.sequence);
      this.#restoreSemanticFocus(renderRoot);
      return;
    }
    const label = `${humanize(this.role)} application menu`;
    renderRoot.innerHTML = `<div class="causeway-menubar-shell causeway-menubar-toolkit" data-causeway-bar-role="${escapeHtml(this.role)}"><nav class="causeway-menubar causeway-menubar-${escapeHtml(this.role)}" aria-label="${escapeHtml(label)}"><${CAUSEWAY_MENUBAR_CONTROL} data-causeway-menubar-tier="${escapeHtml(this.role)}"></${CAUSEWAY_MENUBAR_CONTROL}></nav></div>`;
    const control = renderRoot.querySelector?.(CAUSEWAY_MENUBAR_CONTROL);
    if (control) {
      control.presentation = Object.freeze({
        projection,
        overflowLabel: overflowLabel(globalThis.document?.documentElement?.lang),
        activate: descriptor => this.#activateServiceAction(descriptor.serviceLogicalTypeName, descriptor.actionId, control)
      });
      this.#restoreSemanticFocus(renderRoot, control);
    }
  }

  #restoreSemanticFocus(renderRoot, toolkitControl = null) {
    const intent = this._semanticFocusIntent;
    if (!intent) return;
    this._semanticFocusIntent = null;
    queueMicrotask(() => {
      if (!this.isConnected) return;
      if (toolkitControl?.isConnected) {
        toolkitControl.focus?.({preventScroll: true});
        return;
      }
      const action = [...(renderRoot.querySelectorAll?.('[data-causeway-service-action]') ?? [])]
        .find(candidate => candidate.getAttribute('data-service-logical-type') === intent.serviceLogicalTypeName
          && candidate.getAttribute('data-action-id') === intent.actionId
          && !candidate.disabled);
      const fallback = action
        ?? renderRoot.querySelector?.('[data-causeway-menu-disclosure]')
        ?? renderRoot.querySelector?.('[data-causeway-bar-disclosure]')
        ?? globalThis.document?.querySelector?.('.causeway-shell-brand');
      fallback?.focus?.({preventScroll: true});
    });
  }

  #renderCurrentReadyState() {
    if (!this.isConnected || !this._currentState || !this._currentBar || !isReady(this._currentState.status)) return;
    this.#renderReadyBar(this.#renderRoot(), this._currentBar, this._currentState);
  }

  #scheduleResponsiveRender() {
    if (this._resizeScheduled) return;
    this._resizeScheduled = true;
    const revision = ++this._responsiveRevision;
    globalThis.setTimeout(() => {
      this._resizeScheduled = false;
      if (!this.isConnected || revision !== this._responsiveRevision) return;
      const width = this.getBoundingClientRect?.().width ?? 0;
      const responsive = width > 0 && width <= 768 ? 'narrow' : 'wide';
      this.dataset.causewayMenubarResponsive = responsive;
      if (this._qualificationReason === 'width-unavailable' && width > 0) {
        this.#renderCurrentReadyState();
        return;
      }
      if (this._presentation.startsWith('vaadin-')) {
        this._presentation = responsive === 'narrow' ? 'vaadin-overflow' : 'vaadin-wide';
        this.dataset.causewayMenubarPresentation = this._presentation;
      }
    });
  }

  #handleClick(event) {
    const menuButton = event.target?.closest?.('[data-causeway-menu-disclosure]');
    if (menuButton && this.contains?.(menuButton)) {
      this.#toggleMenu(menuButton);
      return;
    }
    const barButton = event.target?.closest?.('[data-causeway-bar-disclosure]');
    if (barButton && this.contains?.(barButton)) {
      this.#toggleDisclosure(barButton);
      return;
    }
    const actionButton = event.target?.closest?.('[data-causeway-service-action]');
    if (!actionButton || !this.contains?.(actionButton) || actionButton.disabled) {
      return;
    }
    const serviceLogicalTypeName = actionButton.getAttribute('data-service-logical-type');
    const actionId = actionButton.getAttribute('data-action-id');
    if (!serviceLogicalTypeName || !actionId) {
      return;
    }
    const panel = actionButton.closest?.('[data-causeway-menu-panel]');
    const disclosure = panel?.id
      ? this.querySelector?.(`[data-causeway-menu-disclosure][aria-controls="${globalThis.CSS?.escape ? CSS.escape(panel.id) : panel.id}"]`)
      : null;
    if (disclosure) {
      this.#closeMenu(disclosure, {focus: true});
    }
    this.#activateServiceAction(serviceLogicalTypeName, actionId, actionButton);
  }

  #activateServiceAction(serviceLogicalTypeName, actionId, origin) {
    if (!serviceLogicalTypeName || !actionId || !this._context) return;
    const context = this._context.serviceContext(serviceLogicalTypeName);
    this._semanticFocusIntent = Object.freeze({role: this.role, actionId, serviceLogicalTypeName, generation: this._currentState?.generation ?? 0});
    origin.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.ACTION_REQUEST, Object.freeze({
      actionId,
      serviceLogicalTypeName,
      target: context.interactionTarget,
      identity: null,
      context
    }), {cancelable: true}));
  }

  #handleFocusout(event) {
    if (event.relatedTarget && this.contains?.(event.relatedTarget)) {
      return;
    }
    this.#closeExpandedMenus();
  }

  #handleKeydown(event) {
    const menuButton = event.target?.closest?.('[data-causeway-menu-disclosure]');
    const actionButton = event.target?.closest?.('[data-causeway-service-action]');
    if (event.key === 'Escape') {
      const expanded = this.querySelector?.('[data-causeway-menu-disclosure][aria-expanded="true"]');
      if (expanded) {
        event.preventDefault();
        event.stopPropagation();
        this.#closeMenu(expanded, {focus: true});
        return;
      }
      const barButton = this.querySelector?.('[data-causeway-bar-disclosure][aria-expanded="true"]');
      if (barButton) {
        event.preventDefault();
        event.stopPropagation();
        this.#toggleDisclosure(barButton, false);
        barButton.focus?.();
      }
      return;
    }
    if (menuButton) {
      const peers = [...(this.querySelectorAll?.('[data-causeway-menu-disclosure]') ?? [])];
      const index = peers.indexOf(menuButton);
      let target = null;
      if (event.key === 'ArrowRight') {
        target = peers[(index + 1) % peers.length];
      } else if (event.key === 'ArrowLeft') {
        target = peers[(index - 1 + peers.length) % peers.length];
      } else if (event.key === 'Home') {
        target = peers[0];
      } else if (event.key === 'End') {
        target = peers.at(-1);
      } else if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.#toggleMenu(menuButton, true);
        const panel = this.#controlledElement(menuButton);
        panel?.querySelector?.('[data-causeway-service-action]:not([disabled])')?.focus?.();
        return;
      }
      if (target) {
        event.preventDefault();
        target.focus?.();
      }
      return;
    }
    if (actionButton && ['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) {
      const panel = actionButton.closest?.('[data-causeway-menu-panel]');
      const actions = [...(panel?.querySelectorAll?.('[data-causeway-service-action]:not([disabled])') ?? [])];
      const index = actions.indexOf(actionButton);
      const target = event.key === 'Home' ? actions[0]
        : event.key === 'End' ? actions.at(-1)
          : event.key === 'ArrowDown' ? actions[(index + 1) % actions.length]
            : actions[(index - 1 + actions.length) % actions.length];
      if (target) {
        event.preventDefault();
        target.focus?.();
      }
    }
  }

  #toggleMenu(button, expanded = button.getAttribute('aria-expanded') !== 'true') {
    for (const sibling of this.querySelectorAll?.('[data-causeway-menu-disclosure][aria-expanded="true"]') ?? []) {
      if (sibling !== button) {
        this.#closeMenu(sibling);
      }
    }
    this.#toggleDisclosure(button, expanded);
  }

  #closeMenu(button, {focus = false} = {}) {
    this.#toggleDisclosure(button, false);
    if (focus) {
      button.focus?.();
    }
  }

  #closeExpandedMenus() {
    for (const expanded of this.querySelectorAll?.('[data-causeway-menu-disclosure][aria-expanded="true"]') ?? []) {
      this.#closeMenu(expanded);
    }
  }

  #toggleDisclosure(button, expanded = button.getAttribute('aria-expanded') !== 'true') {
    button.setAttribute('aria-expanded', String(expanded));
    const controlled = this.#controlledElement(button);
    if (controlled) {
      controlled.hidden = !expanded;
    }
  }

  #controlledElement(button) {
    const id = button.getAttribute('aria-controls');
    return id ? this.querySelector?.(`#${globalThis.CSS?.escape ? CSS.escape(id) : id}`) : null;
  }

  #renderRoot() {
    let root = [...(this.childNodes ?? [])]
      .find(child => child.getAttribute?.('data-causeway-menubar-render') === 'true');
    if (!root && globalThis.document?.createElement) {
      root = document.createElement('div');
      root.setAttribute('data-causeway-menubar-render', 'true');
      this.appendChild(root);
    }
    return root ?? this;
  }

  #ensureInteractionController() {
    const existing = [...(this.childNodes ?? [])]
      .find(child => child.localName === CausewayElementName.INTERACTION_CONTROLLER);
    if (!existing && globalThis.document?.createElement) {
      const controller = document.createElement(CausewayElementName.INTERACTION_CONTROLLER);
      controller.setAttribute('data-causeway-generated', 'menubar-interaction-controller');
      this.appendChild(controller);
    }
  }
}

export class CausewayMenubarPrimaryElement extends CausewayMenubarElement {
  static barRole = 'primary';
}

export class CausewayMenubarSecondaryElement extends CausewayMenubarElement {
  static barRole = 'secondary';
}

export class CausewayMenubarTertiaryElement extends CausewayMenubarElement {
  static barRole = 'tertiary';
}

function renderBar(bar, sequence) {
  const label = `${humanize(bar.role)} application menu`;
  const contentId = `causeway-menubar-${sequence}-${bar.role}`;
  return `<div class="causeway-menubar-shell" data-causeway-bar-role="${escapeHtml(bar.role)}">
  <button class="causeway-menubar-bar-disclosure" type="button" data-causeway-bar-disclosure aria-expanded="true" aria-controls="${contentId}">${escapeHtml(label)}</button>
  <nav class="causeway-menubar causeway-menubar-${escapeHtml(bar.role)}" id="${contentId}" data-causeway-bar-content aria-label="${escapeHtml(label)}">
    ${bar.menus.map((menu, index) => renderMenu(menu, bar.role, sequence, index)).join('')}
  </nav>
</div>`;
}

function renderMenu(menu, role, sequence, index) {
  const panelId = `causeway-menu-${sequence}-${role}-${index}`;
  const description = menu.description ? ` title="${escapeHtml(menu.description)}"` : '';
  return `<section class="causeway-menu" data-causeway-menu="${index}"${dataHint('icon-hint', menu.iconHint)}>
  <button class="causeway-menu-disclosure" type="button" data-causeway-menu-disclosure aria-expanded="false" aria-controls="${panelId}"${description}>${renderIcon(menu.iconHint)}${escapeHtml(menu.label)}</button>
  <div class="causeway-menu-panel" id="${panelId}" data-causeway-menu-panel hidden>
    ${menu.sections.map((section, sectionIndex) => renderSection(section, sequence, role, index, sectionIndex)).join('')}
  </div>
</section>`;
}

function renderSection(section, sequence, role, menuIndex, sectionIndex) {
  const heading = section.label
    ? `<h2 class="causeway-menu-section-label">${escapeHtml(section.label)}</h2>`
    : '';
  return `<section class="causeway-menu-section" data-causeway-menu-section="${sectionIndex}">${heading}${section.actions
    .map((action, actionIndex) => renderAction(action, sequence, role, menuIndex, sectionIndex, actionIndex))
    .join('')}</section>`;
}

function renderAction(action, sequence, role, menuIndex, sectionIndex, actionIndex) {
  const label = action.label || humanize(action.actionId);
  const reasonId = `causeway-service-action-reason-${sequence}-${role}-${menuIndex}-${sectionIndex}-${actionIndex}`;
  const describedBy = action.disabled ? ` aria-describedby="${reasonId}"` : '';
  const disabled = action.disabled ? ' disabled aria-disabled="true"' : '';
  const title = action.description ? ` title="${escapeHtml(action.description)}"` : '';
  const reason = action.disabled
    ? `<span class="causeway-disabled-reason causeway-visually-hidden" id="${reasonId}">${escapeHtml(action.disabled)}</span>`
    : '';
  return `<div class="causeway-service-action" data-causeway-service-action-region${dataHint('css-hint', action.cssHint)}${dataHint('icon-hint', action.iconHint)}>
  <button class="causeway-service-action-control" type="button" data-causeway-service-action data-service-logical-type="${escapeHtml(action.serviceLogicalTypeName)}" data-action-id="${escapeHtml(action.actionId)}"${disabled}${describedBy}${title}>${renderIcon(action.iconHint)}${escapeHtml(label)}</button>${reason}
</div>`;
}

function renderIcon(iconHint) {
  return iconHint
    ? `<span class="causeway-menu-icon" data-icon-hint="${escapeHtml(iconHint)}" aria-hidden="true"></span>`
    : '';
}

function dataHint(name, value) {
  return value ? ` data-${name}="${escapeHtml(value)}"` : '';
}

function isLoading(status) {
  return [
    MenuBarsStatus.IDLE,
    MenuBarsStatus.APPLICATION_LOADING,
    MenuBarsStatus.RESOURCE_LOADING,
    MenuBarsStatus.SERVICE_LOADING
  ].includes(status);
}

function overflowLabel(language) {
  const labels = {de: 'Weitere Optionen', es: 'Más opciones', fr: 'Plus d’options', nl: 'Meer opties'};
  return labels[String(language ?? '').toLowerCase().split('-')[0]] ?? 'More options';
}

function isReady(status) {
  return status === MenuBarsStatus.READY || status === MenuBarsStatus.PARTIAL_ERROR;
}

function humanize(value) {
  return String(value ?? '')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replaceAll('_', ' ')
    .replace(/^./, first => first.toUpperCase());
}
