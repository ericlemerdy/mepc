class puppet_mepc {
	package {['dnsutils', 'vim-puppet', 'rake']:
		ensure => installed,
		require => Exec['apt-get update'],
	}
}
