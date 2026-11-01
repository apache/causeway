/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {chromium} from 'playwright';
import {startServer} from './server.mjs';

const directory = fileURLToPath(new URL('.', import.meta.url));
const evidence = resolve(directory, '..');
const resultsDirectory = resolve(evidence, 'results');
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const operations = ['connect', 'open', 'close', 'filter', 'select', 'clear', 'validation', 'disabled', 'narrow', 'dark', 'disconnect', 'reconnect'];
const components = ['single', 'multi'];
const {server, origin} = await startServer();
const browser = await chromium.launch({headless: true, executablePath});

try {
  const discoveries = [
    await runCase('single', 'connect', 'exact', []),
    await runCase('multi', 'connect', 'exact', [])
  ];
  const discoveredStyles = [...new Map(discoveries.flatMap(item => item.snapshot.styles).map(style => [style.hash, style])).values()];
  const hashes = discoveredStyles.map(style => style.hash);
  const cases = [];
  for (const policy of ['exact', 'hashes']) {
    for (const component of components) {
      for (const operation of operations) cases.push(await runCase(component, operation, policy, hashes));
    }
  }
  for (const policy of ['attribute-inline', 'element-inline', 'nonce', 'nonce-patched']) {
    for (const component of components) cases.push(await runCase(component, 'open', policy, hashes));
  }
  const result = {
    generatedAt: new Date().toISOString(),
    browserVersion: browser.version(),
    exactPolicy: "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'",
    discoveredStyleHashes: hashes,
    discoveredStyles,
    cases,
    summary: summarize(cases)
  };
  mkdirSync(resultsDirectory, {recursive: true});
  writeFileSync(resolve(resultsDirectory, 'csp-matrix.json'), `${JSON.stringify(result, null, 2)}\n`);
  writeFileSync(resolve(resultsDirectory, 'csp-matrix.md'), markdown(result));
  console.log(JSON.stringify(result.summary, null, 2));
} finally {
  await browser.close();
  await new Promise(resolveClose => server.close(resolveClose));
}

async function runCase(component, operation, policy, hashes) {
  const context = await browser.newContext({viewport: operation === 'narrow' ? {width: 390, height: 844} : {width: 1280, height: 900}, colorScheme: operation === 'dark' ? 'dark' : 'light'});
  const page = await context.newPage();
  const consoleMessages = [];
  const pageErrors = [];
  const requests = [];
  page.on('console', message => consoleMessages.push({type: message.type(), text: message.text()}));
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('request', request => requests.push(request.url()));
  const url = new URL(origin);
  url.searchParams.set('policy', policy);
  for (const hash of hashes) url.searchParams.append('hash', hash);
  await page.goto(url.toString(), {waitUntil: 'networkidle'});
  const before = await page.evaluate(componentName => window.cspHarness.create(componentName), component);
  const snapshot = operation === 'connect' ? before : await page.evaluate(operationName => window.cspHarness.operate(operationName), operation);
  await page.waitForTimeout(50);
  const finalSnapshot = await page.evaluate(() => window.cspHarness.snapshot());
  const externalRequests = requests.filter(request => new URL(request).origin !== origin);
  await context.close();
  return {component, operation, policy, snapshot: finalSnapshot, consoleMessages, pageErrors, externalRequests};
}

function summarize(cases) {
  const byPolicy = {};
  for (const item of cases) {
    const current = byPolicy[item.policy] ?? {cases: 0, violations: 0, consoleErrors: 0, pageErrors: 0, externalRequests: 0, overflowCases: 0};
    current.cases += 1;
    current.violations += item.snapshot.events.length;
    current.consoleErrors += item.consoleMessages.filter(message => message.type === 'error').length;
    current.pageErrors += item.pageErrors.length;
    current.externalRequests += item.externalRequests.length;
    current.overflowCases += item.snapshot.horizontalOverflow > 0 ? 1 : 0;
    byPolicy[item.policy] = current;
  }
  return byPolicy;
}

function markdown(result) {
  const lines = ['# Vaadin reference control CSP matrix', '', `Generated: ${result.generatedAt}`, '', `Browser: ${result.browserVersion}`, '', '## Discovered static style hashes', ''];
  for (const style of result.discoveredStyles) lines.push(`- \`${style.id || '(no id)'}\` in \`${style.parentHost || style.parent}\`: \`${style.hash}\` (${style.textLength} characters).`);
  lines.push('', '## Policy summary', '', '| Policy | Cases | Violations | Console errors | Page errors | External requests | Overflow cases |', '|---|---:|---:|---:|---:|---:|---:|');
  for (const [policy, summary] of Object.entries(result.summary)) lines.push(`| ${policy} | ${summary.cases} | ${summary.violations} | ${summary.consoleErrors} | ${summary.pageErrors} | ${summary.externalRequests} | ${summary.overflowCases} |`);
  lines.push('', 'The exact policy is the current viewer policy.', 'The hash policy adds only the recorded SHA-256 sources for Vaadin-created global style elements and explicitly denies style attributes.', 'The element-inline, attribute-inline, and unmodified nonce variants are diagnostic comparisons and are not adoption recommendations.', 'The nonce-patched variant demonstrates that nonce propagation would work only after both Vaadin style-creation paths are changed to apply the application nonce.', '');
  return `${lines.join('\n')}\n`;
}
