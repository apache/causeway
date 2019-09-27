// Example: see https://github.com/mkraynov/kfsad/blob/master/karma.conf.js
// Doc: see http://karma-runner.github.io/4.0/config/configuration-file.html
module.exports = function (config) {
  config.set({
      files: ['./build/**/*.js'],
      excludes: ['./**/*.css'],
      preprocessors: {
//        'build/**/*_test.js': ['webpack']
      },
      frameworks: ['jasmine'],
      plugins: [
        require('karma-chrome-launcher'),
        require('karma-coverage-istanbul-reporter'),
        require('istanbul-instrumenter-loader'),
        require('karma-jasmine'),
        require('karma-jasmine-html-reporter'),
        require('karma-webpack')
      ],
      reporters: ['coverage-istanbul'],
      coverageIstanbulReporter: {
        reports: ['html', 'lcovonly', 'text-summary'],
        // if using webpack and pre-loaders, work around webpack breaking the source path
        fixWebpackSourcePaths: true
      },
      browsers: ['ChromeHeadless'],

//      logLevel: config.LOG_DEBUG,
      autoWatch: true,
      singleRun: true,
      concurrency: Infinity,
      color: false
    }
  )
};
