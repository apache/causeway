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



#
# parse cmd line args
#
opts = Slop.parse do |o|
  o.bool '-a', '--all', 'process all files'
  o.bool '-x', '--nomonitor', 'do not monitor, just process all files then exit'
  o.int '-p', '--port', 'port (default: 4000)', default: 4000
  o.bool '-b', '--browser', 'launch browser'
  o.bool '-h', '--help', 'help'
end

if opts.help? then
    puts opts
    exit
end

processAll = opts.all? || opts.nomonitor?
port = opts[:port]


#scriptDir = File.absolute_path File.dirname(__FILE__)
templateDir = File.absolute_path '../template'
srcBaseDir = File.absolute_path 'src/main/asciidoc'
targetBaseDir = File.absolute_path 'target/site'

srcBasePath = Pathname.new srcBaseDir
targetBasePath = Pathname.new targetBaseDir


def process(file,srcBasePath,targetBasePath,templateDir,i,lastTimeGenerated,priming)

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

	    # if regenerated within last 3 seconds, then wait a while
	    currentTime = Time.now
	    timeSinceLast = currentTime.to_i - lastTimeGenerated.to_i
	    timeUntilNext = 3 - timeSinceLast
	    if not priming and
	       timeUntilNext > 0 then
	        puts "skipping before regenerating (3 seconds not yet elapsed)"
	    else
                cmd = "asciidoctor #{regenerate} --require asciidoctor-diagram --backend html --eruby erb --template-dir '#{templateDir}' --destination-dir='#{targetRelDir}' -a imagesdir='' -a toc=right -a icons=font -a source-highlighter=coderay"

                unless priming then
                    puts ""
                    puts "#{i}: #{cmd}"
                end

                system cmd

                lastTimeGenerated=Time.now
	    end
        end
    else

        unless File.directory?(srcBase) then

            cmd = "cp #{srcBase} #{targetRelDir}"

            unless priming then
                puts ""
                puts "#{i}: #{cmd}"
            end

            system cmd

        end

    end

    Dir.chdir workingDir

    return i+1, lastTimeGenerated

end


i=0
lastTimeGenerated = Time.now - 10


#
# process all files
#
if processAll then

    files = Dir.glob("src/main/asciidoc/**/*")

    puts ""
    puts ""
    puts ""
    puts "processing all files..."
    puts ""

    files.each { |file|
        absFile = File.absolute_path file
        i,lastTimeGenerated = process absFile, srcBasePath, targetBasePath, templateDir, i, lastTimeGenerated, true
    }
end



if opts.nomonitor? then
    exit
end

puts ""
puts ""
puts ""
puts "monitoring..."
puts ""


#
# then continue monitoring all directories
#
directories = Dir.glob("src/main/asciidoc/**/*/")
fileListener = Listen.to(directories) do |modified, added, removed|
    unless modified.length==0
        modified.each { |file|
            i,lastTimeGenerated = process file, srcBasePath, targetBasePath, templateDir, i, lastTimeGenerated, false
        }
    end
    unless added.length==0
        added.each { |file|
            i,lastTimeGenerated = process file, srcBasePath, targetBasePath, templateDir, i, lastTimeGenerated, false
        }
    end
    unless removed.length==0
        removed.each { |file|
            #puts "removed #{file}"
        }
    end
end
fileListener.start

httpServer = HTTPServer.new(:Port => port,  :DocumentRoot => 'target/site')
trap("INT"){
    httpServer.shutdown
    fileListener.stop
}



if opts.browser? then

    puts ""
    puts "opening web browser @ http://localhost:#{port}/"
    puts

    Launchy.open("http://localhost:#{port}")
else
    puts ""
    puts "open web browser @ http://localhost:#{port}/"
    puts

end



httpServer.start



