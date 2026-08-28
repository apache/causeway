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
const outputDirectory = process.env.CAUSEWAY_MENUBAR_AUDIT_OUTPUT
  ? resolve(process.cwd(), process.env.CAUSEWAY_MENUBAR_AUDIT_OUTPUT)
  : null;
if (outputDirectory) mkdirSync(outputDirectory, {recursive: true});
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
  if (url.pathname === '/fixture.css') return send(response, 'text/css', 'body{margin:1rem}vaadin-menu-bar{box-sizing:border-box;max-width:100%}');
  if (url.pathname === '/asset.js') return send(response, 'text/javascript', readFileSync(resolve(directory, 'generated/assets/vaadin-menubar.js')));
  if (url.pathname === '/axe.js') return send(response, 'text/javascript', readFileSync(resolve(directory, 'node_modules/axe-core/axe.min.js')));
  response.writeHead(404).end();
});
await new Promise(resolveReady => server.listen(0, '127.0.0.1', resolveReady));
const origin = `http://127.0.0.1:${server.address().port}`;
const browser = await chromium.launch({headless: true});
const context = await browser.newContext({viewport: {width: 390, height: 844}, colorScheme: 'dark', reducedMotion: 'reduce'});
const page = await context.newPage();
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
const control = page.locator('vaadin-menu-bar');
await control.focus();
await page.keyboard.press('ArrowRight');
await page.keyboard.press('Enter');
await page.keyboard.press('Escape');
const activation = await page.evaluate(() => {
  const menu = document.querySelector('vaadin-menu-bar');
  const leaf = menu.items[0].children[1];
  menu.dispatchEvent(new CustomEvent('item-selected', {detail: {value: leaf}}));
  return {enabled: document.body.dataset.enabledActivations, disabled: document.body.dataset.disabledActivations};
});
await page.waitForTimeout(200);
const styleTexts = await page.locator('style').evaluateAll(styles => styles.map(style => style.textContent).filter(Boolean));
const styleHashes = [...new Set(styleTexts.map(text => `sha256-${createHash('sha256').update(text).digest('base64')}`))].sort();
if (writePolicy) {
  policy.cspStyleHashes = styleHashes;
  writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);
}
const axeViolations = await page.evaluate(async () => (await globalThis.axe.run(document)).violations.map(violation => violation.id));
const narrowOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, 'menubar-narrow-dark.png'), fullPage: true});
await page.emulateMedia({forcedColors: 'active', colorScheme: 'light', reducedMotion: 'no-preference'});
await page.setViewportSize({width: 1280, height: 800});
const forcedColorViolations = await page.evaluate(async () => (await globalThis.axe.run(document, {
  rules: {'color-contrast': {enabled: false}}
})).violations.map(violation => violation.id));
const wideOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, 'menubar-wide-forced-colors.png'), fullPage: true});
await browser.close();
await new Promise(resolveClose => server.close(resolveClose));
const result = {
  styleHashes,
  enabledActivations: Number(activation.enabled ?? 0),
  disabledActivations: Number(activation.disabled ?? 0),
  cspViolations,
  axeViolations: [...new Set([...axeViolations, ...forcedColorViolations])],
  consoleErrors,
  pageErrors,
  externalRequests,
  overflow: narrowOverflow || wideOverflow
};
if (outputDirectory) writeFileSync(resolve(outputDirectory, 'menubar-audit.json'), `${JSON.stringify(result, null, 2)}\n`);
if (result.enabledActivations !== 1 || result.disabledActivations !== 0) throw new Error(`Unexpected Menu Bar activation counts: ${JSON.stringify(activation)}.`);
for (const [name, values] of Object.entries(result)) {
  if (name !== 'styleHashes' && Array.isArray(values) && values.length > 0) {
    throw new Error(`${name} is not empty: ${JSON.stringify(values)}.`);
  }
}
if (result.overflow) throw new Error('Menu Bar browser audit detected page overflow.');
console.log(JSON.stringify(result, null, 2));

function html() {
  return '<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Menu Bar audit</title><link rel="stylesheet" href="/fixture.css"><script src="/axe.js"></script><script type="module" src="/fixture.js"></script></head><body><main><h1>Application menu</h1><div id="fixture"></div></main></body></html>';
}

function fixture() {
  return `globalThis.Vaadin = {featureFlags: {accessibleDisabledButtons: true, accessibleDisabledMenuItems: true}};
await import('/asset.js');
await customElements.whenDefined('vaadin-menu-bar');
const control = document.createElement('vaadin-menu-bar');
control.setAttribute('aria-label', 'Primary application menu');
control.items = [{text: 'Administration', children: [{text: 'Disabled action', disabled: true, key: 'disabled'}, {text: 'Enabled action', key: 'enabled'}, {text: 'Nested', children: [{text: 'Nested action', key: 'nested'}]}]}, {text: 'Reports', children: [{text: 'Daily report', key: 'report'}]}];
document.body.dataset.enabledActivations = '0';
document.body.dataset.disabledActivations = '0';
control.addEventListener('item-selected', event => {
  if (event.detail.value?.key === 'enabled') document.body.dataset.enabledActivations = String(Number(document.body.dataset.enabledActivations) + 1);
  if (event.detail.value?.key === 'disabled') document.body.dataset.disabledActivations = String(Number(document.body.dataset.disabledActivations) + 1);
});
document.querySelector('#fixture').append(control);
await control.updateComplete;
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
