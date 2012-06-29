#!/bin/bash

if [[ -d cookbooks ]] ;
then
	echo cookbooks already exist
else
	mkdir --verbose cookbooks/
	cd cookbooks/
	git clone https://github.com/opscode-cookbooks/apt.git
	git clone https://github.com/opscode-cookbooks/ark.git
	git clone https://github.com/opscode-cookbooks/git.git
	git clone https://github.com/opscode-cookbooks/java.git
	mkdir --verbose jenkins/
	cd jenkins
	wget https://gist.github.com/gists/978920/download --output-document=978920.tgz
	tar --verbose --extract --file=978920.tgz
	mv --verbose gist* recipes
	rm --verbose 978920.tgz
	cd ../
	git clone https://github.com/opscode-cookbooks/maven.git
	cd ../
fi
