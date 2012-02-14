#Script to generate result of a given model (Ruby)
#Author Piotr J. Puczynski

require 'fileutils'

RESULTPATH = './results/'

ARGV.each do|arg|
	filename = File.basename(arg,File.extname(arg))
	if File.exist? "#{filename}.uml"
		uml = File.open("#{filename}.uml")
		umlResult = File.new("#{RESULTPATH}#{filename}.uml", "w")
		counter = 0
		while (line = uml.gets)
			if (line =~ /<details .* key="topcased-ploted"/)
				counter += 1
			else
				umlResult.puts(line)
			end
		end
		uml.close
		umlResult.close
		warn "Removed #{counter} annotations from #{RESULTPATH}#{filename}.uml"
	end
	if File.exist? "#{filename}.umldi"
		FileUtils.cp "#{filename}.umldi", "#{RESULTPATH}#{filename}.umldi"
		warn "#{RESULTPATH}#{filename}.umldi was copied"
	end
end
