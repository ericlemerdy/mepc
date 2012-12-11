node front {
	class {'nginx':}
	nginx::resource::vhost { 'localhost':
        	ensure   => present,
        	www_root => '/var/www',
	}
}

node app {
}

node cache {
}

node /(int)?legacy-db/ {
	include mysql::server
}

node db {
	class {'mongodb':
		enable_10gen => true,
	}
}

node monitor {
}

node puppet-master {
}
