class nginx {
  package {'nginx':
    ensure => installed,
    require => Exec['apt-get update'],
  }
}
