#! /bin/sh
echo "Exporting network diagram..."
dia --export=assets/network.png --filter=png --verbose src/network.dia
