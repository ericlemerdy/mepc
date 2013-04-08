File {
  owner => root,
  group => root,
  mode => 0755,
}

Exec {
  path => '/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin',
}

Package {
  require => Exec['apt-get update'],
}

class mepc {
  exec {'apt-get update':
    refreshonly => true,
    subscribe => File['/opt/mepc'], 
  }

  file {'/opt/mepc':
    ensure => directory,
  }

  file {'/opt/mepc/webapp.py':
    ensure => present,
    source => 'file:///vagrant/webapp.py',
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/deploy_scheduler.py':
    ensure => present,
    source => 'file:///vagrant/deploy_scheduler.py',
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/scheduler.sh':
    ensure => present,
    source => 'file:///vagrant/schedule.sh',
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/backend':
    source => 'file:///vagrant/backend',
    recurse => true,
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/requirements':
    ensure => present,
    mode => 0644,
    source => 'file:///vagrant/requirements',
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/templates':
    source => 'file:///vagrant/templates',
    recurse => true,
    require => File['/opt/mepc'],
  }

  file {'/opt/mepc/static':
    source => 'file:///vagrant/static',
    recurse => true,
    require => File['/opt/mepc'],
  }

  package {'redis-server':
    ensure => installed,
  }

  service {'redis-server':
    ensure => running,
    enable => true,
    hasstatus => true,
    hasrestart => true,
    require => Package['redis-server']
  }

  package {'python-virtualenv':
    ensure => installed,
  }

  exec {'init venv':
    command => 'virtualenv venv',
    cwd => '/opt/mepc',
    refreshonly => true,
    subscribe => Package['python-virtualenv'],
    require => File['/opt/mepc'],
  }

  package {'python-dev':
    ensure => installed,
  }

  exec {'install dependencies':
    command => 'pip install -r requirements --environment /opt/mepc/venv',
    cwd => '/opt/mepc',
    refreshonly => true,
    subscribe => Exec['init venv'],
    require => [File['/opt/mepc/requirements'], Package['python-dev']],
  }

  file {'/var/lib/mepc':
    ensure => directory,
    owner => root,
    group => root,
    mode => 0777,
  }

  file {'/etc/init.d/webapp':
    ensure => present,
    owner => root,
    group => root,
    mode => 0755,
    source => 'file:///vagrant/files/webapp',
  }

  service {'webapp':
    ensure => running,
    enable => true,
    hasstatus => true,
    hasrestart => true,
    notify => Service['scheduler'],
    require => [Exec['install dependencies'], File['/etc/init.d/webapp']],
  }

  file {'/etc/init.d/scheduler':
    ensure => present,
    owner => root,
    group => root,
    mode => 0755,
    source => 'file:///vagrant/files/scheduler',
  }

  service {'scheduler':
    ensure => running,
    enable => true,
    hasstatus => true,
    hasrestart => true,
    require => [Exec['install dependencies'], File['/etc/init.d/scheduler'], Service['webapp']],
  }

  service {['nginx', 'bind9', 'isc-dhcp-server', 'haproxy']:
    enable => false,
    require => [Package['nginx'],
                Package['bind9'],
                Package['dhcp3-server'],
                Package['haproxy']],
  }

  package {['nginx', 'bind9', 'dhcp3-server', 'haproxy', 'git', 'curl', 'maven', 'openjdk-7-jdk']:
    ensure => installed,
  }
}

include mepc
