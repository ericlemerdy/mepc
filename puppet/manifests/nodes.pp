node front {
  include base
  include nginx
}

node app {
}

node cache {
}

node /(int)?legacy-db/ {
	include mysql::server
}

node db {
}

node monitor {
}

node puppet-master {
}
