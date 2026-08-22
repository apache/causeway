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
import {startPilotServer} from './pilot-server.mjs';

const directory = fileURLToPath(new URL('.', import.meta.url));
const evidence = resolve(directory, '..');
const resultsDirectory = resolve(evidence, 'results');
const screenshotsDirectory = resolve(evidence, 'screenshots');
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const browser = await chromium.launch({headless: true, executablePath});
const {server, origin} = await startPilotServer();
const allScenarios = [
  {name: 'desktop-light', viewport: {width: 1280, height: 900}, colorScheme: 'light'},
  {name: 'desktop-dark-reduced', viewport: {width: 1280, height: 900}, colorScheme: 'dark', reducedMotion: 'reduce'},
  {name: 'narrow-light', viewport: {width: 390, height: 844}, colorScheme: 'light'},
  {name: 'narrow-dark-reduced', viewport: {width: 390, height: 844}, colorScheme: 'dark', reducedMotion: 'reduce'},
  {name: 'forced-colors', viewport: {width: 1280, height: 900}, colorScheme: 'light', forcedColors: 'active'}
];
const scenarios = process.env.PILOT_SCENARIO
  ? allScenarios.filter(scenario => scenario.name === process.env.PILOT_SCENARIO)
  : allScenarios;

try {
  mkdirSync(resultsDirectory, {recursive: true});
  mkdirSync(screenshotsDirectory, {recursive: true});
  const results = [];
  for (const scenario of scenarios) results.push(await runScenario(scenario));
  const evidenceResult = {
    generatedAt: new Date().toISOString(),
    browserVersion: browser.version(),
    headless: true,
    scenarios: results,
    summary: {
      scenarios: results.length,
      axeViolations: results.reduce((sum, result) => sum + result.axe.violations.length, 0),
      cspViolations: results.reduce((sum, result) => sum + result.cspViolations.length, 0),
      consoleErrors: results.reduce((sum, result) => sum + result.consoleErrors.length, 0),
      pageErrors: results.reduce((sum, result) => sum + result.pageErrors.length, 0),
      externalRequests: results.reduce((sum, result) => sum + result.externalRequests.length, 0),
      overflowScenarios: results.filter(result => result.snapshot.horizontalOverflow > 0).length
    }
  };
  writeFileSync(resolve(resultsDirectory, 'pilot-browser.json'), `${JSON.stringify(evidenceResult, null, 2)}\n`);
  console.log(JSON.stringify(evidenceResult.summary, null, 2));
  if (Object.entries(evidenceResult.summary).some(([name, value]) => name !== 'scenarios' && value !== 0)) throw new Error(`Pilot browser gates failed: ${JSON.stringify(evidenceResult.summary)}`);
} finally {
  await browser.close();
  await new Promise(resolveClose => server.close(resolveClose));
}

async function runScenario(scenario) {
  const context = await browser.newContext({viewport: scenario.viewport, colorScheme: scenario.colorScheme, reducedMotion: scenario.reducedMotion, forcedColors: scenario.forcedColors});
  const page = await context.newPage();
  const requests = [];
  const consoleErrors = [];
  const pageErrors = [];
  await page.addInitScript(() => {
    window.__cspViolations = [];
    document.addEventListener('securitypolicyviolation', event => window.__cspViolations.push({effectiveDirective: event.effectiveDirective, blockedURI: event.blockedURI, sourceFile: event.sourceFile}));
  });
  page.on('request', request => requests.push(request.url()));
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
  page.on('pageerror', error => pageErrors.push(error.message));
  await page.goto(`${origin}/pilot.html`, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => window.pilot?.ready === true);
  const candidateRequestsBeforeEditor = requests.filter(url => url.endsWith('/production/vaadin-reference.js')).length;
  const initializationStart = await page.evaluate(() => performance.now());
  await page.evaluate(() => window.pilot.create({id: 'single-owner'}));
  await page.waitForFunction(() => document.querySelector('[data-testid="single-owner"] causeway-reference-editor')?.dataset.widgetState === 'ready');
  const initializationMs = await page.evaluate(start => performance.now() - start, initializationStart);
  await page.evaluate(() => window.pilot.create({id: 'required-owner', required: true}));
  await page.waitForFunction(() => document.querySelector('[data-testid="required-owner"] causeway-reference-editor')?.dataset.widgetState === 'ready');
  await page.evaluate(() => window.pilot.create({id: 'disabled-owner', disabledControl: true}));
  await page.waitForFunction(() => document.querySelector('[data-testid="disabled-owner"] causeway-reference-editor')?.dataset.widgetState === 'ready');
  await page.evaluate(() => window.pilot.create({id: 'multiple-owners', multiple: true, custom: true}));
  await page.waitForFunction(() => document.querySelector('[data-testid="multiple-owners"] causeway-reference-editor')?.dataset.widgetState === 'ready');
  await page.evaluate(() => window.pilot.create({id: 'search-owner', autocomplete: true, custom: true}));
  await page.waitForFunction(() => document.querySelector('[data-testid="search-owner"] causeway-reference-editor')?.dataset.widgetState === 'ready');
  if (scenario.name === 'desktop-light') await keyboardJourney(page);
  await page.addScriptTag({url: `${origin}/axe.js`});
  const axe = await page.evaluate(async () => axe.run(document, {resultTypes: ['violations', 'incomplete']}));
  const snapshot = await page.evaluate(() => window.pilot.snapshot());
  const cspViolations = await page.evaluate(() => window.__cspViolations);
  const screenshot = `pilot-${scenario.name}.jpg`;
  await page.screenshot({path: resolve(screenshotsDirectory, screenshot), type: 'jpeg', quality: 82, fullPage: true});
  const candidateRequests = requests.filter(url => url.endsWith('/production/vaadin-reference.js')).length;
  const externalRequests = requests.filter(url => new URL(url).origin !== origin);
  const candidateResource = await page.evaluate(path => {
    const entry = performance.getEntriesByType('resource').find(resource => new URL(resource.name).pathname === path);
    return entry ? {duration: entry.duration, transferSize: entry.transferSize, encodedBodySize: entry.encodedBodySize, decodedBodySize: entry.decodedBodySize} : null;
  }, '/production/vaadin-reference.js');
  const result = {scenario, candidateRequestsBeforeEditor, candidateRequests, initializationMs, candidateResource, snapshot, axe: {violations: axe.violations, incomplete: axe.incomplete}, cspViolations, consoleErrors, pageErrors, externalRequests, screenshot};
  if (candidateRequestsBeforeEditor !== 0 || candidateRequests !== 1 || !snapshot.vaadinDefined || snapshot.flowRuntime || snapshot.editors['disabled-owner']?.controlDisabled !== true) {
    result.pageErrors.push(`Lazy-loading or disabled-state assertion failed: ${JSON.stringify({candidateRequestsBeforeEditor, candidateRequests, vaadinDefined: snapshot.vaadinDefined, flowRuntime: snapshot.flowRuntime, disabled: snapshot.editors['disabled-owner']?.controlDisabled})}`);
  }
  await context.close();
  return result;
}

async function keyboardJourney(page) {
  const singleInput = page.locator('[data-testid="single-owner"] vaadin-combo-box input');
  await singleInput.focus();
  await page.keyboard.press('Meta+A');
  await page.keyboard.type('Owner 002');
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForFunction(() => window.pilot.snapshot().editors['single-owner']?.pendingValue?.id === 'owner-2');

  const multiInput = page.locator('[data-testid="multiple-owners"] vaadin-multi-select-combo-box input');
  await multiInput.focus();
  await page.keyboard.type('Owner 002');
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.keyboard.press('Escape');
  await page.waitForFunction(() => window.pilot.snapshot().editors['multiple-owners']?.pendingValue?.length === 2);
  await multiInput.focus();
  await page.keyboard.press('Backspace');
  await page.keyboard.press('Backspace');
  await page.waitForFunction(() => window.pilot.snapshot().editors['multiple-owners']?.pendingValue?.length === 1);

  let searchInput = page.locator('[data-testid="search-owner"] vaadin-combo-box input');
  await searchInput.focus();
  await page.keyboard.press('Meta+A');
  await page.keyboard.type('Owner 002');
  await page.waitForFunction(() => window.pilot.snapshot().editors['search-owner']?.calls.includes('search:Owner 002'));
  await page.waitForFunction(() => document.querySelector('[data-testid="search-owner"] vaadin-combo-box')?.items?.length === 1);
  searchInput = page.locator('[data-testid="search-owner"] vaadin-combo-box input');
  await page.locator('[data-testid="search-owner"] vaadin-combo-box').evaluate(control => { control.opened = true; });
  await searchInput.focus();
  await page.waitForTimeout(100);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForFunction(() => window.pilot.snapshot().editors['search-owner']?.pendingValue?.id === 'owner-2');

  await page.locator('[data-testid="single-owner"] vaadin-combo-box [part~="clear-button"]').click();
  await page.waitForFunction(() => window.pilot.snapshot().editors['single-owner']?.pendingValue == null);
  await page.evaluate(() => window.pilot.clear('required-owner'));
  await page.evaluate(() => window.pilot.validate('required-owner'));
  await page.waitForFunction(() => /Select at least one owner/.test(window.pilot.snapshot().editors['required-owner']?.error ?? ''));
  await page.evaluate(() => window.pilot.remove('single-owner'));
  await page.waitForTimeout(50);
  await page.evaluate(() => window.pilot.create({id: 'single-owner', required: true}));
  await page.waitForFunction(() => document.querySelector('[data-testid="single-owner"] causeway-reference-editor')?.dataset.widgetState === 'ready');
}
