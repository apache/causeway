/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createHash} from 'node:crypto';
import {createServer} from 'node:http';
import {mkdirSync, readFileSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {chromium} from 'playwright';

const directory = fileURLToPath(new URL('.', import.meta.url));
const policyPath = resolve(directory, 'policy.json');
const policy = JSON.parse(readFileSync(policyPath, 'utf8'));
const writePolicy = process.argv.includes('--write-policy');
const outputDirectory = process.env.CAUSEWAY_ACTION_AUDIT_OUTPUT
  ? resolve(process.cwd(), process.env.CAUSEWAY_ACTION_AUDIT_OUTPUT)
  : null;
if (outputDirectory) mkdirSync(outputDirectory, {recursive: true});
const axePath = resolve(directory, 'node_modules/axe-core/axe.min.js');
const cspViolations = [];
const consoleErrors = [];
const pageErrors = [];
const externalRequests = [];
const csp = writePolicy || policy.cspStyleHashes.length === 0 ? null : policyFor(policy.cspStyleHashes);
const server = createServer((request, response) => {
  const url = new URL(request.url, 'http://127.0.0.1');
  if (csp) response.setHeader('Content-Security-Policy', csp);
  if (url.pathname === '/') return send(response, 'text/html', html());
  if (url.pathname === '/fixture.js') return send(response, 'text/javascript', fixture());
  if (url.pathname === '/fixture.css') return send(response, 'text/css', 'vaadin-button{box-sizing:border-box;max-width:100%}');
  if (url.pathname === '/asset.js') return send(response, 'text/javascript', readFileSync(resolve(directory, 'generated/assets/vaadin-actions.js')));
  if (url.pathname === '/axe.js') return send(response, 'text/javascript', readFileSync(axePath));
  response.writeHead(404).end();
});
await new Promise(resolveReady => server.listen(0, '127.0.0.1', resolveReady));
const port = server.address().port;
const origin = `http://127.0.0.1:${port}`;
const browser = await chromium.launch({headless: true});
const page = await browser.newPage({viewport: {width: 390, height: 844}, colorScheme: 'dark', reducedMotion: 'reduce'});
page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
page.on('pageerror', error => pageErrors.push(error.message));
page.on('request', request => { if (!request.url().startsWith(origin)) externalRequests.push(request.url()); });
await page.exposeFunction('recordCspViolation', event => cspViolations.push(event));
await page.addInitScript(() => document.addEventListener('securitypolicyviolation', event => globalThis.recordCspViolation({
  directive: event.violatedDirective,
  blockedURI: event.blockedURI,
  sample: event.sample
})));
await page.goto(origin, {waitUntil: 'networkidle'});
await page.waitForFunction(() => document.body.dataset.ready === 'true');
const enabled = page.locator('#enabled');
await enabled.focus();
await page.keyboard.press('Enter');
await page.keyboard.press('Space');
const activationCount = await enabled.getAttribute('data-activation-count');
const disabled = page.locator('#disabled');
await disabled.focus();
await page.keyboard.press('Enter');
const disabledActivationCount = await disabled.getAttribute('data-activation-count');
await page.waitForTimeout(300);
const styleTexts = await page.locator('style').evaluateAll(styles => styles.map(style => style.textContent).filter(Boolean));
const styleHashes = [...new Set(styleTexts.map(text => `sha256-${createHash('sha256').update(text).digest('base64')}`))].sort();
if (writePolicy) {
  policy.cspStyleHashes = styleHashes;
  writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);
}
const axeViolations = await page.evaluate(async () => (await globalThis.axe.run(document, {
  rules: {'color-contrast': {enabled: false}}
})).violations.map(violation => violation.id));
const narrowOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, 'actions-narrow-dark.png'), fullPage: true});
await page.emulateMedia({forcedColors: 'active', colorScheme: 'light', reducedMotion: 'no-preference'});
await page.setViewportSize({width: 1280, height: 800});
const forcedColorViolations = await page.evaluate(async () => (await globalThis.axe.run(document, {
  rules: {'color-contrast': {enabled: false}}
})).violations.map(violation => violation.id));
for (const id of forcedColorViolations) if (!axeViolations.includes(id)) axeViolations.push(id);
const wideOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, 'actions-wide-forced-colors.png'), fullPage: true});
await browser.close();
await new Promise(resolveClosed => server.close(resolveClosed));
const result = {
  styleHashes,
  activationCount: Number(activationCount),
  disabledActivationCount: Number(disabledActivationCount),
  cspViolations,
  axeViolations,
  consoleErrors,
  pageErrors,
  externalRequests,
  overflow: narrowOverflow || wideOverflow
};
if (outputDirectory) writeFileSync(resolve(outputDirectory, 'action-audit.json'), `${JSON.stringify(result, null, 2)}\n`);
console.log(JSON.stringify(result, null, 2));
if (!writePolicy && (result.activationCount !== 2 || result.disabledActivationCount !== 0
    || result.cspViolations.length || result.axeViolations.length || result.consoleErrors.length
    || result.pageErrors.length || result.externalRequests.length || result.overflow)) {
  process.exitCode = 1;
}

function html() {
  return '<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Action audit</title><link rel="stylesheet" href="/fixture.css"><script src="/axe.js"></script><script type="module" src="/fixture.js"></script></head><body><main><h1>Actions</h1><div id="fixture"></div></main></body></html>';
}

function fixture() {
  return `import '/asset.js';
await customElements.whenDefined('vaadin-button');
const fixture = document.querySelector('#fixture');
for (const state of [{id: 'enabled', disabled: false}, {id: 'disabled', disabled: true}]) {
  const control = document.createElement('vaadin-button');
  control.id = state.id;
  control.textContent = state.disabled ? 'Disabled action' : 'Enabled action';
  control.disabled = state.disabled;
  control.dataset.activationCount = '0';
  control.addEventListener('click', () => control.dataset.activationCount = String(Number(control.dataset.activationCount) + 1));
  fixture.append(control);
}
await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
document.body.dataset.ready = 'true';`;
}

function policyFor(hashes) {
  const sources = hashes.map(hash => `'${hash}'`).join(' ');
  return `default-src 'self'; script-src 'self'; style-src 'self' ${sources}; style-src-elem 'self' ${sources}; style-src-attr 'none'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'`;
}

function send(response, type, body) {
  response.writeHead(200, {'Content-Type': type, 'Cache-Control': 'no-store'});
  response.end(body);
}
