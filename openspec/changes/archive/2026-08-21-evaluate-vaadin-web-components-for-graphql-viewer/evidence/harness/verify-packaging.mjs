/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {statSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {spawnSync} from 'node:child_process';

const directory = fileURLToPath(new URL('.', import.meta.url));
const jar = resolve(directory, '..', 'packaging', 'target', 'causeway-vaadin-web-component-analysis-assets-1.0.0-analysis.jar');
const listed = spawnSync('jar', ['tf', jar], {encoding: 'utf8'});
if (listed.status) throw new Error(listed.stderr || listed.stdout);
const entries = listed.stdout.trim().split('\n');
const count = prefix => entries.filter(entry => entry.startsWith(prefix) && !entry.endsWith('/')).length;
const result = {
  generatedAt: new Date().toISOString(),
  jar: jar.slice(resolve(directory, '..').length + 1),
  jarBytes: statSync(jar).size,
  entryCount: entries.length,
  broadAssets: count('META-INF/resources/causeway-vaadin-analysis/vaadin-selective.js'),
  splitAssets: count('META-INF/resources/causeway-vaadin-analysis/split/'),
  directLicenses: count('META-INF/licenses/vaadin-analysis/'),
  runtimeCdnRequired: false
};
if (result.broadAssets !== 1 || result.splitAssets < 3 || result.directLicenses !== 14) throw new Error(`Unexpected JAR contents: ${JSON.stringify(result)}`);
if (process.argv.includes('--write')) writeFileSync(resolve(directory, '..', 'results', 'packaging-verification.json'), `${JSON.stringify(result, null, 2)}\n`);
console.log(JSON.stringify(result, null, 2));
