#!/bin/sh

if [ ! -f /usr/share/dia/sheets/network_fi.sheet ]
then
	echo 'Downloading additional icons according to http://blog.admin-linux.org/logiciels-libres/schema-architecture-reseaux-sous-gnu-linux...'
	wget http://blog.admin-linux.org/wp-content/uploads/2010/04/dia_network_by_fi.zip
	echo 'Installing additional icons...'
	sudo unzip dia_network_by_fi.zip -d /usr/share/
	rm dia_network_by_fi.zip
fi
	
echo 'Exporting network diagram...'
dia --export=assets/network.png --filter=png --verbose src/network.dia
