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

const directory = fileURLToPath(new URL('.', import.meta.url));
const resultsDirectory = resolve(directory, '../results');
const screenshotsDirectory = resolve(directory, '../screenshots');
const origin = process.env.PETCLINIC_ORIGIN ?? 'http://127.0.0.1:8080';
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const browser = await chromium.launch({headless: true, executablePath});

try {
  const context = await browser.newContext({viewport: {width: 1280, height: 900}, colorScheme: 'light'});
  const page = await context.newPage();
  page.setDefaultTimeout(40_000);
  const requests = [];
  const consoleErrors = [];
  const pageErrors = [];
  const httpFailures = [];
  await page.addInitScript(() => {
    window.__pilotCspViolations = [];
    document.addEventListener('securitypolicyviolation', event => window.__pilotCspViolations.push({
      effectiveDirective: event.effectiveDirective,
      blockedURI: event.blockedURI,
      sourceFile: event.sourceFile,
      lineNumber: event.lineNumber
    }));
  });
  page.on('request', request => requests.push(request.url()));
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push({text: message.text(), location: message.location()}); });
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('response', response => { if (response.status() >= 400) httpFailures.push({status: response.status(), url: response.url()}); });
  await page.goto(`${origin}/htmx`, {waitUntil: 'networkidle'});
  await waitForReady(page);
  const shellCsp = (await page.request.get(`${origin}/htmx`)).headers()['content-security-policy'];
  const candidatePath = '/causeway-webcomponents/vaadin-reference/vaadin-reference.js';
  const requestsBeforeReferencePrompt = requests.filter(url => new URL(url).pathname === candidatePath).length;

  await page.goto(`${origin}/htmx/object/petclinic.PetOwner/s_owner-mary`, {waitUntil: 'domcontentloaded'});
  await waitForReady(page);
  await page.locator("causeway-action[member='removePet'] button").click();
  await page.locator("dialog[data-testid='action-prompt']").waitFor();
  await page.waitForTimeout(500);
  if (await page.locator("causeway-reference-editor[data-testid='action-prompt-parameter-pet']").count() === 0) {
    throw new Error(`Reference editor was not selected: ${await page.locator("dialog[data-testid='action-prompt']").evaluate(element => element.outerHTML)}`);
  }
  await page.waitForFunction(() => document.querySelector("causeway-reference-editor[data-testid='action-prompt-parameter-pet']")?.dataset.widgetState === 'ready');
  const input = page.locator("[data-testid='action-prompt-parameter-pet'] vaadin-combo-box input");
  await input.focus();
  await page.keyboard.type('Basil');
  await page.waitForTimeout(100);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForFunction(() => document.querySelector("causeway-reference-editor[data-testid='action-prompt-parameter-pet']")?.value?.id);
  const selectedIdentity = await page.locator("causeway-reference-editor[data-testid='action-prompt-parameter-pet']").evaluate(element => element.value);
  await page.locator("[data-testid='action-prompt-parameter-pet'] vaadin-combo-box input").focus();
  await page.keyboard.press('Escape');
  await page.waitForTimeout(100);
  await page.keyboard.press('Escape');
  await page.waitForFunction(() => !document.querySelector("dialog[data-testid='action-prompt']"));
  await page.waitForTimeout(100);

  const snapshot = await page.evaluate(() => ({
    routeState: document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState,
    menuState: document.querySelector('causeway-menubars')?.dataset.menuState,
    flowRuntime: Boolean(window.Vaadin?.Flow?.clients),
    overlayCount: document.querySelectorAll('vaadin-combo-box-overlay, vaadin-multi-select-combo-box-overlay').length,
    horizontalOverflow: Math.max(0, document.documentElement.scrollWidth - document.documentElement.clientWidth),
    activeMember: document.activeElement?.closest?.('causeway-action')?.getAttribute('member') ?? null
  }));
  const cspViolations = await page.evaluate(() => window.__pilotCspViolations);
  const candidateRequests = requests.filter(url => new URL(url).pathname === candidatePath).length;
  const externalRequests = requests.filter(url => new URL(url).origin !== origin);
  mkdirSync(resultsDirectory, {recursive: true});
  mkdirSync(screenshotsDirectory, {recursive: true});
  const screenshot = 'petclinic-reference-pilot.jpg';
  await page.screenshot({path: resolve(screenshotsDirectory, screenshot), type: 'jpeg', quality: 82, fullPage: true});
  const result = {
    generatedAt: new Date().toISOString(),
    browserVersion: browser.version(),
    shellCsp,
    requestsBeforeReferencePrompt,
    candidateRequests,
    selectedIdentity,
    snapshot,
    cspViolations,
    consoleErrors,
    pageErrors,
    httpFailures,
    externalRequests,
    screenshot,
    assertions: {
      optInConfigurationPresent: await page.evaluate(() => document.documentElement.dataset.causewayReferenceWidgets === 'vaadin'),
      exactHashesPresent: shellCsp?.includes("style-src-elem 'self' 'sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw='"),
      noBlanketInlinePermission: !shellCsp?.includes("'unsafe-inline'"),
      noCandidateBeforePrompt: requestsBeforeReferencePrompt === 0,
      candidateLoadedOnce: candidateRequests === 1,
      stableReferenceSelected: Boolean(selectedIdentity?.id),
      routeRemainedReady: ['ready', 'partial-error'].includes(snapshot.routeState),
      menuRemainedReady: ['ready', 'partial-error'].includes(snapshot.menuState),
      focusRestored: snapshot.activeMember === 'removePet',
      noFlowRuntime: !snapshot.flowRuntime,
      noOverflow: snapshot.horizontalOverflow === 0,
      zeroCspViolations: cspViolations.length === 0,
      zeroConsoleErrors: consoleErrors.length === 0,
      zeroPageErrors: pageErrors.length === 0,
      zeroHttpFailures: httpFailures.length === 0,
      zeroExternalRequests: externalRequests.length === 0
    }
  };
  writeFileSync(resolve(resultsDirectory, 'petclinic-production-pilot.json'), `${JSON.stringify(result, null, 2)}\n`);
  console.log(JSON.stringify(result.assertions, null, 2));
  if (Object.values(result.assertions).some(value => !value)) throw new Error(`Petclinic production pilot failed: ${JSON.stringify(result, null, 2)}`);
  await context.close();
} finally {
  await browser.close();
}

async function waitForReady(page) {
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState));
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('causeway-menubars')?.dataset.menuState));
}
