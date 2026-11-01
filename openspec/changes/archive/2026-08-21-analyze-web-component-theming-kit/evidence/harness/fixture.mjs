/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

const harnessBase = '/openspec/changes/analyze-web-component-theming-kit/evidence/harness';
const parameters = new URLSearchParams(globalThis.location.search);
const supportedCandidates = new Set(['baseline', 'bootstrap', 'webawesome', 'openprops']);
const candidate = supportedCandidates.has(parameters.get('candidate')) ? parameters.get('candidate') : 'baseline';
const theme = parameters.get('theme') === 'dark' ? 'dark' : 'light';
const requestedState = parameters.get('state') ?? 'all';
const motion = parameters.get('motion') === 'reduce' ? 'reduce' : 'normal';
let promptInvoker = null;

document.documentElement.dataset.candidate = candidate;
document.documentElement.dataset.theme = theme;
document.documentElement.dataset.motion = motion;
document.documentElement.style.colorScheme = theme;
if (candidate === 'bootstrap') {
  document.documentElement.dataset.bsTheme = theme;
}
if (candidate === 'webawesome') {
  document.documentElement.classList.add(theme === 'dark' ? 'wa-dark' : 'wa-light');
}

await loadCandidateAssets();

const root = document.querySelector('#fixture-root');
root.replaceChildren();
root.insertAdjacentHTML('beforeend', renderFixture());
setupInteractions();
await awaitCandidateDefinitions();
activateRequestedState();

const badge = document.createElement('div');
badge.className = 'prototype-badge';
badge.textContent = `${candidate} · ${theme} · ${requestedState}`;
document.body.append(badge);

await document.fonts?.ready;
window.__causewayFixture = Object.freeze({candidate, theme, requestedState, motion, ready: true});
document.documentElement.dataset.fixtureReady = 'true';
document.dispatchEvent(new CustomEvent('causeway-fixture-ready', {detail: window.__causewayFixture}));

async function loadCandidateAssets() {
  const assets = {
    baseline: [
      '/viewers/webcomponents/foundation/src/theme.css',
      '/viewers/webcomponents/foundation/src/component-styles.css',
      '/viewers/webcomponents/htmx/src/main/resources/META-INF/resources/causeway-htmx/causeway-htmx.css',
      `${harnessBase}/candidates/baseline.css`
    ],
    bootstrap: [
      `${harnessBase}/node_modules/bootstrap/dist/css/bootstrap.min.css`,
      `${harnessBase}/candidates/bootstrap.css`
    ],
    webawesome: [
      `${harnessBase}/node_modules/@awesome.me/webawesome/dist-cdn/styles/webawesome.css`,
      `${harnessBase}/candidates/web-awesome.css`
    ],
    openprops: [
      `${harnessBase}/node_modules/open-props/open-props.min.css`,
      `${harnessBase}/node_modules/open-props/normalize.min.css`,
      `${harnessBase}/node_modules/open-props/buttons.min.css`,
      `${harnessBase}/candidates/open-props.css`
    ]
  };
  await Promise.all(assets[candidate].map(loadStylesheet));
  if (candidate === 'webawesome') {
    const distributionPath = `${harnessBase}/node_modules/@awesome.me/webawesome/dist-cdn`;
    const {setBasePath} = await import(`${distributionPath}/utilities/base-path.js`);
    setBasePath(distributionPath);
    await import(`${distributionPath}/webawesome.loader.js`);
  }
}

function loadStylesheet(href) {
  return new Promise((resolve, reject) => {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = href;
    link.addEventListener('load', resolve, {once: true});
    link.addEventListener('error', () => reject(new Error(`Unable to load ${href}`)), {once: true});
    document.head.append(link);
  });
}

function renderFixture() {
  return `<div class="fixture-app">
    <header class="fixture-header">
      <div class="fixture-navbar">
        <a class="fixture-brand" href="#fixture-main" aria-label="Pet Clinic home">
          <span class="fixture-mark" aria-hidden="true">C</span>
          <span>Pet Clinic</span>
        </a>
        ${control('Application menu', {kind: 'secondary', attributes: 'class="fixture-nav-toggle" data-nav-trigger aria-expanded="false" aria-controls="fixture-navigation"'})}
        <nav id="fixture-navigation" class="fixture-nav" data-collapsed="true" aria-label="Application navigation">
          <causeway-menubars data-prototype-host class="fixture-menus">
            ${renderMenus()}
          </causeway-menubars>
        </nav>
      </div>
    </header>

    <main id="fixture-main" class="fixture-main" tabindex="-1">
      <div class="fixture-heading-row">
        <div>
          <p class="fixture-eyebrow">Pet owner</p>
          <h1>Mary Smith and a deliberately long contextual title</h1>
          <p>Reference object <code>petclinic.PetOwner / s_owner-mary</code></p>
        </div>
        <div class="fixture-actions" aria-label="Object actions">
          ${control('Update name', {kind: 'secondary', attributes: 'data-object-action="update"'})}
          ${control('Book visit', {kind: 'primary', attributes: 'data-object-action="book"'})}
          ${control('Delete', {kind: 'danger', attributes: 'data-object-action="delete"'})}
        </div>
      </div>

      <aside class="fixture-shell-result fixture-status" data-tone="success" aria-live="polite">
        <strong>Action completed.</strong>
        The fixture retains a concise semantic result without replacing the route.
      </aside>

      <div class="fixture-page-grid">
        <causeway-object data-prototype-host class="fixture-card">
          <causeway-object-header data-prototype-host>
            <h2>Owner details</h2>
            <p class="fixture-eyebrow">Ready · editable · fixture state</p>
          </causeway-object-header>
          ${renderTabs()}
        </causeway-object>

        <aside class="fixture-card" aria-labelledby="associated-actions-heading">
          <h2 id="associated-actions-heading">Associated actions</h2>
          <div class="fixture-actions">
            ${control('Add pet', {kind: 'primary'})}
            ${control('Remove pet', {kind: 'secondary'})}
            ${control('Unavailable action', {kind: 'secondary', attributes: 'disabled aria-describedby="disabled-action-reason"'})}
          </div>
          <p id="disabled-action-reason" class="fixture-eyebrow">Available only when a pet is selected.</p>
        </aside>
      </div>

      <causeway-collection data-prototype-host class="fixture-card" aria-labelledby="collection-heading">
        <h2 id="collection-heading">Visits and related records</h2>
        <div class="fixture-table-region" tabindex="0" aria-label="Scrollable visits table">
          ${renderTable()}
        </div>
      </causeway-collection>

      <section class="fixture-card" aria-labelledby="status-heading">
        <h2 id="status-heading">State presentations</h2>
        <div class="fixture-status-grid">
          <div class="fixture-status" role="status"><strong>Loading</strong><br>${candidate === 'webawesome' ? '<wa-spinner></wa-spinner>' : 'Loading object data…'}</div>
          <div class="fixture-status" data-tone="success"><strong>Ready</strong><br>All requested members are available.</div>
          <div class="fixture-status" data-tone="warning"><strong>Partial information</strong><br>One optional collection could not be loaded.</div>
          <div class="fixture-status" data-tone="danger" role="alert"><strong>Validation error</strong><br>Correct the highlighted fields and retry.</div>
          <div class="fixture-status"><strong>Empty</strong><br>No matching records were found.</div>
          <div class="fixture-status" data-tone="danger" role="alert"><strong>Page unavailable</strong><br>The request could not be completed safely.</div>
        </div>
      </section>
    </main>

    <footer class="fixture-footer">Powered by Apache Causeway · analysis fixture only</footer>
    ${renderPrompt()}
  </div>`;
}

function renderMenus() {
  if (candidate === 'webawesome') {
    return `<wa-dropdown data-app-dropdown>
      <wa-button appearance="filled" slot="trigger" with-caret>Pet Owners</wa-button>
      <wa-dropdown-item value="create" data-menu-action="create">Create</wa-dropdown-item>
      <wa-dropdown-item value="find">Find By Name</wa-dropdown-item>
      <wa-dropdown-item value="long">A deliberately long application action label</wa-dropdown-item>
      <wa-dropdown-item value="disabled" disabled>Unavailable action</wa-dropdown-item>
    </wa-dropdown>
    <wa-dropdown>
      <wa-button appearance="plain" slot="trigger" with-caret>Visits</wa-button>
      <wa-dropdown-item value="upcoming">List Upcoming</wa-dropdown-item>
    </wa-dropdown>
    <wa-dropdown>
      <wa-button appearance="plain" slot="trigger" with-caret>System</wa-button>
      <wa-dropdown-item value="about">About</wa-dropdown-item>
    </wa-dropdown>`;
  }
  const popover = candidate === 'openprops' ? 'popover="auto"' : 'hidden';
  const target = candidate === 'openprops' ? 'popovertarget="pet-owner-menu"' : '';
  return `<div class="fixture-menu">
      ${control('Pet Owners', {kind: 'secondary', attributes: `data-menu-trigger ${target} aria-expanded="false" aria-controls="pet-owner-menu"`})}
      <div id="pet-owner-menu" class="fixture-menu-panel" data-menu-panel ${popover} aria-label="Pet Owners actions">
        ${control('Create', {kind: 'primary', attributes: 'data-menu-action="create"'})}
        ${control('Find By Name', {kind: 'secondary', attributes: 'data-menu-action="find"'})}
        ${control('A deliberately long application action label', {kind: 'secondary', attributes: 'data-menu-action="long"'})}
        ${control('Unavailable action', {kind: 'secondary', attributes: 'disabled aria-describedby="menu-disabled-reason"'})}
        <span id="menu-disabled-reason" class="fixture-eyebrow">Requires administrator access.</span>
      </div>
    </div>
    <div class="fixture-menu">${control('Visits', {kind: 'secondary', attributes: 'aria-expanded="false"'})}</div>
    <div class="fixture-menu">${control('System', {kind: 'secondary', attributes: 'aria-expanded="false"'})}</div>`;
}

function renderTabs() {
  if (candidate === 'webawesome') {
    return `<wa-tab-group>
      <wa-tab slot="nav" panel="details">Details</wa-tab>
      <wa-tab slot="nav" panel="notes">Notes and preferences</wa-tab>
      <wa-tab-panel name="details">${renderProperties()}</wa-tab-panel>
      <wa-tab-panel name="notes"><p class="fixture-long-value">This long unbroken value checks containment: abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789.</p></wa-tab-panel>
    </wa-tab-group>`;
  }
  return `<div class="fixture-tabs">
    <div role="tablist" aria-label="Owner sections">
      ${control('Details', {kind: 'secondary', attributes: 'role="tab" id="details-tab" aria-selected="true" aria-controls="details-panel" data-tab="details-panel"'})}
      ${control('Notes and preferences', {kind: 'secondary', attributes: 'role="tab" id="notes-tab" aria-selected="false" aria-controls="notes-panel" data-tab="notes-panel"'})}
    </div>
    <div id="details-panel" role="tabpanel" aria-labelledby="details-tab">${renderProperties()}</div>
    <div id="notes-panel" role="tabpanel" aria-labelledby="notes-tab" hidden><p class="fixture-long-value">This long unbroken value checks containment: abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789.</p></div>
  </div>`;
}

function renderProperties() {
  return `<div class="fixture-properties">
    <span class="fixture-property-label">Name</span><span>Mary Smith</span>
    <span class="fixture-property-label">Known as</span><span>Mary</span>
    <span class="fixture-property-label">Telephone number with a long label</span><span>020 7946 0312</span>
    <span class="fixture-property-label">Email</span><a href="mailto:mary@example.test">mary@example.test</a>
    <span class="fixture-property-label">Notes</span><span>Prefers morning appointments and requires step-free access.</span>
  </div>`;
}

function renderTable() {
  const tableClass = candidate === 'bootstrap' ? 'fixture-table table table-striped table-hover align-middle' : 'fixture-table';
  return `<table class="${tableClass}">
    <thead><tr><th>Pet</th><th>Visit date</th><th>Reason</th><th>Status</th><th>Veterinarian</th><th>Reference</th></tr></thead>
    <tbody>
      <tr><td><a href="#basil">Basil · dog</a></td><td>2026-08-24 09:30</td><td>Routine check-up</td><td>Confirmed</td><td>Dr Green</td><td>visit-2026-000031</td></tr>
      <tr><td><a href="#parsley">Parsley · cat</a></td><td>2026-09-03 15:15</td><td>A deliberately long follow-up description that tests wrapping</td><td>Awaiting notes</td><td>Dr Hernández-Smith</td><td>visit-2026-000032</td></tr>
    </tbody>
  </table>`;
}

function renderPrompt() {
  const fields = candidate === 'webawesome'
    ? `<wa-input label="Name" required value="Invalid % name" data-prompt-field></wa-input>
       <wa-select label="Preferred species" value="DOG"><wa-option value="DOG">Dog</wa-option><wa-option value="CAT">Cat</wa-option></wa-select>
       <wa-textarea label="Notes" rows="4">Requires a quiet waiting area.</wa-textarea>`
    : `<div class="fixture-field"><label for="owner-name">Name</label><input id="owner-name" required value="Invalid % name" aria-invalid="true" aria-describedby="owner-name-error" data-prompt-field><span id="owner-name-error" class="fixture-error" role="alert">Name cannot contain the % character.</span></div>
       <div class="fixture-field-row"><div class="fixture-field"><label for="species">Preferred species</label><select id="species"><option>Dog</option><option>Cat</option></select></div><div class="fixture-field"><label for="visit-date">First visit</label><input id="visit-date" type="date" value="2026-08-24"></div></div>
       <div class="fixture-field"><label for="owner-notes">Notes</label><textarea id="owner-notes" rows="4">Requires a quiet waiting area.</textarea></div>`;
  const content = `<form class="fixture-dialog-form" data-prompt-form>
      <p>Register a new pet owner using representative controls and validation.</p>
      ${fields}
      <div class="fixture-dialog-actions">
        ${control('Cancel', {kind: 'secondary', attributes: 'type="button" data-prompt-cancel'})}
        ${control('Create owner', {kind: 'primary', attributes: 'type="submit"'})}
      </div>
    </form>`;
  if (candidate === 'webawesome') {
    return `<causeway-action-prompt data-prototype-host><wa-dialog label="Create pet owner" data-prompt>${content}</wa-dialog></causeway-action-prompt>`;
  }
  return `<causeway-action-prompt data-prototype-host><dialog data-prompt aria-labelledby="prompt-heading"><h2 id="prompt-heading">Create pet owner</h2>${content}</dialog></causeway-action-prompt>`;
}

function control(label, {kind = 'secondary', attributes = ''} = {}) {
  if (candidate === 'webawesome') {
    const appearance = kind === 'primary' || kind === 'danger' ? 'filled' : 'outlined';
    const variant = kind === 'danger' ? 'danger' : kind === 'primary' ? 'brand' : 'neutral';
    return `<wa-button appearance="${appearance}" variant="${variant}" ${attributes}>${label}</wa-button>`;
  }
  const classes = candidate === 'bootstrap'
    ? `btn ${kind === 'primary' ? 'btn-primary' : kind === 'danger' ? 'btn-danger' : 'btn-outline-secondary'}`
    : 'fixture-button';
  return `<button class="${classes}" data-kind="${kind}" type="button" ${attributes}>${label}</button>`;
}

function setupInteractions() {
  const navTrigger = document.querySelector('[data-nav-trigger]');
  const navigation = document.querySelector('#fixture-navigation');
  navTrigger?.addEventListener('click', () => {
    const expanded = navTrigger.getAttribute('aria-expanded') !== 'true';
    navTrigger.setAttribute('aria-expanded', String(expanded));
    navigation.dataset.collapsed = String(!expanded);
  });

  if (candidate !== 'webawesome' && candidate !== 'openprops') {
    const trigger = document.querySelector('[data-menu-trigger]');
    const panel = document.querySelector('[data-menu-panel]');
    trigger?.addEventListener('click', () => toggleDisclosure(trigger, panel));
    document.addEventListener('click', event => {
      if (trigger?.getAttribute('aria-expanded') === 'true' && !trigger.parentElement.contains(event.target)) {
        closeDisclosure(trigger, panel, false);
      }
    });
  }

  if (candidate === 'openprops') {
    const trigger = document.querySelector('[data-menu-trigger]');
    const panel = document.querySelector('[data-menu-panel]');
    if (!('showPopover' in HTMLElement.prototype)) {
      panel.removeAttribute('popover');
      panel.hidden = true;
      trigger.removeAttribute('popovertarget');
      trigger.addEventListener('click', () => toggleDisclosure(trigger, panel));
      document.documentElement.dataset.popoverFallback = 'true';
    } else {
      trigger.addEventListener('click', () => positionPopover(trigger, panel), {capture: true});
      panel.addEventListener('toggle', event => trigger.setAttribute('aria-expanded', String(event.newState === 'open')));
    }
  }

  document.addEventListener('click', event => {
    const action = event.target.closest?.('[data-menu-action="create"]');
    if (!action) return;
    promptInvoker = document.querySelector('[data-menu-trigger], [data-app-dropdown] wa-button');
    dismissApplicationMenu();
    openPrompt();
  });

  document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') return;
    const trigger = document.querySelector('[data-menu-trigger]');
    const panel = document.querySelector('[data-menu-panel]');
    if (trigger?.getAttribute('aria-expanded') === 'true' && candidate !== 'openprops') {
      event.preventDefault();
      closeDisclosure(trigger, panel, true);
    }
  });

  const prompt = document.querySelector('[data-prompt]');
  prompt?.addEventListener(candidate === 'webawesome' ? 'wa-after-hide' : 'close', () => promptInvoker?.focus());
  document.querySelector('[data-prompt-cancel]')?.addEventListener('click', closePrompt);
  document.querySelector('[data-prompt-form]')?.addEventListener('submit', event => {
    event.preventDefault();
    const invalid = document.querySelector('[aria-invalid="true"], wa-input[required]');
    invalid?.focus?.();
  });

  for (const tab of document.querySelectorAll('[role="tab"][data-tab]')) {
    tab.addEventListener('click', () => selectTab(tab));
    tab.addEventListener('keydown', event => {
      if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) return;
      const tabs = [...document.querySelectorAll('[role="tab"][data-tab]')];
      const direction = event.key === 'ArrowRight' ? 1 : -1;
      const next = tabs[(tabs.indexOf(tab) + direction + tabs.length) % tabs.length];
      event.preventDefault();
      selectTab(next);
      next.focus();
    });
  }
}

function toggleDisclosure(trigger, panel) {
  const expanded = trigger.getAttribute('aria-expanded') !== 'true';
  trigger.setAttribute('aria-expanded', String(expanded));
  panel.hidden = !expanded;
}

function closeDisclosure(trigger, panel, focus) {
  trigger?.setAttribute('aria-expanded', 'false');
  if (panel) panel.hidden = true;
  if (focus) trigger?.focus();
}

function dismissApplicationMenu() {
  if (candidate === 'webawesome') {
    const dropdown = document.querySelector('[data-app-dropdown]');
    if (dropdown) dropdown.open = false;
    return;
  }
  const trigger = document.querySelector('[data-menu-trigger]');
  const panel = document.querySelector('[data-menu-panel]');
  if (candidate === 'openprops' && panel?.matches(':popover-open')) {
    panel.hidePopover();
    trigger?.setAttribute('aria-expanded', 'false');
  } else {
    closeDisclosure(trigger, panel, false);
  }
}

function positionPopover(trigger, panel) {
  const rect = trigger.getBoundingClientRect();
  panel.style.insetInlineStart = `${Math.max(8, rect.left)}px`;
  panel.style.insetBlockStart = `${rect.bottom + 4}px`;
}

function openPrompt() {
  const prompt = document.querySelector('[data-prompt]');
  if (!prompt) return;
  if (candidate === 'webawesome') {
    prompt.addEventListener('wa-after-show', () => prompt.querySelector('[data-prompt-field]')?.focus(), {once: true});
    prompt.open = true;
  } else if (!prompt.open) {
    prompt.showModal();
    prompt.querySelector('[data-prompt-field]')?.focus();
  }
}

function closePrompt() {
  const prompt = document.querySelector('[data-prompt]');
  if (!prompt) return;
  if (candidate === 'webawesome') prompt.open = false;
  else if (prompt.open) prompt.close();
  promptInvoker?.focus();
}

function selectTab(selected) {
  for (const tab of document.querySelectorAll('[role="tab"][data-tab]')) {
    const active = tab === selected;
    tab.setAttribute('aria-selected', String(active));
    document.getElementById(tab.dataset.tab).hidden = !active;
  }
}

async function awaitCandidateDefinitions() {
  if (candidate !== 'webawesome') return;
  await Promise.all(['wa-button', 'wa-dialog', 'wa-dropdown', 'wa-dropdown-item', 'wa-input', 'wa-option', 'wa-select', 'wa-spinner', 'wa-tab', 'wa-tab-group', 'wa-tab-panel', 'wa-textarea']
    .map(name => customElements.whenDefined(name)));
}

function activateRequestedState() {
  if (requestedState === 'menu-open' || requestedState === 'responsive-nav') {
    const navTrigger = document.querySelector('[data-nav-trigger]');
    const navigation = document.querySelector('#fixture-navigation');
    navTrigger?.setAttribute('aria-expanded', 'true');
    if (navigation) navigation.dataset.collapsed = 'false';
    if (candidate === 'webawesome') {
      const dropdown = document.querySelector('[data-app-dropdown]');
      if (dropdown) dropdown.open = true;
    } else if (candidate === 'openprops') {
      const trigger = document.querySelector('[data-menu-trigger]');
      const panel = document.querySelector('[data-menu-panel]');
      if (panel && 'showPopover' in panel) {
        positionPopover(trigger, panel);
        panel.showPopover();
        trigger.setAttribute('aria-expanded', 'true');
      } else {
        toggleDisclosure(trigger, panel);
      }
    } else {
      toggleDisclosure(document.querySelector('[data-menu-trigger]'), document.querySelector('[data-menu-panel]'));
    }
  }
  if (requestedState === 'prompt') openPrompt();
}
