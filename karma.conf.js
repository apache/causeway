// see https://github.com/mkraynov/kfsad/blob/master/karma.conf.js
module.exports = function (config) {
  config.set({
    basePath: '',
    // all plugins have to be installed before via
    // npm install <plugin> --save-dev
    plugins: [
      'karma-webpack',
      'karma-browserify',
      'karma-jasmine',
      'karma-mocha',
      'karma-mocha-reporter',
      'karma-chrome-launcher',
      'karma-phantomjs-launcher',
      'karma-coverage'],
    frameworks: ['mocha'],
    files: [
      'build/js-tests/kroviz-tests.js'
    ],
    exclude: [],
    preprocessors: {
      'build/**/*_test.js': ['webpack']
    },
//    webpack: require('./webpack.karma'),
    webpack: {
      entry: './build/js-tests/kroviz-tests.js',
      output: {
        filename: 'bundle.js'
      }
    },
    webpackMiddleware: {
      noInfo: true
    },
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
      // https://github.com/karma-runner/karma-chrome-launcher/issues/158#issuecomment-339265457
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },
    singleRun: true,
    concurrency: Infinity,
    client: {
      mocha: {
        timeout: 6000
      }
    }

   /* plugins: [
      'karma-browserify',
      'karma-mocha',
      'karma-mocha-reporter',
      'karma-chrome-launcher',
      'karma-phantomjs-launcher',
      'karma-coverage'],
    browserify: ({
      entries: ['./build/js/kroviz.js'], */
//      noParse: ['./node_modules_imported/**/*js']
//    }),
  })
};
