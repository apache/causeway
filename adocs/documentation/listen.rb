#!/c/bin/Ruby200-x64/bin/ruby -w

require 'rubygems'
require 'pathname'
require 'fileutils'
require 'bundler/setup'

Bundler.require(:default)

# uses: https://github.com/guard/listen

# to suppress some debugs
$CELLULOID_DEBUG=false
$CELLULOID_TEST=false

#scriptDir = File.absolute_path File.dirname(__FILE__)
templateDir = File.absolute_path '../template'
srcBaseDir = File.absolute_path 'src/main/asciidoc'
targetBaseDir = File.absolute_path 'target/site'

srcBasePath = Pathname.new srcBaseDir
targetBasePath = Pathname.new targetBaseDir

puts ""
puts ""
puts ""
puts "now monitoring..."
puts ""

i=0

def process(file,srcBasePath,targetBasePath,templateDir,i)

    workingDir = Dir.pwd

    srcDir = File.dirname file
    srcBase = File.basename file

    srcSplit = srcBase.split('_')
    if srcSplit[0].length==0 then
        # handle include files of form
        # _xxx-xxx_yyy-yyy_zzz  => xxx-xxx.adoc
        regenerate = srcSplit[1] + ".adoc"
    else
        regenerate = srcBase
    end

    srcPath = Pathname.new srcDir
    srcRel = srcPath.relative_path_from srcBasePath

    targetRelPath = targetBasePath + srcRel
    targetRelDir = File.absolute_path targetRelPath.to_s

    Dir.chdir srcDir
    FileUtils.mkdir_p targetRelDir

    cmd = "asciidoctor #{regenerate} --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{targetRelDir}' -a imagesdir='' -a toc=right -a icons=font -a source-highlighter=coderay"

    puts ""
    puts "#{i}: #{cmd}"

    system cmd

    Dir.chdir workingDir

    return i+1
end

listener = Listen.to('src/main/asciidoc/user-guide') do |modified, added, removed|
    unless modified.length==0
        modified.each { |file|
            i = process file, srcBasePath, targetBasePath, templateDir, i
        }
    end
    unless added.length==0
        added.each { |file|
            i = process file, srcBasePath, targetBasePath, templateDir, i
        }
    end
    unless removed.length==0
        removed.each { |file|
            puts "removed #{file}"
      
        }
    end
end
listener.start
listener.only(/.*\.adoc$/)
sleep
