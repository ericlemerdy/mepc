class mepc::app_deploy {
	file {'/opt/mepc':
		ensure => directory,
		mode => 0755,
		notify => Exec['extract mepc']
	}

	exec {'extract mepc':
		command => 'tar xzf /tmp/mepc.tar.gz',
		cwd => '/opt/mepc',
		refreshonly => true,
		notify => Exec['install service'],
		require => File['/opt/mepc'],
	}
	
	exec {'install service':
		command => '/usr/local/bin/foreman export upstart /etc/init -a mepc -u root',	
		cwd => '/opt/mepc',
		environment => 'HOME=/tmp',
		refreshonly => true,
		require => Exec['extract mepc'],
	}

	service {'mepc':
		ensure => running,
		enable => true,
		require => Exec['install service'],
	}
}
