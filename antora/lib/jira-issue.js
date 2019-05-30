module.exports = function (registry) {
  registry.blockMacro(function () {
    var self = this
    self.named('jira-issue')
    self.process(function (parent, target, attrs) {
      var result = `link:https://issues.apache.org/jira/browse/${target}[${target}]`
      return self.createBlock(parent, 'paragraph', result)
    })
  })
}
