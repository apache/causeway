/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {gzipSync} from 'node:zlib';
import {mkdirSync, readFileSync, readdirSync, writeFileSync} from 'node:fs';
import {dirname, extname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = resolve(directory, '../../../../..');

const groups = {
  baseline: [
    ...collect('viewers/webcomponents/foundation/src', new Set(['.css', '.mjs'])),
    ...collect('viewers/webcomponents/htmx/src/main/resources/META-INF/resources/causeway-htmx', new Set(['.css', '.mjs'])),
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/candidates/baseline.css'
  ],
  bootstrapCss: [
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/bootstrap/dist/css/bootstrap.min.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/candidates/bootstrap.css'
  ],
  bootstrapOptionalJavascript: [
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/bootstrap/dist/js/bootstrap.bundle.min.js'
  ],
  webAwesomeEntry: [
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/@awesome.me/webawesome/dist-cdn/styles/webawesome.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/@awesome.me/webawesome/dist-cdn/webawesome.loader.js',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/candidates/web-awesome.css'
  ],
  webAwesomeSelectiveBundle: [
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/generated/webawesome-selective.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/generated/webawesome-selective.js',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/candidates/web-awesome.css'
  ],
  webAwesomeCompleteDistribution: collect('openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/@awesome.me/webawesome/dist-cdn', new Set(['.css', '.js', '.svg', '.woff2'])),
  openProps: [
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/open-props/open-props.min.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/open-props/normalize.min.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/node_modules/open-props/buttons.min.css',
    'openspec/changes/analyze-web-component-theming-kit/evidence/harness/candidates/open-props.css'
  ]
};

const measurements = Object.fromEntries(Object.entries(groups).map(([name, files]) => [name, measure(files)]));
const result = {
  generatedAt: new Date().toISOString(),
  note: 'Groups measure candidate-owned assets. Shared harness files are excluded. Complete distributions are not equivalent to browser-loaded selective assets.',
  measurements
};
console.log(JSON.stringify(result, null, 2));
if (process.argv.includes('--write')) {
  const target = resolve(directory, '../results/asset-sizes.json');
  mkdirSync(dirname(target), {recursive: true});
  writeFileSync(target, `${JSON.stringify(result, null, 2)}\n`);
}

function collect(relativeDirectory, extensions) {
  const absoluteDirectory = resolve(repositoryRoot, relativeDirectory);
  const results = [];
  function walk(current) {
    for (const entry of readdirSync(current, {withFileTypes: true})) {
      const absolute = resolve(current, entry.name);
      if (entry.isDirectory()) walk(absolute);
      else if (extensions.has(extname(entry.name))) results.push(absolute.slice(repositoryRoot.length + 1));
    }
  }
  walk(absoluteDirectory);
  return results.sort();
}

function measure(files) {
  let rawBytes = 0;
  let gzipBytes = 0;
  for (const file of files) {
    const bytes = readFileSync(resolve(repositoryRoot, file));
    rawBytes += bytes.length;
    gzipBytes += gzipSync(bytes, {level: 9}).length;
  }
  return files.length <= 50
    ? {fileCount: files.length, rawBytes, gzipBytes, files}
    : {fileCount: files.length, rawBytes, gzipBytes, sampleFiles: files.slice(0, 20)};
}
