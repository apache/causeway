// Example: see https://github.com/mkraynov/kfsad/blob/master/karma.conf.js
// Doc: see http://karma-runner.github.io/4.0/config/configuration-file.html
module.exports = function (config) {
  config.set({
      basePath: '',
      frameworks: ['jasmine'],
      files: ['./build/**/*.js'],
      excludes: ['./**/*.css'],
      preprocessors: {
          './build/js-tests/*-tests.js': ['coverage']
      },
      plugins: [
        require('karma-jasmine'),
        require('karma-coverage'),
//        require('karma-phantomjs-launcher'),
        require('karma-chrome-launcher'),
        require('karma-webpack')
      ],
      reporters: ['progress', 'coverage'],
      port: 9876,
      colors: true,
      logLevel: config.LOG_INFO,
      autowatch: true,
      browsers: ['ChromeHeadless'],
      singleRun: true,
      concurrency: Infinity,
      coverageReporter: {
        includeAllSources: true,
        dir: 'coverage/',
        reporters: [
          {type: "html", subdir: "html"},
 //         {type: "lcov"},
          {type: 'text-summary'}
        ]

      }
    }
  )
};
