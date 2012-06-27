#!/bin/bash

if [[ -d cookbooks ]] ;
then
	echo cookbooks already exist
else
	mkdir --verbose cookbooks/
	cd cookbooks/
	git clone https://github.com/opscode-cookbooks/varnish.git 
	cd ../
fi
