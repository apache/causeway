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
    o.int '-p', '--port', 'port (default: 4000)', default: 4000
    o.bool '-x', '--suppress', 'suppress monitoring'
    o.bool '-b', '--browser', 'launch browser'
    o.bool '-h', '--help', 'help'
end

if opts.help? then
    puts opts
    exit
end

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
                regenerate = srcSplit[1]
                regenerate += ".adoc" unless regenerate.end_with? ".adoc"
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
                    # wait 1 further second for any additional edits
                    sleep 1
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


if not opts.suppress? then

    i=0
    lastTimeGenerated = Time.now - 10

    puts ""
    puts ""
    puts ""
    puts "monitoring..."
    puts ""
    
    
    #
    # then continue monitoring all directories
    #
    adocFiles = Dir.glob("src/main/asciidoc/**/*.adoc")
    directories = adocFiles.each{ |f| File.new(f) }.uniq{ |f| File.dirname(f) }.map{ |f| File.dirname(f) }
    
    puts "listening to: #{directories}"
    
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
end
    
httpServer = HTTPServer.new(
    :Port => port,
    :DocumentRoot => 'target/site',
    #Logger: WEBrick::Log.new("/dev/null"),
    AccessLog: [],
    )
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



