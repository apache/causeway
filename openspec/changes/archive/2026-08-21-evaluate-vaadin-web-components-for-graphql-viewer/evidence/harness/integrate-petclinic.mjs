/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync, readFileSync, writeFileSync} from 'node:fs';
import {extname, resolve, sep} from 'node:path';
import {fileURLToPath} from 'node:url';
import {chromium} from 'playwright';

const directory = resolve(fileURLToPath(new URL('.', import.meta.url)));
const evidenceDirectory = resolve(directory, '..');
const resultsDirectory = resolve(evidenceDirectory, 'results');
const screenshotsDirectory = resolve(evidenceDirectory, 'screenshots');
const petclinicOrigin = process.env.PETCLINIC_ORIGIN ?? 'http://127.0.0.1:8080';
const chrome = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ?? '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const browser = await chromium.launch({headless: true, executablePath: chrome});

try {
  const context = await browser.newContext({viewport: {width: 1440, height: 1000}, colorScheme: 'light'});
  const page = await context.newPage();
  page.setDefaultTimeout(40_000);
  const failures = [];
  const requests = [];
  page.on('pageerror', error => failures.push({kind: 'page-error', message: error.message}));
  page.on('console', message => { if (message.type() === 'error') failures.push({kind: 'console-error', message: message.text()}); });
  page.on('requestfailed', request => failures.push({kind: 'request-failed', message: `${request.url()} ${request.failure()?.errorText ?? ''}`}));
  page.on('request', request => requests.push(request.url()));
  await installAnalysisRoute(page);
  await page.goto(`${petclinicOrigin}/htmx`, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState));
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState));
  const before = await snapshot(page);

  await page.evaluate(async () => {
    await import('/__analysis/generated/vaadin-selective.js');
    await Promise.all(['vaadin-combo-box', 'vaadin-grid', 'vaadin-date-picker'].map(name => customElements.whenDefined(name)));
    const stylesheet = document.createElement('link');
    stylesheet.rel = 'stylesheet';
    stylesheet.href = '/__analysis/integration.css';
    document.head.append(stylesheet);
    const route = document.querySelector('[data-testid="causeway-route-page"]');
    const panel = document.createElement('section');
    panel.dataset.analysisAdapter = 'vaadin-probe';
    panel.setAttribute('aria-label', 'Analysis-only Vaadin integration probe');
    panel.innerHTML = '<strong>Analysis-only Vaadin free-core probe</strong><vaadin-combo-box label="Reference lookup"></vaadin-combo-box><vaadin-date-picker label="Review date" value="2026-08-21"></vaadin-date-picker><vaadin-grid aria-label="Probe collection"><vaadin-grid-column path="name" header="Name"></vaadin-grid-column><vaadin-grid-column path="status" header="Status"></vaadin-grid-column></vaadin-grid>';
    route.append(panel);
    panel.querySelector('vaadin-combo-box').items = [{id: '1', label: 'Owner 001'}, {id: '2', label: 'Owner 002'}];
    panel.querySelector('vaadin-combo-box').itemLabelPath = 'label';
    panel.querySelector('vaadin-combo-box').itemValuePath = 'id';
    panel.querySelector('vaadin-grid').items = [{id: '1', name: 'Visit 001', status: 'Scheduled'}, {id: '2', name: 'Visit 002', status: 'Complete'}];
  });
  await page.waitForFunction(() => document.querySelector('[data-analysis-adapter="vaadin-probe"] vaadin-grid')?.shadowRoot != null);

  const menuTrigger = page.locator('[data-causeway-menu-disclosure]').filter({hasText: 'Pet Owners'}).first();
  await menuTrigger.click();
  await page.waitForTimeout(100);
  const menuOpened = await menuTrigger.getAttribute('aria-expanded');
  await page.keyboard.press('Escape');
  await page.waitForTimeout(100);
  const menuClosed = await menuTrigger.getAttribute('aria-expanded');
  const after = await snapshot(page);
  mkdirSync(resultsDirectory, {recursive: true});
  mkdirSync(screenshotsDirectory, {recursive: true});
  const screenshot = 'integration-vaadin.jpg';
  await page.screenshot({path: resolve(screenshotsDirectory, screenshot), type: 'jpeg', quality: 82, fullPage: true});
  const externalRequests = requests.filter(url => new URL(url).origin !== petclinicOrigin);
  const knownCspStyleFailures = failures.filter(failure => failure.kind === 'console-error' && failure.message.includes("style-src 'self'"));
  const unexpectedFailures = failures.filter(failure => !knownCspStyleFailures.includes(failure));
  const evidence = {
    generatedAt: new Date().toISOString(),
    mode: 'headless same-origin analysis injection',
    before,
    after,
    menuOpened,
    menuClosed,
    failures,
    knownCspStyleFailures,
    unexpectedFailures,
    externalRequests,
    screenshot,
    compatibility: {strictStyleCspCompatible: knownCspStyleFailures.length === 0, requiredPolicyChange: knownCspStyleFailures.length ? 'Vaadin inserts component styles that the current style-src self policy blocks; adoption requires a reviewed nonce, hash, adopted-stylesheet, or policy strategy.' : null},
    assertions: {
      routeRemainedReady: ['ready', 'partial-error'].includes(after.routeState),
      menuRemainedReady: ['ready', 'partial-error'].includes(after.menuState),
      menuDismissal: menuOpened === 'true' && menuClosed === 'false',
      oneInjectedAdapter: after.analysisAdapterCount === 1,
      vaadinDefined: after.vaadinDefined,
      noFlowRuntime: !after.flowRuntime,
      noNewOverflow: after.horizontalOverflow <= before.horizontalOverflow,
      noUnexpectedBrowserFailures: unexpectedFailures.length === 0,
      cspFailureClassified: knownCspStyleFailures.length > 0,
      noExternalRequests: externalRequests.length === 0
    }
  };
  writeFileSync(resolve(resultsDirectory, 'real-viewer-integration.json'), `${JSON.stringify(evidence, null, 2)}\n`);
  writeFileSync(resolve(resultsDirectory, 'real-viewer-integration.md'), markdown(evidence));
  console.log(JSON.stringify(evidence.assertions, null, 2));
  if (Object.values(evidence.assertions).some(value => !value)) throw new Error(`Petclinic integration assertions failed: ${JSON.stringify(evidence, null, 2)}`);
  await context.close();
} finally {
  await browser.close();
}

async function installAnalysisRoute(page) {
  const prefix = `${petclinicOrigin}/__analysis/`;
  await page.route(`${petclinicOrigin}/__analysis/**`, async route => {
    const relative = decodeURIComponent(route.request().url().slice(prefix.length));
    const target = resolve(directory, relative);
    if (target !== directory && !target.startsWith(`${directory}${sep}`)) {
      await route.fulfill({status: 403, body: 'Forbidden'});
      return;
    }
    const types = {'.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8', '.svg': 'image/svg+xml', '.woff2': 'font/woff2'};
    try { await route.fulfill({status: 200, contentType: types[extname(target)] ?? 'application/octet-stream', body: readFileSync(target)}); }
    catch { await route.fulfill({status: 404, body: 'Not found'}); }
  });
}

async function snapshot(page) {
  return page.evaluate(() => ({
    routeState: document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState,
    menuState: document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState,
    causewayElementCount: [...document.querySelectorAll('*')].filter(element => element.localName.startsWith('causeway-')).length,
    analysisAdapterCount: document.querySelectorAll('[data-analysis-adapter]').length,
    vaadinDefined: ['vaadin-combo-box', 'vaadin-grid', 'vaadin-date-picker'].every(name => Boolean(customElements.get(name))),
    flowRuntime: Boolean(window.Vaadin?.Flow?.clients),
    horizontalOverflow: Math.max(0, document.documentElement.scrollWidth - document.documentElement.clientWidth),
    bodyBackground: getComputedStyle(document.body).backgroundColor
  }));
}

function markdown(result) {
  const lines = ['# Real Petclinic Vaadin integration check', '', `Generated: ${result.generatedAt}`, '', 'The check injects selective Vaadin assets only into a disposable headless browser context through a same-origin Playwright route.', 'Production source files and server resources are unchanged.', '', '## Assertions', ''];
  for (const [name, passed] of Object.entries(result.assertions)) lines.push(`- ${passed ? 'PASS' : 'FAIL'}: ${name}`);
  lines.push('', '## Observations', '', `- Route state before/after: ${result.before.routeState}/${result.after.routeState}.`, `- Menu state before/after: ${result.before.menuState}/${result.after.menuState}.`, `- Menu disclosure before Escape/after Escape: ${result.menuOpened}/${result.menuClosed}.`, `- Horizontal overflow before/after: ${result.before.horizontalOverflow}/${result.after.horizontalOverflow} pixels.`, `- Browser failures: ${result.failures.length}, of which ${result.knownCspStyleFailures.length} are classified strict-style-CSP incompatibilities.`, `- External requests: ${result.externalRequests.length}.`, `- Flow runtime detected: ${result.after.flowRuntime}.`, `- Strict style CSP compatible: ${result.compatibility.strictStyleCspCompatible}.`, `- Screenshot: ${result.screenshot}.`, '', 'The probe demonstrates same-origin module delivery and coexistence, but Vaadin component style insertion conflicts with the viewer current style-src self policy.', 'A production proposal must resolve that policy deliberately rather than silently enabling unsafe inline style.', '')
  return `${lines.join('\n')}\n`;
}
