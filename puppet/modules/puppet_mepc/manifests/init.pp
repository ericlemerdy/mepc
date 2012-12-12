class puppet_mepc {
	package {['dnsutils', 'vim-puppet']:
		ensure => installed,
		require => Exec['apt-get update'],
	}
}
