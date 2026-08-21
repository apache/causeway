/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {existsSync, mkdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {spawn} from 'node:child_process';
import {chromium} from 'playwright';

const directory = fileURLToPath(new URL('.', import.meta.url));
const evidence = resolve(directory, '..');
const resultsDirectory = resolve(evidence, 'results');
const screenshotsDirectory = resolve(evidence, 'screenshots');
const origin = process.env.HARNESS_ORIGIN ?? 'http://127.0.0.1:4184';
const writeEvidence = process.argv.includes('--write');
const chromeCandidates = [
  process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE,
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium'
].filter(Boolean);
const executablePath = chromeCandidates.find(existsSync);
let server;

if (!(await reachable(origin))) {
  server = spawn(process.execPath, [resolve(directory, 'server.mjs')], {
    cwd: directory,
    detached: false,
    stdio: ['ignore', 'pipe', 'pipe'],
    env: {...process.env, HOST: new URL(origin).hostname, PORT: new URL(origin).port || '4184'}
  });
  let serverError = '';
  server.stderr.on('data', data => { serverError += data; });
  try {
    await waitForServer(origin, server);
  } catch (error) {
    server.kill();
    throw new Error(`${error.message}${serverError ? `\n${serverError}` : ''}`);
  }
}

const browser = await chromium.launch({headless: true, executablePath});
try {
  const journey = await runJourney(browser);
  const scenarios = [];
  const definitions = [
    {name: 'generic-desktop-light', mode: 'generic', width: 1440, height: 1000, colorScheme: 'light'},
    {name: 'semantic-desktop-dark', mode: 'semantic', width: 1440, height: 1000, colorScheme: 'dark'},
    {name: 'raw-desktop-light', mode: 'raw', width: 1440, height: 1000, colorScheme: 'light'},
    {name: 'generic-narrow-light', mode: 'generic', width: 390, height: 844, colorScheme: 'light'},
    {name: 'semantic-narrow-dark', mode: 'semantic', width: 390, height: 844, colorScheme: 'dark'},
    {name: 'generic-forced-colors', mode: 'generic', width: 390, height: 844, colorScheme: 'light', forcedColors: 'active'}
  ];
  for (const definition of definitions) scenarios.push(await captureScenario(browser, definition));
  const timings = [];
  for (let index = 0; index < 5; index += 1) timings.push(await measureRun(browser));
  const result = {
    generatedAt: new Date().toISOString(),
    environment: {browser: await browser.version(), executablePath: executablePath ?? 'playwright-managed', origin, headless: true},
    journey,
    scenarios,
    performance: {runs: timings, medians: medianObject(timings)}
  };
  if (writeEvidence) {
    mkdirSync(resultsDirectory, {recursive: true});
    writeFileSync(resolve(resultsDirectory, 'browser-evidence.json'), `${JSON.stringify(result, null, 2)}\n`);
    writeFileSync(resolve(resultsDirectory, 'browser-evidence.md'), markdown(result));
  }
  console.log(JSON.stringify({assertions: journey.assertions, scenarios: scenarios.map(item => ({name: item.name, violations: item.axe.violations.length, overflow: item.overflow, externalRequests: item.externalRequests.length})), medians: result.performance.medians}, null, 2));
} finally {
  await browser.close();
  server?.kill();
}

async function runJourney(browser) {
  const context = await browser.newContext({viewport: {width: 1280, height: 900}, colorScheme: 'light', reducedMotion: 'reduce'});
  const page = await context.newPage();
  const consoleErrors = [];
  const pageErrors = [];
  const requests = [];
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('request', request => requests.push(request.url()));
  await page.goto(origin, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => document.documentElement.dataset.analysisReady === 'true');

  const initial = await page.evaluate(() => ({
    standalone: window.analysis.standalone,
    flowRuntime: window.analysis.flowRuntime,
    flowClients: Boolean(window.Vaadin?.Flow?.clients),
    contextCount: document.querySelectorAll('#analysis-route > causeway-object-context-stub').length,
    defined: ['vaadin-combo-box', 'vaadin-multi-select-combo-box', 'vaadin-grid', 'vaadin-date-picker', 'vaadin-dialog'].every(name => Boolean(customElements.get(name)))
  }));

  await page.evaluate(() => {
    const combo = document.querySelector('causeway-vaadin-reference').combo;
    combo.inputElement.focus();
    combo.opened = true;
  });
  await page.keyboard.type('Owner 019');
  await page.waitForTimeout(500);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForTimeout(80);
  const keyboardSingle = await page.evaluate(() => ({selected: document.querySelector('causeway-vaadin-reference').combo.selectedItem?.id ?? null, active: document.activeElement?.localName}));
  await page.keyboard.press('Escape');
  await page.evaluate(() => document.querySelector('causeway-vaadin-reference').combo.close());

  const keyboardMultiControl = page.locator('causeway-vaadin-multi-reference vaadin-multi-select-combo-box').first();
  await keyboardMultiControl.click();
  await page.keyboard.type('Owner 002');
  await page.waitForTimeout(500);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForTimeout(80);
  await page.keyboard.press('Escape');
  await page.waitForTimeout(80);
  const keyboardMulti = await page.evaluate(() => {
    const combo = document.querySelector('causeway-vaadin-multi-reference').combo;
    const result = {selected: combo.selectedItems.map(item => item.id), closedByEscape: combo.opened === false};
    combo.close();
    return result;
  });
  await page.evaluate(() => window.analysis.renderRoute('generic', {focus: false}));
  await page.waitForTimeout(150);

  const gridFocusTarget = await page.evaluate(() => {
    const grid = document.querySelector('causeway-vaadin-grid').grid;
    const target = grid.shadowRoot?.querySelector('[tabindex="0"]') ?? grid;
    target.focus();
    return {localName: target.localName, part: target.getAttribute('part')};
  });
  await page.keyboard.press('ArrowDown');
  const keyboardGrid = await page.evaluate(target => ({target, active: document.activeElement?.localName, shadowActive: document.querySelector('causeway-vaadin-grid').grid.shadowRoot?.activeElement?.localName ?? null}), gridFocusTarget);

  const single = await page.evaluate(() => new Promise(resolve => {
    const combo = document.querySelector('causeway-vaadin-reference').combo;
    combo.dataProvider({filter: 'Owner 019', page: 0, pageSize: 25}, (items, totalCount) => resolve({items: items.map(item => item.id), totalCount}));
  }));
  const multi = await page.evaluate(() => {
    const wrapper = document.querySelector('causeway-vaadin-multi-reference');
    const items = window.analysis.referenceItems.slice(0, 3);
    wrapper.combo.selectedItems = items;
    wrapper.combo.dispatchEvent(new CustomEvent('selected-items-changed', {detail: {value: items}}));
    return {selected: wrapper.combo.selectedItems.map(item => item.id)};
  });
  const grid = await page.evaluate(() => new Promise(resolve => {
    const control = document.querySelector('causeway-vaadin-grid').grid;
    control.dataProvider({page: 2, pageSize: 50, sortOrders: [], filters: []}, (items, totalCount) => resolve({first: items[0].id, count: items.length, totalCount}));
  }));
  const unsupportedGrid = await page.evaluate(async () => {
    const result = await window.analysis.adapter.collectionWindow({offset: 0, size: 25, sortOrders: [{path: 'name', direction: 'asc'}], filters: [{path: 'status', value: 'Complete'}]});
    const gapRecorded = window.analysis.adapter.events.some(event => event.kind === 'graphql-collection-window-start' && (event.unsupportedSortOrders?.length ?? 0) > 0 && (event.unsupportedFilters?.length ?? 0) > 0);
    return {result, gapRecorded};
  });
  const stale = await page.evaluate(() => window.analysis.runStaleSearchTest());
  const representativeStates = await page.evaluate(() => window.analysis.runRepresentativeStateTest());
  const validation = await page.evaluate(() => {
    const field = document.querySelector('vaadin-text-field[required]');
    field.value = '';
    field.dispatchEvent(new CustomEvent('value-changed', {detail: {value: ''}}));
    return {invalid: field.invalid, errorMessage: field.errorMessage};
  });
  await page.locator('[data-open-action]').click();
  await page.waitForFunction(() => document.querySelector('[data-action-dialog]')?.opened === true);
  const actionOpen = await page.evaluate(() => ({opened: document.querySelector('[data-action-dialog]').opened, openEvents: window.analysis.adapter.events.filter(event => event.kind === 'action-open').length}));
  await page.keyboard.press('Escape');
  await page.waitForFunction(() => document.querySelector('[data-action-dialog]')?.opened === false);
  const actionClose = await page.evaluate(() => ({opened: document.querySelector('[data-action-dialog]').opened, focusRestored: document.activeElement?.hasAttribute('data-open-action') ?? false}));
  await page.evaluate(() => window.analysis.renderRoute('semantic'));
  const semantic = await page.evaluate(() => ({mode: window.analysis.mode, contexts: document.querySelectorAll('#analysis-route > causeway-object-context-stub').length, wrappers: document.querySelectorAll('[data-analysis-adapter]').length}));
  await page.evaluate(() => window.analysis.renderRoute('raw'));
  const raw = await page.evaluate(() => ({mode: window.analysis.mode, contexts: document.querySelectorAll('#analysis-route > causeway-object-context-stub').length, rawWidgets: document.querySelectorAll('[data-raw-combo], [data-raw-multi], [data-raw-grid]').length}));
  const lifecycle = await page.evaluate(() => window.analysis.runLifecycle(20));
  const externalRequests = requests.filter(url => new URL(url).origin !== origin);
  const graphqlEvents = await page.evaluate(() => window.analysis.adapter.events.filter(event => event.kind.startsWith('graphql-') || event.kind.startsWith('adapter-')).slice(-40));
  const assertions = {
    standaloneNoFlow: initial.standalone && !initial.flowRuntime && !initial.flowClients,
    oneRouteContext: initial.contextCount === 1 && semantic.contexts === 1 && raw.contexts === 1 && lifecycle.contextCount === 1,
    componentsDefined: initial.defined,
    keyboardSingleReference: keyboardSingle.selected?.endsWith(':19') === true,
    keyboardMultiReference: keyboardMulti.selected.some(id => id.endsWith(':2')),
    keyboardGridFocus: keyboardGrid.active === 'vaadin-grid' || keyboardGrid.shadowActive != null,
    singleReference: single.totalCount > 0 && single.items.some(id => id.endsWith(':19')),
    multiReference: multi.selected.length === 3,
    gridWindow: grid.count === 50 && grid.totalCount === 1200 && grid.first.endsWith(':101'),
    sortFilterGapRecorded: unsupportedGrid.result.totalCount === 1200 && unsupportedGrid.gapRecorded,
    staleSearchSuppressed: stale.old === 'AbortError' && stale.staleApplied === false,
    representativeStates: representativeStates.partialChoices === 2 && representativeStates.choiceError.includes('failure') && representativeStates.partialWindow.includes('partial') && representativeStates.collectionError.includes('failure') && Object.keys(representativeStates.outcomes).length === 5,
    validationAuthoritative: validation.invalid && validation.errorMessage.includes('required'),
    actionPrompt: actionOpen.opened && actionOpen.openEvents === 1 && !actionClose.opened && actionClose.focusRestored,
    semanticCustomPage: semantic.wrappers >= 3,
    rawTier: raw.rawWidgets === 3,
    lifecycleBounded: lifecycle.elementGrowth < 80 && lifecycle.overlayGrowth <= 1,
    noExternalRequests: externalRequests.length === 0,
    noBrowserErrors: consoleErrors.length === 0 && pageErrors.length === 0
  };
  await context.close();
  if (Object.values(assertions).some(value => !value)) throw new Error(`Journey assertions failed: ${JSON.stringify({assertions, keyboardSingle, keyboardMulti, keyboardGrid, consoleErrors, pageErrors, externalRequests, lifecycle}, null, 2)}`);
  return {initial, keyboardSingle, keyboardMulti, keyboardGrid, single, multi, grid, stale, representativeStates, validation, actionOpen, actionClose, semantic, raw, lifecycle, graphqlEvents, externalRequests, consoleErrors, pageErrors, assertions};
}

async function captureScenario(browser, definition) {
  const context = await browser.newContext({
    viewport: {width: definition.width, height: definition.height},
    colorScheme: definition.colorScheme,
    reducedMotion: definition.name.includes('semantic') ? 'reduce' : 'no-preference',
    forcedColors: definition.forcedColors ?? 'none'
  });
  const page = await context.newPage();
  const requests = [];
  const errors = [];
  page.on('request', request => requests.push(request.url()));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  page.on('pageerror', error => errors.push(error.message));
  await page.goto(origin, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => document.documentElement.dataset.analysisReady === 'true');
  await page.evaluate(mode => window.analysis.renderRoute(mode, {focus: false}), definition.mode);
  if (definition.mode !== 'raw') {
    await page.evaluate(() => new Promise(resolve => {
      const grid = document.querySelector('causeway-vaadin-grid').grid;
      grid.dataProvider({page: 0, pageSize: 50, sortOrders: [], filters: []}, () => resolve());
    }));
  }
  await page.addScriptTag({path: resolve(directory, 'node_modules/axe-core/axe.min.js')});
  const axe = await page.evaluate(async () => {
    const result = await axe.run(document, {resultTypes: ['violations', 'incomplete']});
    return {violations: result.violations.map(item => ({id: item.id, impact: item.impact, help: item.help, nodes: item.nodes.map(node => ({target: node.target, failureSummary: node.failureSummary}))})), incomplete: result.incomplete.map(item => ({id: item.id, impact: item.impact, nodes: item.nodes.length}))};
  });
  const layout = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    clientWidth: document.documentElement.clientWidth,
    activeElement: document.activeElement?.localName,
    contextCount: document.querySelectorAll('#analysis-route > causeway-object-context-stub').length,
    theme: document.documentElement.getAttribute('theme'),
    readyMark: performance.getEntriesByName('causeway-analysis-ready')[0]?.startTime ?? null
  }));
  if (writeEvidence) {
    mkdirSync(screenshotsDirectory, {recursive: true});
    await page.screenshot({path: resolve(screenshotsDirectory, `${definition.name}.jpg`), type: 'jpeg', quality: 82, fullPage: true});
  }
  await context.close();
  return {...definition, axe, errors, overflow: Math.max(0, layout.scrollWidth - layout.clientWidth), layout, externalRequests: requests.filter(url => new URL(url).origin !== origin), requestCount: requests.length};
}

async function measureRun(browser) {
  const context = await browser.newContext({viewport: {width: 1280, height: 900}});
  const page = await context.newPage();
  const started = performance.now();
  await page.goto(origin, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => document.documentElement.dataset.analysisReady === 'true');
  const measured = await page.evaluate(() => ({
    navigation: performance.getEntriesByType('navigation')[0]?.duration ?? null,
    readyMark: performance.getEntriesByName('causeway-analysis-ready')[0]?.startTime ?? null,
    resources: performance.getEntriesByType('resource').length,
    transferSize: performance.getEntriesByType('resource').reduce((sum, item) => sum + (item.transferSize ?? 0), 0),
    decodedBodySize: performance.getEntriesByType('resource').reduce((sum, item) => sum + (item.decodedBodySize ?? 0), 0)
  }));
  measured.wallClock = performance.now() - started;
  await context.close();
  return measured;
}

function medianObject(rows) {
  const result = {};
  for (const key of Object.keys(rows[0] ?? {})) {
    const values = rows.map(row => row[key]).filter(Number.isFinite).sort((a, b) => a - b);
    result[key] = values.length ? values[Math.floor(values.length / 2)] : null;
  }
  return result;
}

async function reachable(url) {
  try { return (await fetch(url, {signal: AbortSignal.timeout(1000)})).ok; } catch { return false; }
}

async function waitForServer(url, child) {
  for (let attempt = 0; attempt < 80; attempt += 1) {
    if (await reachable(url)) return;
    if (child.exitCode != null) throw new Error(`Harness server exited with ${child.exitCode}`);
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error(`Harness server did not start at ${url}`);
}

function markdown(result) {
  const lines = ['# Headless browser evidence', '', `Generated: ${result.generatedAt}`, '', '## Journey assertions', ''];
  for (const [name, passed] of Object.entries(result.journey.assertions)) lines.push(`- ${passed ? 'PASS' : 'FAIL'}: ${name}`);
  lines.push('', '## Scenarios', '', '| Scenario | Axe violations | Axe incomplete | Overflow px | External requests | Browser errors |', '|---|---:|---:|---:|---:|---:|');
  for (const item of result.scenarios) lines.push(`| ${item.name} | ${item.axe.violations.length} | ${item.axe.incomplete.length} | ${item.overflow} | ${item.externalRequests.length} | ${item.errors.length} |`);
  lines.push('', '## Median timings', '', '```json', JSON.stringify(result.performance.medians, null, 2), '```', '', '## Known contract evidence', '', '- Combo Box paging is local because GraphQL autocomplete accepts search only.', '- Grid sorting and filtering are recorded as unsupported because the collection window accepts only offset and size.', '- Every route mode retains exactly one route object context.', '- No Vaadin Flow client or external network request was present during the journey.', '');
  return `${lines.join('\n')}\n`;
}
