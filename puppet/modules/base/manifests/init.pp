class base {
  stage {'before':}
  stage {'after':}

  class {'base::apt':
  	stage => before,
  }

  Stage['before'] -> Stage['main'] -> Stage['after']

  user {'mepc':
    ensure => present,
    uid => 1100,
    gid => 1100,
  }

  group {'mepc':
    ensure => present,
    gid => 1100,
  }

  package {['curl', 'htop', 'zsh']:
	ensure => installed,
#	require => Exec['apt-get update'],
  }

  package {'language-pack-en':
	ensure => installed,
#	require => Exec['apt-get update'],
	notify => Exec['update-locale'],
  }

  file {'/etc/default/locale':
	ensure => present,
	content => 'LANG=en-US.UTF-8',
  }

  exec {'update-locale':
	refreshonly => true,
	path => '/usr/sbin',
  }
}
