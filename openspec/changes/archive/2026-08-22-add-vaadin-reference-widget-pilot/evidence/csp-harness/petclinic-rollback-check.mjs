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
const origin = process.env.PETCLINIC_ORIGIN ?? 'http://127.0.0.1:8080';
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const browser = await chromium.launch({headless: true, executablePath});
try {
  const page = await browser.newPage();
  const requests = [];
  const consoleErrors = [];
  page.on('request', request => requests.push(request.url()));
  page.on('console', message => { if (message.type() === 'error') consoleErrors.push(message.text()); });
  const response = await page.goto(`${origin}/htmx/object/petclinic.PetOwner/s_owner-mary`, {waitUntil: 'networkidle'});
  await page.waitForFunction(() => ['ready', 'partial-error'].includes(document.querySelector('[data-testid="causeway-route-page"]')?.dataset.routeState));
  await page.locator("causeway-action[member='removePet'] button").click();
  await page.locator("dialog[data-testid='action-prompt']").waitFor();
  const selector = "[data-testid='action-prompt-parameter-pet']";
  const result = {
    generatedAt: new Date().toISOString(),
    csp: response.headers()['content-security-policy'],
    assertions: {
      optInAttributeAbsent: await page.evaluate(() => !document.documentElement.dataset.causewayReferenceWidgets),
      styleHashesAbsent: !response.headers()['content-security-policy']?.includes('sha256-'),
      nativeEditorRestored: await page.locator(`${selector}:not(causeway-reference-editor)`).count() === 1,
      candidateEditorAbsent: await page.locator('causeway-reference-editor').count() === 0,
      candidateRequestAbsent: requests.every(url => !new URL(url).pathname.endsWith('/vaadin-reference/vaadin-reference.js')),
      zeroConsoleErrors: consoleErrors.length === 0
    }
  };
  await page.locator("[data-testid='action-prompt-cancel']").click();
  mkdirSync(resultsDirectory, {recursive: true});
  writeFileSync(resolve(resultsDirectory, 'petclinic-rollback.json'), `${JSON.stringify(result, null, 2)}\n`);
  console.log(JSON.stringify(result.assertions, null, 2));
  if (Object.values(result.assertions).some(value => !value)) throw new Error(`Rollback failed: ${JSON.stringify(result, null, 2)}`);
} finally {
  await browser.close();
}
