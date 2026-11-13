/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {spawn} from 'node:child_process';
import {mkdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {chromium} from 'playwright';

const directory = fileURLToPath(new URL('.', import.meta.url));
const evidenceDirectory = resolve(directory, '..');
const screenshotDirectory = resolve(evidenceDirectory, 'screenshots');
const resultDirectory = resolve(evidenceDirectory, 'results');
const origin = process.env.HARNESS_ORIGIN ?? 'http://127.0.0.1:4173';
const candidates = ['baseline', 'bootstrap', 'webawesome', 'openprops'];
const cases = [
  {name: 'desktop-light-menu', viewport: {width: 1440, height: 900}, theme: 'light', state: 'menu-open'},
  {name: 'desktop-dark-prompt', viewport: {width: 1440, height: 900}, theme: 'dark', state: 'prompt'},
  {name: 'narrow-light-nav', viewport: {width: 390, height: 844}, theme: 'light', state: 'responsive-nav', mobile: true},
  {name: 'narrow-dark-all', viewport: {width: 390, height: 844}, theme: 'dark', state: 'all', mobile: true}
];

mkdirSync(screenshotDirectory, {recursive: true});
mkdirSync(resultDirectory, {recursive: true});

const server = await ensureServer();
const executablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined;
const browser = await chromium.launch({headless: true, executablePath});
const evidence = {
  generatedAt: new Date().toISOString(),
  origin,
  playwrightVersion: '1.61.0',
  candidates: {},
  notes: [
    'Screenshots use identical fixture content and deterministic viewport, theme, and state parameters.',
    'DOM audits are bounded structural checks and do not replace Lighthouse or manual assistive-technology review.',
    'Performance values are local medians after one warm-up and exclude shared harness files from candidate transfer summaries.'
  ]
};

try {
  for (const candidate of candidates) {
    const candidateEvidence = {screenshots: [], interactions: {}, media: {}, performance: null};
    for (const testCase of cases) {
      const context = await browser.newContext({
        viewport: testCase.viewport,
        colorScheme: testCase.theme,
        reducedMotion: 'no-preference'
      });
      const page = await context.newPage();
      const failures = collectFailures(page);
      await installPerformanceObservers(page);
      const url = prototypeUrl(candidate, testCase);
      await page.goto(url, {waitUntil: 'networkidle'});
      await waitForFixture(page);
      const audit = await auditPage(page);
      const screenshotName = `${candidate}-${testCase.name}.jpg`;
      await page.screenshot({path: resolve(screenshotDirectory, screenshotName), fullPage: true, type: 'jpeg', quality: 82});
      candidateEvidence.screenshots.push({case: testCase.name, file: `screenshots/${screenshotName}`, url, audit, failures});
      await context.close();
    }
    candidateEvidence.interactions = await runInteractionJourneys(browser, candidate);
    candidateEvidence.media = await runPreferenceJourney(browser, candidate);
    candidateEvidence.performance = await measurePerformance(browser, candidate);
    evidence.candidates[candidate] = candidateEvidence;
  }
} finally {
  await browser.close();
  server?.kill();
}

const jsonTarget = resolve(resultDirectory, 'browser-evidence.json');
writeFileSync(jsonTarget, `${JSON.stringify(evidence, null, 2)}\n`);
writeFileSync(resolve(resultDirectory, 'browser-evidence.md'), renderMarkdown(evidence));
console.log(`Wrote ${jsonTarget}`);
console.log(summary(evidence));

async function ensureServer() {
  try {
    const response = await fetch(`${origin}/`);
    if (response.ok || response.status === 302) return null;
  } catch {
    // Start the local server below.
  }
  const child = spawn(process.execPath, [resolve(directory, 'server.mjs')], {
    cwd: directory,
    env: {...process.env, PORT: new URL(origin).port || '4173', HOST: new URL(origin).hostname},
    stdio: ['ignore', 'pipe', 'pipe']
  });
  const output = [];
  child.stdout.on('data', chunk => output.push(String(chunk)));
  child.stderr.on('data', chunk => output.push(String(chunk)));
  for (let attempt = 0; attempt < 50; attempt += 1) {
    await new Promise(resolvePromise => setTimeout(resolvePromise, 100));
    try {
      const response = await fetch(`${origin}/`);
      if (response.ok || response.status === 302) return child;
    } catch {
      // Retry until the bounded timeout.
    }
  }
  child.kill();
  throw new Error(`Harness server did not start: ${output.join('')}`);
}

function prototypeUrl(candidate, options = {}) {
  const url = new URL('/openspec/changes/analyze-web-component-theming-kit/evidence/harness/prototype.html', origin);
  url.searchParams.set('candidate', candidate);
  url.searchParams.set('theme', options.theme ?? 'light');
  url.searchParams.set('state', options.state ?? 'all');
  url.searchParams.set('motion', options.motion ?? 'normal');
  return url.href;
}

async function waitForFixture(page) {
  await page.waitForFunction(() => document.documentElement.dataset.fixtureReady === 'true', null, {timeout: 20_000});
}

function collectFailures(page) {
  const failures = [];
  page.on('pageerror', error => failures.push({kind: 'page-error', message: String(error)}));
  page.on('console', message => {
    if (message.type() === 'error') failures.push({kind: 'console-error', message: message.text()});
  });
  page.on('requestfailed', request => failures.push({kind: 'request-failed', message: `${request.method()} ${request.url()} ${request.failure()?.errorText ?? ''}`}));
  page.on('response', response => {
    if (response.status() >= 400) failures.push({kind: 'http-error', message: `${response.status()} ${response.url()}`});
  });
  return failures;
}

async function installPerformanceObservers(page) {
  await page.addInitScript(() => {
    globalThis.__causewayLcp = null;
    try {
      new PerformanceObserver(list => {
        const entries = list.getEntries();
        globalThis.__causewayLcp = entries.at(-1)?.startTime ?? globalThis.__causewayLcp;
      }).observe({type: 'largest-contentful-paint', buffered: true});
    } catch {
      // The result remains null when the browser does not expose LCP.
    }
  });
}

async function auditPage(page) {
  return page.evaluate(() => {
    const ids = [...document.querySelectorAll('[id]')].map(element => element.id);
    const duplicateIds = [...new Set(ids.filter((id, index) => ids.indexOf(id) !== index))];
    const unlabeledButtons = [...document.querySelectorAll('button, wa-button')]
      .filter(element => !(element.getAttribute('aria-label') || element.textContent?.trim()))
      .map(element => element.outerHTML.slice(0, 180));
    const unlabeledInputs = [...document.querySelectorAll('input, select, textarea')]
      .filter(element => !(element.labels?.length || element.getAttribute('aria-label') || element.getAttribute('aria-labelledby')))
      .map(element => element.outerHTML.slice(0, 180));
    const dialogsWithoutName = [...document.querySelectorAll('dialog, wa-dialog')]
      .filter(element => !(element.getAttribute('label') || element.getAttribute('aria-label') || element.getAttribute('aria-labelledby')))
      .map(element => element.outerHTML.slice(0, 180));
    const active = document.activeElement;
    const hiddenFocus = Boolean(active && active !== document.body && (active.hidden || active.closest('[hidden]')));
    return {
      title: document.title,
      heading: document.querySelector('h1')?.textContent?.trim(),
      duplicateIds,
      unlabeledButtons,
      unlabeledInputs,
      dialogsWithoutName,
      hiddenFocus,
      horizontalOverflow: document.documentElement.scrollWidth - document.documentElement.clientWidth,
      fixtureReady: document.documentElement.dataset.fixtureReady,
      bodyBackground: getComputedStyle(document.body).backgroundColor,
      bodyColor: getComputedStyle(document.body).color
    };
  });
}

async function runInteractionJourneys(browser, candidate) {
  const journeys = {};
  const context = await browser.newContext({viewport: {width: 1440, height: 900}, colorScheme: 'light'});
  const page = await context.newPage();
  const failures = collectFailures(page);
  await page.goto(prototypeUrl(candidate), {waitUntil: 'networkidle'});
  await waitForFixture(page);

  const menuTrigger = candidate === 'webawesome' ? 'wa-dropdown[data-app-dropdown] wa-button' : '[data-menu-trigger]';
  await page.locator(menuTrigger).click();
  await page.waitForTimeout(100);
  journeys.menuOpened = await menuState(page, candidate);
  await page.keyboard.press('Escape');
  await page.waitForTimeout(100);
  journeys.menuEscape = await menuState(page, candidate);

  await page.locator(menuTrigger).click();
  await page.waitForTimeout(100);
  const createAction = candidate === 'webawesome' ? 'wa-dropdown-item[data-menu-action="create"]' : '[data-menu-action="create"]';
  await page.locator(createAction).click();
  await page.waitForFunction(() => document.querySelector('[data-prompt]')?.open === true, null, {timeout: 10_000});
  await page.waitForTimeout(candidate === 'webawesome' ? 500 : 100);
  journeys.actionSelection = {
    menu: await menuState(page, candidate),
    prompt: await promptState(page),
    focused: await focusedElement(page)
  };
  await page.keyboard.press('Escape');
  await page.waitForTimeout(candidate === 'webawesome' ? 500 : 150);
  journeys.promptEscape = {prompt: await promptState(page), focused: await focusedElement(page)};

  const tabs = candidate === 'webawesome' ? page.locator('wa-tab') : page.locator('[role="tab"]');
  if (await tabs.count() > 1) {
    await tabs.nth(0).focus();
    await page.keyboard.press('ArrowRight');
    await page.waitForTimeout(100);
    journeys.tabs = await page.evaluate(expected => {
      if (expected === 'webawesome') {
        return [...document.querySelectorAll('wa-tab')].map(tab => ({text: tab.textContent.trim(), active: tab.active ?? tab.getAttribute('aria-selected')}));
      }
      return [...document.querySelectorAll('[role="tab"]')].map(tab => ({text: tab.textContent.trim(), active: tab.getAttribute('aria-selected'), focused: document.activeElement === tab}));
    }, candidate);
  }
  journeys.failures = failures;
  await context.close();
  return journeys;
}

async function menuState(page, candidate) {
  return page.evaluate(expected => {
    if (expected === 'webawesome') {
      const dropdown = document.querySelector('[data-app-dropdown]');
      const trigger = dropdown?.querySelector('wa-button');
      return {open: Boolean(dropdown?.open), focused: document.activeElement === trigger, trigger: trigger?.textContent?.trim()};
    }
    const trigger = document.querySelector('[data-menu-trigger]');
    const panel = document.querySelector('[data-menu-panel]');
    const nativeOpen = panel?.matches?.(':popover-open') ?? false;
    const nativePopoverSupported = expected === 'openprops' && 'showPopover' in HTMLElement.prototype;
    return {
      open: expected === 'openprops' ? (nativePopoverSupported ? nativeOpen : !panel?.hidden) : trigger?.getAttribute('aria-expanded') === 'true',
      ariaExpanded: trigger?.getAttribute('aria-expanded'),
      panelHidden: panel?.hidden,
      panelDisplay: panel ? getComputedStyle(panel).display : null,
      focused: document.activeElement === trigger
    };
  }, candidate);
}

async function promptState(page) {
  return page.evaluate(() => {
    const prompt = document.querySelector('[data-prompt]');
    return {open: Boolean(prompt?.open), localName: prompt?.localName};
  });
}

async function focusedElement(page) {
  return page.evaluate(() => {
    const active = document.activeElement;
    return active ? {localName: active.localName, id: active.id || null, text: active.textContent?.trim().slice(0, 80) || null, testField: active.hasAttribute?.('data-prompt-field') ?? false} : null;
  });
}

async function runPreferenceJourney(browser, candidate) {
  const context = await browser.newContext({
    viewport: {width: 390, height: 844},
    colorScheme: 'dark',
    reducedMotion: 'reduce',
    forcedColors: 'active'
  });
  const page = await context.newPage();
  const failures = collectFailures(page);
  await page.goto(prototypeUrl(candidate, {theme: 'dark', state: 'responsive-nav', motion: 'reduce'}), {waitUntil: 'networkidle'});
  await waitForFixture(page);
  const result = await page.evaluate(() => ({
    reducedMotion: matchMedia('(prefers-reduced-motion: reduce)').matches,
    forcedColors: matchMedia('(forced-colors: active)').matches,
    dark: matchMedia('(prefers-color-scheme: dark)').matches,
    horizontalOverflow: document.documentElement.scrollWidth - document.documentElement.clientWidth,
    navVisible: getComputedStyle(document.querySelector('#fixture-navigation')).display !== 'none'
  }));
  result.failures = failures;
  await context.close();
  return result;
}

async function measurePerformance(browser, candidate) {
  const context = await browser.newContext({viewport: {width: 1440, height: 900}, colorScheme: 'light'});
  const page = await context.newPage();
  const samples = [];
  for (let run = 0; run < 6; run += 1) {
    await installPerformanceObservers(page);
    await page.goto(prototypeUrl(candidate), {waitUntil: 'networkidle'});
    await waitForFixture(page);
    const sample = await page.evaluate(expected => {
      const navigation = performance.getEntriesByType('navigation')[0];
      const paint = Object.fromEntries(performance.getEntriesByType('paint').map(entry => [entry.name, entry.startTime]));
      const resources = performance.getEntriesByType('resource').map(entry => ({name: entry.name, transferSize: entry.transferSize, encodedBodySize: entry.encodedBodySize, duration: entry.duration}));
      const marker = expected === 'baseline' ? '/viewers/webcomponents/'
        : expected === 'bootstrap' ? '/node_modules/bootstrap/'
          : expected === 'webawesome' ? '/node_modules/@awesome.me/webawesome/'
            : '/node_modules/open-props/';
      const owned = resources.filter(entry => entry.name.includes(marker) || entry.name.includes(`/candidates/${expected === 'webawesome' ? 'web-awesome' : expected === 'openprops' ? 'open-props' : expected}.css`));
      return {
        domContentLoaded: navigation?.domContentLoadedEventEnd ?? null,
        load: navigation?.loadEventEnd ?? null,
        firstContentfulPaint: paint['first-contentful-paint'] ?? null,
        largestContentfulPaint: globalThis.__causewayLcp,
        candidateRequestCount: owned.length,
        candidateTransferBytes: owned.reduce((sum, entry) => sum + entry.transferSize, 0),
        candidateEncodedBytes: owned.reduce((sum, entry) => sum + entry.encodedBodySize, 0),
        candidateResourceDuration: owned.reduce((sum, entry) => sum + entry.duration, 0)
      };
    }, candidate);
    if (run > 0) samples.push(sample);
  }
  await context.close();
  return {samples, median: Object.fromEntries(Object.keys(samples[0] ?? {}).map(key => [key, median(samples.map(sample => sample[key]).filter(value => Number.isFinite(value)))]))};
}

function median(values) {
  if (values.length === 0) return null;
  const sorted = [...values].sort((left, right) => left - right);
  const middle = Math.floor(sorted.length / 2);
  return sorted.length % 2 ? sorted[middle] : (sorted[middle - 1] + sorted[middle]) / 2;
}

function renderMarkdown(result) {
  const lines = ['# Browser evidence summary', '', `Generated: ${result.generatedAt}`, ''];
  for (const [candidate, data] of Object.entries(result.candidates)) {
    const screenshotFailures = data.screenshots.flatMap(item => item.failures);
    lines.push(`## ${candidate}`, '');
    lines.push(`- Screenshots: ${data.screenshots.length}.`);
    lines.push(`- Browser failures: ${screenshotFailures.length + data.interactions.failures.length + data.media.failures.length}.`);
    lines.push(`- Horizontal overflow: ${data.screenshots.map(item => item.audit.horizontalOverflow).join(', ')} pixels.`);
    lines.push(`- Menu opened: ${JSON.stringify(data.interactions.menuOpened)}.`);
    lines.push(`- Menu after Escape: ${JSON.stringify(data.interactions.menuEscape)}.`);
    lines.push(`- Action selection: ${JSON.stringify(data.interactions.actionSelection)}.`);
    lines.push(`- Prompt after Escape: ${JSON.stringify(data.interactions.promptEscape)}.`);
    lines.push(`- Reduced motion / forced colors: ${data.media.reducedMotion} / ${data.media.forcedColors}.`);
    lines.push(`- Median performance: ${JSON.stringify(data.performance.median)}.`, '');
  }
  return `${lines.join('\n')}\n`;
}

function summary(result) {
  return Object.entries(result.candidates).map(([candidate, data]) => {
    const failures = data.screenshots.flatMap(item => item.failures).length + data.interactions.failures.length + data.media.failures.length;
    return `${candidate}: screenshots=${data.screenshots.length}, failures=${failures}, overflow=${Math.max(...data.screenshots.map(item => item.audit.horizontalOverflow))}`;
  }).join('\n');
}
