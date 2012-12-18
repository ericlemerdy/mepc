#!/bin/sh

echo 'Packaging front assets...'
tar --directory=../front -czvf ../front.tar.gz .
