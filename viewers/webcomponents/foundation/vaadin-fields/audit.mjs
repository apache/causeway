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
const outputDirectory = process.env.CAUSEWAY_FIELD_AUDIT_OUTPUT
  ? resolve(process.cwd(), process.env.CAUSEWAY_FIELD_AUDIT_OUTPUT)
  : null;
if (outputDirectory) mkdirSync(outputDirectory, {recursive: true});
const axePath = resolve(directory, 'node_modules/axe-core/axe.min.js');
const families = {
  basic: ['vaadin-text-field', 'vaadin-text-area', 'vaadin-password-field', 'vaadin-checkbox', 'vaadin-select'],
  numeric: ['vaadin-text-field', 'vaadin-integer-field', 'vaadin-number-field'],
  'local-temporal': ['vaadin-date-picker', 'vaadin-time-picker', 'vaadin-date-time-picker']
};
const results = {};

for (const [family, controls] of Object.entries(families)) {
  results[family] = await auditFamily(family, controls, writePolicy ? [] : policy.families[family].cspStyleHashes);
  if (writePolicy) policy.families[family].cspStyleHashes = results[family].styleHashes;
}
if (writePolicy) writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);
if (outputDirectory) writeFileSync(resolve(outputDirectory, 'field-audit.json'), `${JSON.stringify(results, null, 2)}\n`);
console.log(JSON.stringify(results, null, 2));
if (!writePolicy && Object.values(results).some(result => result.cspViolations.length || result.axeViolations.length
    || result.consoleErrors.length || result.pageErrors.length || result.externalRequests.length || result.overflow
    || result.readOnlyState.some(state => state.changed || state.opened || !state.named || !state.described || !state.readOnly)
    || result.timePickerState.some(state => !state.minuteStep || !state.hasToggle || !state.pointerOperable))) {
  process.exitCode = 1;
}

async function auditFamily(family, controls, hashes) {
  const csp = hashes.length ? policyFor(hashes) : null;
  const server = createServer((request, response) => {
    const url = new URL(request.url, 'http://127.0.0.1');
    if (csp) response.setHeader('Content-Security-Policy', csp);
    if (url.pathname === '/') return send(response, 'text/html', html(family));
    if (url.pathname === '/fixture.js') return send(response, 'text/javascript', fixture(family, controls));
    if (url.pathname === '/fixture.css') return send(response, 'text/css', 'vaadin-text-field,vaadin-text-area,vaadin-password-field,vaadin-checkbox,vaadin-select,vaadin-integer-field,vaadin-number-field,vaadin-date-picker,vaadin-time-picker,vaadin-date-time-picker{display:block;box-sizing:border-box;width:100%;max-width:100%}');
    if (url.pathname === '/asset.js') return send(response, 'text/javascript', readFileSync(resolve(directory, 'generated/assets', `vaadin-${family}.js`)));
    if (url.pathname === '/axe.js') return send(response, 'text/javascript', readFileSync(axePath));
    response.writeHead(404).end();
  });
  await new Promise(resolveReady => server.listen(0, '127.0.0.1', resolveReady));
  const port = server.address().port;
  const origin = `http://127.0.0.1:${port}`;
  const browser = await chromium.launch({headless: true});
  const page = await browser.newPage({viewport: {width: 390, height: 844}, colorScheme: 'dark', reducedMotion: 'reduce'});
  const cspViolations = [];
  const consoleErrors = [];
  const pageErrors = [];
  const externalRequests = [];
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
  try {
    await page.waitForFunction(() => document.body.dataset.ready === 'true');
  } catch (error) {
    throw new Error(`Field audit fixture did not become ready: ${JSON.stringify({family, consoleErrors, pageErrors})}`, {cause: error});
  }
  for (const [index, tag] of controls.entries()) {
    const locator = page.locator(`#control-${index}`);
    await locator.focus();
    if (tag === 'vaadin-checkbox') await locator.click();
    else if (tag === 'vaadin-select' || tag.endsWith('-picker')) await locator.click();
    else await locator.evaluate((control, value) => {
      control.value = value;
      control.dispatchEvent(new Event('input', {bubbles: true, composed: true}));
    }, tag.includes('number') || tag.includes('integer') ? '42' : 'changed');
    await page.keyboard.press('Escape');
    await page.waitForTimeout(50);
    await page.keyboard.press('Escape');
    await locator.evaluate(control => {
      if ('opened' in control) control.opened = false;
      control.blur();
    });
  }
  const timePickerState = family === 'local-temporal' ? await page.evaluate(() => {
    const states = [];
    for (const control of document.querySelectorAll('[id^="control-"]')) {
      const pickers = control.localName === 'vaadin-time-picker' ? [control] : [];
      for (const root of [control, control.shadowRoot]) {
        pickers.push(...(root?.querySelectorAll?.('vaadin-time-picker') ?? []));
      }
      for (const picker of new Set(pickers)) {
        const trigger = picker.shadowRoot?.querySelector('[part~="toggle-button"]');
        const before = Boolean(picker.opened);
        trigger?.click();
        const pointerOperable = !before && Boolean(picker.opened);
        picker.close?.();
        states.push({
          owner: control.id,
          minuteStep: picker.step === 60,
          hasToggle: Boolean(trigger),
          pointerOperable
        });
      }
    }
    return states;
  }) : [];
  const readOnlyState = [];
  for (const [index, tag] of controls.entries()) {
    const locator = page.locator(`#view-${index}`);
    const before = await locator.evaluate(control => ({
      value: 'checked' in control ? control.checked : control.value,
      opened: Boolean(control.opened)
    }));
    await locator.focus();
    if (tag === 'vaadin-checkbox') {
      await page.keyboard.press('Space');
      await locator.click({force: true});
    } else if (tag === 'vaadin-select' || tag.endsWith('-picker')) {
      await page.keyboard.press('ArrowDown');
      await page.keyboard.press('Enter');
      await locator.click({force: true});
      await page.keyboard.press('Escape');
    } else {
      await page.keyboard.type('changed');
    }
    const after = await locator.evaluate(control => ({
      value: 'checked' in control ? control.checked : control.value,
      opened: Boolean(control.opened),
      readOnly: control.readOnly === true && control.hasAttribute('readonly'),
      named: (() => {
        const labelId = `${control.id}-label`;
        const inputs = [control.inputElement, control.focusElement];
        for (const root of [control, control.shadowRoot]) {
          for (const field of root?.querySelectorAll('vaadin-date-picker, vaadin-time-picker') ?? []) {
            inputs.push(field.inputElement, field.focusElement);
          }
        }
        const targets = [...new Set(inputs.filter(Boolean))];
        const composite = control.querySelectorAll('vaadin-date-picker, vaadin-time-picker').length > 0;
        if (composite) return targets.length > 0 && targets.every(input => input.getAttribute('aria-label'));
        return control.accessibleNameRef === labelId
          ? targets.some(input => input.getAttribute('aria-labelledby')?.split(' ').includes(labelId))
          : targets.length > 0 && targets.every(input => input.getAttribute('aria-label'));
      })(),
      described: (() => {
        const descriptionId = `${control.id}-description`;
        const inputs = [control.inputElement, control.focusElement];
        for (const root of [control, control.shadowRoot]) {
          for (const field of root?.querySelectorAll('vaadin-date-picker, vaadin-time-picker') ?? []) {
            inputs.push(field.inputElement, field.focusElement);
          }
        }
        const targets = [...new Set(inputs.filter(Boolean))];
        return targets.length > 0 && targets.every(input => input.getAttribute('aria-describedby')?.split(' ').includes(descriptionId));
      })()
    }));
    readOnlyState.push({tag, changed: before.value !== after.value, opened: before.opened || after.opened, ...after});
  }
  await page.waitForTimeout(500);
  const styleTexts = await page.locator('style').evaluateAll(styles => styles.map(style => style.textContent).filter(Boolean));
  const styleHashes = [...new Set(styleTexts.map(text => `sha256-${createHash('sha256').update(text).digest('base64')}`))].sort();
  const axeViolations = await page.evaluate(async () => (await globalThis.axe.run(document, {
    rules: {'color-contrast': {enabled: false}}
  })).violations.map(violation => violation.id));
  const accessibilityState = await page.evaluate(() => ({
    opened: [...document.querySelectorAll('[opened]')].map(element => element.localName),
    hidden: [...document.querySelectorAll('[aria-hidden="true"]')].map(element => element.localName)
  }));
  const narrowOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, `${family}-narrow-dark.png`), fullPage: true});
  await page.emulateMedia({forcedColors: 'active', colorScheme: 'light', reducedMotion: 'no-preference'});
  await page.setViewportSize({width: 1280, height: 800});
  const forcedColorAxe = await page.evaluate(async () => (await globalThis.axe.run(document, {
    rules: {'color-contrast': {enabled: false}}
  })).violations.map(violation => violation.id));
  axeViolations.push(...forcedColorAxe.filter(id => !axeViolations.includes(id)));
  const wideOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  if (outputDirectory) await page.screenshot({path: resolve(outputDirectory, `${family}-wide-forced-colors.png`), fullPage: true});
  const overflow = narrowOverflow || wideOverflow;
  await browser.close();
  await new Promise(resolveClosed => server.close(resolveClosed));
  return {
    styleHashes,
    cspViolations,
    axeViolations,
    accessibilityState,
    readOnlyState,
    timePickerState,
    presentation: {narrowDarkReducedMotion: !narrowOverflow, wideForcedColors: !wideOverflow},
    consoleErrors,
    pageErrors,
    externalRequests,
    overflow
  };
}

function html(family) {
  return `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>${family} field audit</title><link rel="stylesheet" href="/fixture.css"><script src="/axe.js"></script><script type="module" src="/fixture.js"></script></head><body><main><h1>${family} fields</h1><div id="fixture"></div></main></body></html>`;
}

function fixture(family, controls) {
  const setups = controls.map((tag, index) => `{
    const control = document.createElement('${tag}');
    control.label = '${tag.replace('vaadin-', '')}';
    control.id = 'control-${index}';
    control.required = ${index === 0};
    if ('items' in control) control.items = [{label: 'One', value: 'one'}, {label: 'Two', value: 'two'}];
    if ('${tag}' === 'vaadin-time-picker' || '${tag}' === 'vaadin-date-time-picker') control.step = 60;
    if ('${tag}' === 'vaadin-checkbox') control.checked = true;
    else if ('value' in control) control.value = ${JSON.stringify(initialValue(family, tag))};
    fixture.append(control);
    await control.updateComplete;
    for (const field of control.querySelectorAll('vaadin-date-picker, vaadin-time-picker')) {
      field.accessibleName = control.label + (field.localName === 'vaadin-date-picker' ? ' date' : ' time');
    }

    const label = document.createElement('label');
    label.id = 'view-${index}-label';
    label.textContent = 'Read-only ${tag.replace('vaadin-', '')}';
    const description = document.createElement('span');
    description.id = 'view-${index}-description';
    description.textContent = 'Authoritative Causeway value';
    const view = document.createElement('${tag}');
    view.id = 'view-${index}';
    view.readOnly = true;
    view.setAttribute('readonly', '');
    if ('accessibleNameRef' in view) view.accessibleNameRef = label.id;
    else view.accessibleName = label.textContent;
    if ('items' in view) view.items = [{label: 'One', value: 'one'}, {label: 'Two', value: 'two'}];
    if ('${tag}' === 'vaadin-time-picker' || '${tag}' === 'vaadin-date-time-picker') view.step = 0.001;
    if ('${tag}' === 'vaadin-checkbox') view.checked = true;
    else if ('value' in view) view.value = ${JSON.stringify(initialValue(family, tag))};
    fixture.append(label, description, view);
    await view.updateComplete;
    const compositeFields = [...view.querySelectorAll('vaadin-date-picker, vaadin-time-picker')];
    for (const field of compositeFields) {
      field.accessibleName = label.textContent + (field.localName === 'vaadin-date-picker' ? ' date' : ' time');
    }
    await Promise.all(compositeFields.map(field => field.updateComplete));
    const inputs = [view.inputElement, view.focusElement];
    for (const root of [view, view.shadowRoot]) {
      for (const field of root?.querySelectorAll('vaadin-date-picker, vaadin-time-picker') ?? []) {
        inputs.push(field.inputElement, field.focusElement);
      }
    }
    for (const input of new Set(inputs.filter(Boolean))) input.setAttribute('aria-describedby', description.id);
  }`).join('\n');
  return `import '/asset.js';
const fixture = document.querySelector('#fixture');
await Promise.all(${JSON.stringify(controls)}.map(tag => customElements.whenDefined(tag)));
${setups}
await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
document.body.dataset.ready = 'true';`;
}

function initialValue(family, tag) {
  if (tag === 'vaadin-checkbox') return false;
  if (tag === 'vaadin-select') return 'one';
  if (tag === 'vaadin-date-picker') return '2026-08-24';
  if (tag === 'vaadin-time-picker') return '13:14:15.123';
  if (tag === 'vaadin-date-time-picker') return '2026-08-24T13:14:15.123';
  if (family === 'numeric') return '12';
  return tag === 'vaadin-password-field' ? '' : 'initial';
}

function policyFor(hashes) {
  const sources = hashes.map(hash => `'${hash}'`).join(' ');
  return `default-src 'self'; script-src 'self'; style-src 'self' ${sources}; style-src-elem 'self' ${sources}; style-src-attr 'none'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'`;
}

function send(response, type, body) {
  response.writeHead(200, {'Content-Type': type, 'Cache-Control': 'no-store'});
  response.end(body);
}
