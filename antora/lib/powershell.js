const { spawn } = require('child_process')
const { spawnSync } = require('child_process');
const { execSync } = require('child_process');

module.exports = function (registry) {

  registry.blockMacro(function () {
    var self = this
    self.named('powershell')
    self.process(function (parent, target, attrs) {
      var stdoutLines = execSync(`powershell -nologo -noprofile ${target}`, {stdio: ['ignore','pipe','pipe']}).toString()
      var blockType = attrs.block || 'paragraph'
      return self.createBlock(parent, blockType, stdoutLines)
    })
  })

  registry.block(function () {
    var self = this
    self.named('powershell')
    self.$content_model('raw')
    self.onContexts(['literal', 'paragraph'])
    self.positionalAttributes(['type'])
    self.process(function (parent, reader, attrs) {
      var lines = reader.$read_lines()
      var stdoutLines = lines.map( r => execSync(`powershell -nologo -noprofile ${r}`, {stdio: ['ignore','pipe','pipe']})).map(p => p.toString()).join("\n")

      var blockType = attrs.block || 'pass'
      return self.createBlock(parent, blockType, stdoutLines, {})
    })
  })
}
