#!/c/bin/Ruby200-x64/bin/ruby -w

require 'rubygems'
require 'pathname'
require 'fileutils'
require 'bundler/setup'

Bundler.require(:default)

# https://github.com/guard/listen


# to suppress
$CELLULOID_DEBUG=false
$CELLULOID_TEST=false


#scriptDir = File.absolute_path File.dirname(__FILE__)
templateDir = File.absolute_path '../template'
srcBaseDir = File.absolute_path 'src/main/asciidoc'
targetBaseDir = File.absolute_path 'target/site'
workingDir = Dir.pwd

#templatePath = Pathname.new templateDir
srcBasePath = Pathname.new srcBaseDir
targetBasePath = Pathname.new targetBaseDir
workingPath = Pathname.new workingDir

#puts "workingDir: #{workingDir}"
#puts "workingPath: #{workingPath}"

#puts "srcBaseDir: #{srcBaseDir}"
#puts "srcBasePath: #{srcBasePath}"

#puts "targetBaseDir: #{targetBaseDir}"
#puts "targetBasePath: #{targetBasePath}"

puts ""
puts ""
puts ""
puts "now monitoring..."
puts ""

def process(file)
    puts "regenerating #{file}..."
end

i=0
listener = Listen.to('src/main/asciidoc/user-guide') do |modified, added, removed|
  unless modified.length==0
    modified.each { |file|

      srcDir = File.dirname file
      srcBase = File.basename file

      srcSplit = srcBase.split('_')
      if srcSplit[0].length==0 then
          regenerate = srcSplit[1] + ".adoc"
      else
          regenerate = srcBase
      end

      srcPath = Pathname.new srcDir
      srcRel = srcPath.relative_path_from srcBasePath
      targetRelPath = targetBasePath + srcRel
      targetRelDir = File.absolute_path targetRelPath.to_s

      #puts "regenerate: #{regenerate}"
      #puts "targetRelPath: #{targetRelPath}"
      #puts "targetRelDir: #{targetRelDir}"

      process regenerate
      Dir.chdir srcDir
      FileUtils.mkdir_p targetRelDir

      #cmd = "asciidoctor #{regenerate} --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{targetRelDir}' -a imagesdir='' -a toc=right -a icons=font -a source-highlighter=coderay"
      cmd = "asciidoctor #{regenerate} --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{targetRelDir}' -a imagesdir='' -a toc=right -a icons=font"

      i=i+1
      puts ""
      puts "#{i}: #{cmd}"
      system cmd

      Dir.chdir workingDir
    }
  end
  unless added.length==0
    puts "added absolute path: #{added}"
    added.each { |file|
      puts "added #{file}"
      
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
