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
    command => 'apt-get update',
    refreshonly => true,
  }
}
