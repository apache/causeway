/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync, readFileSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {chromium} from 'playwright';

const directory = fileURLToPath(new URL('.', import.meta.url));
const evidenceDirectory = resolve(directory, '..');
const resultsDirectory = resolve(evidenceDirectory, 'results');
const petclinicOrigin = process.env.PETCLINIC_ORIGIN ?? 'http://127.0.0.1:8080';
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const matrix = JSON.parse(readFileSync(resolve(resultsDirectory, 'csp-matrix.json'), 'utf8'));
const hashSources = matrix.discoveredStyleHashes.map(hash => `'${hash}'`).join(' ');
const browser = await chromium.launch({headless: true, executablePath});

try {
  const context = await browser.newContext({viewport: {width: 1440, height: 1000}, colorScheme: 'light'});
  const page = await context.newPage();
  page.setDefaultTimeout(40_000);
  const consoleErrors = [];
  const pageErrors = [];
  const requests = [];
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('request', request => requests.push(request.url()));
  await page.addInitScript(() => {
    window.__causewayCspViolations = [];
    document.addEventListener('securitypolicyviolation', event => window.__causewayCspViolations.push({
      blockedURI: event.blockedURI,
      effectiveDirective: event.effectiveDirective,
      lineNumber: event.lineNumber,
      columnNumber: event.columnNumber,
      originalPolicy: event.originalPolicy,
      sourceFile: event.sourceFile,
      violatedDirective: event.violatedDirective
    }));
  });
  await installCandidateRoute(page);
  await installDocumentPolicyRoute(page);
  await page.goto(`${petclinicOrigin}/htmx`, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState));
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState));
  const before = await snapshot(page);
  await page.evaluate(async () => {
    await import('/__vaadin-csp/candidate.js');
    await Promise.all(['vaadin-combo-box', 'vaadin-multi-select-combo-box'].map(name => customElements.whenDefined(name)));
    const items = Array.from({length: 12}, (_, index) => ({id: `owner-${index + 1}`, label: `Owner ${`${index + 1}`.padStart(3, '0')}`}));
    const panel = document.createElement('section');
    panel.dataset.vaadinCspPilot = 'true';
    panel.setAttribute('aria-label', 'Vaadin CSP pilot controls');
    const single = document.createElement('vaadin-combo-box');
    single.label = 'Owner';
    single.itemLabelPath = 'label';
    single.itemValuePath = 'id';
    single.items = items;
    single.clearButtonVisible = true;
    const multi = document.createElement('vaadin-multi-select-combo-box');
    multi.label = 'Owners';
    multi.itemLabelPath = 'label';
    multi.itemValuePath = 'id';
    multi.items = items;
    multi.clearButtonVisible = true;
    panel.append(single, multi);
    document.querySelector('[data-testid="causeway-route-page"]').append(panel);
    await Promise.all([single.updateComplete, multi.updateComplete]);
    single.opened = true;
    single.filter = 'Owner 00';
    multi.opened = true;
    multi.filter = 'Owner 00';
    await new Promise(resolveFrame => requestAnimationFrame(() => requestAnimationFrame(resolveFrame)));
    single.selectedItem = items[1];
    multi.selectedItems = [items[1], items[2]];
    await Promise.all([single.updateComplete, multi.updateComplete]);
    single.opened = false;
    multi.opened = false;
    single.required = true;
    multi.required = true;
    single.validate();
    multi.validate();
    await new Promise(resolveFrame => requestAnimationFrame(() => requestAnimationFrame(resolveFrame)));
  });
  const during = await snapshot(page);
  await page.evaluate(() => document.querySelector('[data-vaadin-csp-pilot]')?.remove());
  await page.waitForTimeout(100);
  const after = await snapshot(page);
  const violations = await page.evaluate(() => window.__causewayCspViolations);
  const externalRequests = requests.filter(request => new URL(request).origin !== petclinicOrigin);
  const result = {
    generatedAt: new Date().toISOString(),
    browserVersion: browser.version(),
    policyStrategy: 'Current policy plus four pinned style-src-elem SHA-256 sources and style-src-attr none',
    hashes: matrix.discoveredStyleHashes,
    before,
    during,
    after,
    violations,
    consoleErrors,
    pageErrors,
    externalRequests,
    assertions: {
      routeRemainedReady: ['ready', 'partial-error'].includes(after.routeState),
      menuRemainedReady: ['ready', 'partial-error'].includes(after.menuState),
      controlsDefined: during.controlsDefined,
      noFlowRuntime: !during.flowRuntime,
      noHorizontalOverflow: during.horizontalOverflow === 0,
      zeroCspViolations: violations.length === 0,
      zeroConsoleErrors: consoleErrors.length === 0,
      zeroPageErrors: pageErrors.length === 0,
      zeroExternalRequests: externalRequests.length === 0,
      panelRemoved: !after.panelPresent
    }
  };
  mkdirSync(resultsDirectory, {recursive: true});
  writeFileSync(resolve(resultsDirectory, 'petclinic-csp-check.json'), `${JSON.stringify(result, null, 2)}\n`);
  console.log(JSON.stringify(result.assertions, null, 2));
  if (Object.values(result.assertions).some(value => !value)) throw new Error(`Petclinic CSP assertions failed: ${JSON.stringify(result, null, 2)}`);
  await context.close();
} finally {
  await browser.close();
}

async function installCandidateRoute(page) {
  await page.route(`${petclinicOrigin}/__vaadin-csp/**`, async route => {
    const pathname = new URL(route.request().url()).pathname;
    if (pathname !== '/__vaadin-csp/candidate.js') return route.abort();
    return route.fulfill({status: 200, contentType: 'text/javascript; charset=utf-8', body: readFileSync(resolve(directory, 'generated/candidate.js'))});
  });
}

async function installDocumentPolicyRoute(page) {
  await page.route(`${petclinicOrigin}/htmx**`, async route => {
    if (route.request().resourceType() !== 'document') return route.continue();
    const response = await route.fetch();
    const headers = response.headers();
    const current = headers['content-security-policy'];
    if (!current?.includes("style-src 'self'")) throw new Error(`Unexpected Petclinic CSP: ${current}`);
    headers['content-security-policy'] = current.replace(
      "style-src 'self'",
      `style-src 'self' ${hashSources}; style-src-elem 'self' ${hashSources}; style-src-attr 'none'`
    );
    await route.fulfill({response, headers});
  });
}

async function snapshot(page) {
  return page.evaluate(() => ({
    routeState: document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState,
    menuState: document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState,
    panelPresent: Boolean(document.querySelector('[data-vaadin-csp-pilot]')),
    controlsDefined: ['vaadin-combo-box', 'vaadin-multi-select-combo-box'].every(name => Boolean(customElements.get(name))),
    flowRuntime: Boolean(window.Vaadin?.Flow?.clients),
    horizontalOverflow: Math.max(0, document.documentElement.scrollWidth - document.documentElement.clientWidth),
    overlayCount: document.querySelectorAll('vaadin-combo-box-overlay, vaadin-multi-select-combo-box-overlay').length
  }));
}
