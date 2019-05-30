// as per https://gitlab.com/antora/antora/issues/246
module.exports = function () {
  this.includeProcessor(function () {
    this.$option('position', '>>')
    this.handles((target) => target.startsWith('http://'))
    this.process((doc, reader, target, attrs) => {
      const contents = require('child_process')
        .execFileSync('curl',
          ['-H', 'Authorization: Basic c2RtYnVpbGQxOmJ1aWxkaW5nMQ==', '-L', target],
          { encoding: 'utf8' })
      //      const contents = require('child_process').execFileSync('curl',
      //['--user', 'sdmbuild1', 'building1', '--silent', '-L', target], { encoding: 'utf8' })
      //
      reader.pushInclude(contents, target, target, 1, attrs)
    })
  })
}
