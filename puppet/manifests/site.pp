import "nodes"
import "defines"

filebucket { main: server => "ec2-176-34-206-168.eu-west-1.compute.amazonaws.com" }

# global defaults
File { backup => main }
Exec { path => "/usr/bin:/usr/sbin/:/bin:/sbin" }

