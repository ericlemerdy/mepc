class dashku {
  package {'mongodb':
    ensure => installed,
  }

  package {'redis-server':
    ensure => installed,
  }

  package {'npm':
    ensure => installed,
    require => [Exec['apt-get update'],
                Exec['add node ppa']],
  }

  package {'git':
    ensure => installed,
  }

  package {'build-essential':
    ensure => installed,
  }

  package {'python-software-properties':
    ensure => installed,
    notify => Exec['add node ppa'],
  }

  package {'psmisc':
    ensure => installed,
  }

  exec {'add node ppa':
    command => '/usr/bin/add-apt-repository ppa:chris-lea/node.js',
    refreshonly => true,
    notify => Exec['apt-get update'],
  }

  exec {'clone dashku':
    command => 'git clone https://github.com/Anephenix/dashku.git',
    cwd => '/opt',
    creates => '/opt/dashku',
    require => [Package['mongodb'],
                Package['redis-server'],
                Package['npm'],
		Package['git']],
    notify => Exec['dashku install'],
  }

  exec {'dashku install':
    command => 'npm install',
    cwd => '/opt/dashku',
    refreshonly => true,
    require => Exec['clone dashku'],
  }

  file {'/opt/dashku/installed':
    ensure => present,
    require => Exec['dashku install'],
  }

  service {'dashku':
    provider => base,
    ensure => running,
    start => 'cd /opt/dashku && ./node_modules/.bin/coffee app.coffee &',
    stop => 'killall coffee',
    restart => 'killall coffee && cd /opt/dashku && ./node_modules/.bin/coffee app.coffee &',
    status => 'ps ax |grep coffee |grep -v grep',
    require => [File['/opt/dashku/installed'],
		Package['psmisc']],
  }
}
