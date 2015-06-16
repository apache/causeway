require 'rubygems'
require 'pathname'
require 'fileutils'
require 'bundler/setup'
require "slop"
require 'launchy'
require 'webrick'

Bundler.require(:default)

include WEBrick

# to (try to) suppress some debugs
$CELLULOID_DEBUG=false
$CELLULOID_TEST=false



adocFiles = Dir.glob("src/main/asciidoc/**/*.adoc")

files = adocFiles.each{ |f| File.new(f) }.uniq{ |f| File.dirname(f) }.map{ |f| File.dirname(f) }

puts "listening to: #{files}"

