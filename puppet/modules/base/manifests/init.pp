class base {
  user {'mepc':
    ensure => present,
    uid => 1100,
    gid => 1100,
  }

  group {'mepc':
    ensure => present,
    gid => 1100,
  }

  exec {'apt-get update':
	command => '/usr/bin/apt-get update',
	refreshonly => true,
  }

  package {['curl', 'htop', 'zsh']:
	ensure => installed,
	require => Exec['apt-get update'],
  }
}
