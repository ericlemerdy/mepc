node /(int|blue|green)?front[0-9]{14}/ {
	include base
	include mepc::front
	class {'mepc::front_deploy':
		stage => after,
	}
	class {'mepc::deployed':
		stage => after,
	}
}

node /(int|blue|green)?app[0-9]{14}/ {
	include base
	include mepc::app
	class {'mepc::app_deploy':
		stage => after,
	}
	class {'mepc::deployed':
		stage => after,
	}
}

node /(int|blue|green)?legacydb[0-9]{14}/ {
	include base
	class {'mysql::server':
		require => Exec['apt-get update'],
	}
}

node /^(int|blue|green)?db[0-9]{14}/ {
	include base
	class {'mongodb':
		enable_10gen => true,
	}
}

node /intcache/ {
	include base
	class {'mepc::cache':
		env => 'int',
	}
}

node /cache/ {
	include base
	class {'mepc::cache':
		env => 'prod',
	}
}

node monitor {
	include base
	include dashku
}

node puppet {
	include base
	include puppet_mepc
}

node lxc-host {
	include base
	include mepc::lxc_host
}
