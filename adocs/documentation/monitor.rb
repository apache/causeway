#!/c/bin/Ruby200-x64/bin/ruby -w

require 'rubygems'
require 'pathname'
require 'fileutils'
require 'bundler/setup'

#require 'launchy'

require 'webrick'

#include Launchy
include WEBrick


Bundler.require(:default)



#prime = false


# to suppress some debugs
$CELLULOID_DEBUG=false
$CELLULOID_TEST=false

#scriptDir = File.absolute_path File.dirname(__FILE__)
templateDir = File.absolute_path '../template'
srcBaseDir = File.absolute_path 'src/main/asciidoc'
targetBaseDir = File.absolute_path 'target/site'

srcBasePath = Pathname.new srcBaseDir
targetBasePath = Pathname.new targetBaseDir

i=0

def process(file,srcBasePath,targetBasePath,templateDir,i,priming)

    workingDir = Dir.pwd

    srcDir = File.dirname file
    srcBase = File.basename file
    ext = File.extname file

    srcPath = Pathname.new srcDir
    srcRel = srcPath.relative_path_from srcBasePath

    targetRelPath = targetBasePath + srcRel
    targetRelDir = File.absolute_path targetRelPath.to_s

    Dir.chdir srcDir
    FileUtils.mkdir_p targetRelDir

    if ext == ".adoc" then

        srcSplit = srcBase.split('_')
        if srcSplit[0].length==0 then
            # handle include files of form
            # _xxx-xxx_yyy-yyy_zzz  => xxx-xxx.adoc
            if priming then
                regenerate = ""
            else
                regenerate = srcSplit[1] + ".adoc"
            end
        else
            regenerate = srcBase
        end

        unless regenerate == "" then

            cmd = "asciidoctor #{regenerate} --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{targetRelDir}' -a imagesdir='' -a toc=right -a icons=font -a source-highlighter=coderay"

            #unless priming then
                puts ""
                puts "#{i}: #{cmd}"
            #end

            system cmd
        end

    else

        unless File.directory?(srcBase) then

            cmd = "cp #{srcBase} #{targetRelDir}"

            #unless priming then
                puts ""
                puts "#{i}: #{cmd}"
            #end

            system cmd

        end

    end

    Dir.chdir workingDir

    return i+1

end


puts ""
puts ""
puts ""
puts "priming (processing all files)..."
puts ""


#
# priming: process all files
#
files = Dir.glob("src/main/asciidoc/**/*")
#if prime then
    files.each { |file|
        absFile = File.absolute_path file
        i = process absFile, srcBasePath, targetBasePath, templateDir, i, true
    }
#end


puts ""
puts ""
puts ""
puts "now monitoring..."
puts ""


#
# then continue monitoring all directories
#
directories = Dir.glob("src/main/asciidoc/**/*/")
listener = Listen.to(directories) do |modified, added, removed|
    unless modified.length==0
        modified.each { |file|
            i = process file, srcBasePath, targetBasePath, templateDir, i, false
        }
    end
    unless added.length==0
        added.each { |file|
            i = process file, srcBasePath, targetBasePath, templateDir, i, false
        }
    end
    unless removed.length==0
        removed.each { |file|
            #puts "removed #{file}"
        }
    end
end
listener.start

s = HTTPServer.new(:Port => 4000,  :DocumentRoot => 'target/site')
trap("INT"){
    s.shutdown
    listener.stop
}
s.start


#puts ""
#puts "opening web browser @ http://localhost:4000/"
#puts
#Launchy.open("http://localhost:4000")

