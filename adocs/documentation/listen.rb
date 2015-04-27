#!/c/bin/Ruby200-x64/bin/ruby -w

require 'rubygems'
require 'pathname'
require 'fileutils'
require 'bundler/setup'

Bundler.require(:default)


#https://github.com/guard/listen

$CELLULOID_DEBUG=false
$CELLULOID_TEST=false

#
#guard 'shell' do
#  watch(/^.*\.adoc$/) {|m|
#    #Asciidoctor.convert_file(m[0], :in_place => true)
#    puts "Hello, Ruby!";
#  }
#end

scriptDir = File.absolute_path File.dirname(__FILE__)
templateDir = File.absolute_path '../template'
targetDir = File.absolute_path 'target/site'
workingDir = Dir.pwd

templateDirPath = Pathname.new templateDir
targetDirPath = Pathname.new targetDir
workingDirPath = Pathname.new workingDir

listener = Listen.to('src/foo', 'src/bar') do |modified, added, removed|
  unless modified.length==0
    modified.each { |file|

      dir = File.dirname file 
      base = File.basename file

      #puts "modified #{file}"
      #puts "dir: #{dir}"
      #puts "base: #{base}"

      dirPath = Pathname.new dir

      srcRel = dirPath.relative_path_from workingDirPath
      #templateRel = dirPath.relative_path_from templateDirPath

      targetRelPath = targetDirPath + srcRel
      destinationDir = File.absolute_path targetRelPath.to_s

      Dir.chdir(dir)

      #puts "dir: #{dir}"
      #puts "file: #{file}"
      #puts "templateDir: #{templateDir}"
      #puts "destinationDir: #{destinationDir}"

      FileUtils.mkdir_p destinationDir

      #attributes = {:imagesdir => '', :toc => 'right', :icons => 'font', :source-highlighter => 'coderay'}

      #Asciidoctor.convert_file(file, :in_place => true, :backend => 'html', :eruby => 'erb', :template_dir => templateDir, :destination_dir => targetRelDir, :attributes => attributes)

      #Asciidoctor.convert_file(file, :in_place => true, :backend => 'html', :eruby => 'erb', :template_dir => templateDir, :destination_dir => targetRelDir)

      cmd = "asciidoctor #{file} --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{destinationDir}' -a imagesdir='' -a toc=right -a icons=font -a source-highlighter=coderay"

      puts ""
      puts cmd

#Asciidoctor.convert_file file, :in_place => true, :backend => 'html', :eruby => 'erb', :template_dir => templateDir, :destination_dir => targetRelDir 

      system cmd

      Dir.chdir(workingDir)
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
