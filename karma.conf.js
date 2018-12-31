module.exports = function (config) {
  config.set({
    frameworks: ['mocha', 'browserify'],
    reporters: ['mocha'],
    files: [
      'build/node_modules/*.js',
      'build/classes/kotlin/main/*.js',
      'build/classes/kotlin/test/*.js'
    ],
    exclude: [],
    colors: true,
    autoWatch: false,
    browsers: [
//            'ChromeHeadlessNoSandbox'
      'Chrome'
    ],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },
    captureTimeout: 5000,
    singleRun: true,
    reportSlowerThan: 500,
    preprocessors: {
      'build/**/*.js': ['browserify'],
    }
  })
};