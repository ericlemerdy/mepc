class mepc::front_deploy {
	exec {'extract mepc':
		command => 'tar xzf /tmp/front.tar.gz',
		cwd => '/var/www',
		creates => '/var/www/index/html'
	}
	
	file {'/var/www/conf.js':
		ensure => present,
		content => '{ "dataHost": "app:80" }',
	}
}
