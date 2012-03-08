#Script to generate result(s) of a given model(s) (Ruby)
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
		#revert changes in original file
		if system("git checkout -- \"#{filename}.uml\"")
			warn "Git checkout #{filename}.uml sucessful"
		end
		#add new file to git
		if system("git add \"#{RESULTPATH}#{filename}.uml\"")
			warn "Git added #{RESULTPATH}#{filename}.uml sucessfuly"
		end
	end
	if File.exist? "#{filename}.umldi"
		FileUtils.cp "#{filename}.umldi", "#{RESULTPATH}#{filename}.umldi"
		warn "#{RESULTPATH}#{filename}.umldi was copied"
		#revert changes in original file
		if system("git checkout -- \"#{filename}.umldi\"")
			warn "Git checkout #{filename}.umldi sucessful"
		end
		#add new file to git
		if system("git add \"#{RESULTPATH}#{filename}.umldi\"")
			warn "Git added #{RESULTPATH}#{filename}.umldi sucessfuly"
		end
	end
end
