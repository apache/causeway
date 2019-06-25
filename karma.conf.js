module.exports = function (config) {
  config.set({
 //   basePath: '.',
    frameworks: ['mocha', 'browserify'],
    reporters: ['mocha'],
    plugins: [
      'karma-browserify',
      'karma-mocha',
      'karma-mocha-reporter',
      'karma-chrome-launcher'],
    files: [
      'build/node_modules/*.js',
      'build/classes/kotlin/main/*.js',
      'build/classes/kotlin/test/*.js'
    ],
    exclude: [],
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false,
    concurrency: Infinity,
    preprocessors: {
      'build/**/*.js': ['browserify'],
    }
  })
}
