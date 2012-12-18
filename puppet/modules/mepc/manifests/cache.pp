class mepc::cache ($env) {
        package {'varnish':
                ensure => installed,
                require => Exec['apt-get update'],
        }

	service {'varnish':
		ensure => running,
		enable => true,
	}

	file {'/etc/default/varnish':
		ensure => present,
		source => 'puppet:///mepc/varnish/default',
		notify => Service['varnish'],
		require => Package['varnish'],
	}

	if $env == 'int' {
		file {'/etc/varnish/mepc.vcl':
			ensure => present,
			source => 'puppet:///mepc/varnish/int.vcl',
			notify => Service['varnish'],
			require => Package['varnish'],
		}
	} else {
		file {'/etc/varnish/blue.vcl':
			ensure => present,
			source => 'puppet:///mepc/varnish/blue.vcl',
			notify => Service['varnish'],
			require => Package['varnish'],
		}
		file {'/etc/varnish/green.vcl':
			ensure => present,
			source => 'puppet:///mepc/varnish/green.vcl',
			notify => Service['varnish'],
			require => Package['varnish'],
		}
		exec {'link conf':
			command => '/bin/ln -s blue.vcl mepc.vcl',
			cwd => '/etc/varnish',
			creates => '/etc/varnish/mepc.vcl',
			require => File['/etc/varnish/blue.vcl'],
		}
	}
}
