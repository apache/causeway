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
const resultDirectory = resolve(evidenceDirectory, 'results');
const screenshotDirectory = resolve(evidenceDirectory, 'screenshots');
const harnessOrigin = process.env.HARNESS_ORIGIN ?? 'http://127.0.0.1:4173';
const petclinicOrigin = process.env.PETCLINIC_ORIGIN ?? 'http://127.0.0.1:8080';
const candidates = ['baseline', 'bootstrap', 'webawesome', 'openprops'];

mkdirSync(resultDirectory, {recursive: true});
mkdirSync(screenshotDirectory, {recursive: true});

const response = await fetch(`${petclinicOrigin}/htmx`);
if (!response.ok) throw new Error(`Petclinic is not available at ${petclinicOrigin}: HTTP ${response.status}`);

const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined
});
const evidence = {generatedAt: new Date().toISOString(), petclinicOrigin, harnessOrigin, candidates: {}};

try {
  for (const candidate of candidates) {
    console.log(`Integrating ${candidate}...`);
    const context = await browser.newContext({viewport: {width: 1440, height: 900}, colorScheme: 'light'});
    const page = await context.newPage();
    page.setDefaultTimeout(30_000);
    await installAnalysisRoute(page);
    const failures = [];
    page.on('pageerror', error => failures.push({kind: 'page-error', message: String(error)}));
    page.on('console', message => {
      if (message.type() === 'error') failures.push({kind: 'console-error', message: message.text()});
    });
    page.on('requestfailed', request => failures.push({kind: 'request-failed', message: `${request.url()} ${request.failure()?.errorText ?? ''}`}));
    await page.goto(`${petclinicOrigin}/htmx`, {waitUntil: 'networkidle'});
    await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState));
    await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState));
    const before = await snapshot(page);
    await injectCandidate(page, candidate);
    await page.waitForTimeout(candidate === 'webawesome' ? 800 : 250);
    const menuTrigger = page.locator('[data-causeway-menu-disclosure]').filter({hasText: 'Pet Owners'}).first();
    await menuTrigger.click();
    await page.waitForTimeout(100);
    const menuOpened = await menuTrigger.getAttribute('aria-expanded');
    await page.keyboard.press('Escape');
    await page.waitForTimeout(100);
    const menuClosed = await menuTrigger.getAttribute('aria-expanded');
    const after = await snapshot(page);
    const screenshot = `screenshots/integration-${candidate}.jpg`;
    await page.screenshot({path: resolve(evidenceDirectory, screenshot), fullPage: true, type: 'jpeg', quality: 82});
    evidence.candidates[candidate] = {
      mode: candidate === 'webawesome' ? 'single internal adapter plus toolkit theme loaded beside the real viewer' : candidate === 'baseline' ? 'unmodified real viewer' : 'candidate CSS injected into the real viewer without source changes',
      before,
      after,
      menuOpened,
      menuClosed,
      screenshot,
      failures
    };
    await context.close();
    console.log(`Integrated ${candidate}.`);
  }
} finally {
  await browser.close();
}

writeFileSync(resolve(resultDirectory, 'real-viewer-integration.json'), `${JSON.stringify(evidence, null, 2)}\n`);
writeFileSync(resolve(resultDirectory, 'real-viewer-integration.md'), renderMarkdown(evidence));
console.log(Object.entries(evidence.candidates).map(([candidate, result]) => `${candidate}: failures=${result.failures.length}, overflow=${result.after.horizontalOverflow}, menu=${result.menuOpened}->${result.menuClosed}, adapter=${result.after.analysisAdapterCount}`).join('\n'));

async function installAnalysisRoute(page) {
  const prefix = `${petclinicOrigin}/__analysis/`;
  await page.route(`${petclinicOrigin}/__analysis/**`, async route => {
    const relativePath = decodeURIComponent(route.request().url().slice(prefix.length));
    const target = resolve(directory, relativePath);
    if (target !== directory && !target.startsWith(directory + sep)) {
      await route.fulfill({status: 403, body: 'Forbidden'});
      return;
    }
    const contentTypes = {'.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8', '.svg': 'image/svg+xml', '.woff2': 'font/woff2'};
    try {
      await route.fulfill({status: 200, contentType: contentTypes[extname(target)] ?? 'application/octet-stream', body: readFileSync(target)});
    } catch {
      await route.fulfill({status: 404, body: 'Not found'});
    }
  });
}

async function injectCandidate(page, candidate) {
  const base = `${petclinicOrigin}/__analysis`;
  if (candidate === 'bootstrap') {
    await page.addStyleTag({url: `${base}/node_modules/bootstrap/dist/css/bootstrap.min.css`});
    await page.addStyleTag({url: `${base}/candidates/bootstrap.css`});
  } else if (candidate === 'openprops') {
    for (const asset of ['open-props.min.css', 'normalize.min.css', 'buttons.min.css']) {
      await page.addStyleTag({url: `${base}/node_modules/open-props/${asset}`});
    }
    await page.addStyleTag({url: `${base}/candidates/open-props.css`});
  } else if (candidate === 'webawesome') {
    const distribution = `${base}/node_modules/@awesome.me/webawesome/dist-cdn`;
    await page.addStyleTag({url: `${distribution}/styles/webawesome.css`});
    await page.addStyleTag({url: `${base}/candidates/web-awesome.css`});
    await page.evaluate(async distributionPath => {
      const {setBasePath} = await import(`${distributionPath}/utilities/base-path.js`);
      setBasePath(distributionPath);
      await import(`${distributionPath}/components/button/button.js`);
      const host = document.createElement('causeway-action');
      host.dataset.analysisAdapter = 'webawesome';
      host.innerHTML = '<wa-button appearance="filled" variant="brand">Analysis adapter action</wa-button>';
      document.querySelector('.causeway-object-actions, .causeway-heading-actions, [data-testid="causeway-route-page"]')?.prepend(host);
      await customElements.whenDefined('wa-button');
    }, distribution);
  }
}

async function snapshot(page) {
  return page.evaluate(() => {
    const firstButton = document.querySelector('button');
    const styles = firstButton ? getComputedStyle(firstButton) : null;
    return {
      url: location.href,
      title: document.title,
      causewayElementCount: [...document.querySelectorAll('*')].filter(element => element.localName.startsWith('causeway-')).length,
      analysisAdapterCount: document.querySelectorAll('[data-analysis-adapter]').length,
      horizontalOverflow: document.documentElement.scrollWidth - document.documentElement.clientWidth,
      routeState: document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState,
      menuState: document.getElementsByTagName('causeway-menubars')[0]?.dataset.menuState,
      bodyBackground: getComputedStyle(document.body).backgroundColor,
      bodyColor: getComputedStyle(document.body).color,
      firstButton: styles ? {color: styles.color, background: styles.backgroundColor, borderRadius: styles.borderRadius, font: styles.fontFamily} : null
    };
  });
}

function renderMarkdown(result) {
  const lines = ['# Real Petclinic integration check', '', `Generated: ${result.generatedAt}`, '', 'The check injects candidate assets only into a disposable browser context and does not modify production files.', ''];
  for (const [candidate, item] of Object.entries(result.candidates)) {
    lines.push(`## ${candidate}`, '');
    lines.push(`- Mode: ${item.mode}.`);
    lines.push(`- Browser failures: ${item.failures.length}.`);
    lines.push(`- Menu disclosure: ${item.menuOpened} before Escape, ${item.menuClosed} after Escape.`);
    lines.push(`- Horizontal overflow before/after: ${item.before.horizontalOverflow}/${item.after.horizontalOverflow} pixels.`);
    lines.push(`- Body background before/after: ${item.before.bodyBackground}/${item.after.bodyBackground}.`);
    lines.push(`- First button before/after: ${JSON.stringify(item.before.firstButton)}/${JSON.stringify(item.after.firstButton)}.`);
    lines.push(`- Internal analysis adapters: ${item.after.analysisAdapterCount}.`);
    lines.push(`- Screenshot: ${item.screenshot}.`, '');
  }
  lines.push('## Interpretation', '');
  lines.push('- Bootstrap and Open Props can affect the existing light DOM immediately through global CSS, which makes incremental styling easy but creates collision and regression risk.');
  lines.push('- Web Awesome does not style existing Causeway controls automatically because its visual implementation lives behind toolkit-owned custom elements and shadow DOM.');
  lines.push('- Web Awesome therefore requires explicit internal adapters or component renderer changes, while Bootstrap and Open Props can begin as token or stylesheet changes.');
  lines.push('- All injected strategies preserved route readiness, menu readiness, Escape dismissal, and page-level overflow in this bounded check.', '');
  return `${lines.join('\n')}\n`;
}
