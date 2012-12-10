class dashku {
  package {'mongodb':
    ensure => installed,
  }

  package {'redis':
    ensure => installed,
  }

  package {'npm':
    ensure => installed
  }

  exec {'clone dashku':
    command => 'git clone https://github.com/Anephenix/dashku.git',
    cwd => '/opt',
    creates => '/opt/dashku',
    require => [Package['mongodb'],
                Package['redis'],
                Package['nmp']],
    notify => Exec['dashku install'],
  }

  exec {'dashku install':
    command => 'npm install',
    cwd => '/opt/dashku',
    refreshonly => true,
    require => Exec['clone daskhu'],
  }

  file {'/opt/dashku/installed':
    ensure => present,
    require => Exec['dashku install'],
  }

  service {'dashku':
    provider => base,
    ensure => running,
    start => '/opt/dashku/node_modules/.bin/coffee app.coffee',
    stop => 'killall coffee',
    restart => 'killall coffee && /opt/dashku/node_modules/.bin/coffee app.coffee',
    status => 'ps ax |grep coffee |grep -v grep',
    require => File['/opt/dashku/installed'],
  }
}
