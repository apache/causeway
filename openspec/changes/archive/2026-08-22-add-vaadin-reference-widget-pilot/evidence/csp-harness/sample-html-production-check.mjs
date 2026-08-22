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
const origin = process.env.SAMPLE_HTML_ORIGIN ?? 'http://127.0.0.1:8080';
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const browser = await chromium.launch({headless: true, executablePath});

try {
  const context = await browser.newContext({viewport: {width: 1280, height: 900}, colorScheme: 'light'});
  const page = await context.newPage();
  const requests = [];
  const consoleErrors = [];
  const pageErrors = [];
  const httpFailures = [];
  page.on('request', request => requests.push(request.url()));
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push({text: message.text(), location: message.location()}); });
  page.on('pageerror', error => pageErrors.push(error.message));
  page.on('response', async response => {
    if (response.status() >= 400) {
      let body = null;
      try {
        body = await response.text();
      } catch {
        body = '(response body unavailable)';
      }
      httpFailures.push({status: response.status(), url: response.url(), request: response.request().postData(), body});
    }
  });
  await page.goto(`${origin}/sample-html/index.html`, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="sample-app"]')?.dataset.state));
  const candidatePath = '/causeway-webcomponents/vaadin-reference/vaadin-reference.js';
  const before = requests.filter(url => new URL(url).pathname === candidatePath).length;
  const consoleErrorsBeforePrompt = consoleErrors.length;
  const httpFailuresBeforePrompt = httpFailures.length;
  await page.locator("[data-testid='action-select-related'] button").click();
  await page.locator("dialog[data-testid='action-prompt']").waitFor();
  await page.waitForFunction(() => document.querySelector("causeway-reference-editor[data-testid='action-prompt-parameter-related']")?.dataset.widgetState === 'ready');
  const input = page.locator("[data-testid='action-prompt-parameter-related'] vaadin-combo-box input");
  await input.focus();
  await page.keyboard.type('Schema');
  await page.waitForTimeout(100);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await page.waitForFunction(() => document.querySelector("causeway-reference-editor[data-testid='action-prompt-parameter-related']")?.value?.id);
  const selected = await page.locator("causeway-reference-editor[data-testid='action-prompt-parameter-related']").evaluate(element => element.value);
  await page.locator("[data-testid='action-prompt-parameter-related'] vaadin-combo-box input").focus();
  await page.keyboard.press('Escape');
  await page.waitForFunction(() => !document.querySelector("dialog[data-testid='action-prompt']"));
  const after = requests.filter(url => new URL(url).pathname === candidatePath).length;
  const externalRequests = requests.filter(url => new URL(url).origin !== origin);
  const result = {
    generatedAt: new Date().toISOString(),
    browserVersion: browser.version(),
    selected,
    requestsBeforePrompt: before,
    candidateRequests: after,
    consoleErrorsBeforePrompt,
    httpFailuresBeforePrompt,
    consoleErrors,
    httpFailures,
    pageErrors,
    externalRequests,
    assertions: {
      noCandidateBeforePrompt: before === 0,
      candidateLoadedOnce: after === 1,
      stableReferenceSelected: Boolean(selected?.id),
      promptDismissed: await page.locator("dialog[data-testid='action-prompt']").count() === 0,
      focusRestored: await page.locator("[data-testid='action-select-related'] button").evaluate(element => document.activeElement === element),
      noFlowRuntime: await page.evaluate(() => !window.Vaadin?.Flow?.clients),
      noOverflow: await page.evaluate(() => document.documentElement.scrollWidth === document.documentElement.clientWidth),
      zeroConsoleErrors: consoleErrors.length === 0,
      zeroPageErrors: pageErrors.length === 0,
      zeroHttpFailures: httpFailures.length === 0,
      zeroExternalRequests: externalRequests.length === 0
    }
  };
  mkdirSync(resultsDirectory, {recursive: true});
  writeFileSync(resolve(resultsDirectory, 'sample-html-production-pilot.json'), `${JSON.stringify(result, null, 2)}\n`);
  console.log(JSON.stringify(result.assertions, null, 2));
  if (Object.values(result.assertions).some(value => !value)) throw new Error(`Vanilla sample pilot failed: ${JSON.stringify(result, null, 2)}`);
  await context.close();
} finally {
  await browser.close();
}
