#Script to change name of a model (Ruby)
#Author Piotr J. Puczynski

require 'fileutils'

unless ARGV.count == 2
	abort('Usage: changename from to')
end

from = File.basename(ARGV[0], File.extname(ARGV[0]))
to = File.basename(ARGV[1], File.extname(ARGV[1]))

if File.exist? "#{from}.uml"
	FileUtils.cp "#{from}.uml", "#{to}.uml"
	warn "#{to}.uml was copied"
end

if File.exist? "#{from}.umldi"
	umldi = File.open("#{from}.umldi")
	umldiResult = File.new("#{to}.umldi", "w")
	while (line = umldi.gets)
		umldiResult.puts(line.gsub(from, to))
	end
	umldi.close
	umldiResult.close
end