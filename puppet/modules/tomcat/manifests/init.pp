class tomcat {
  package {'tomcat7':
    ensure => installed,
    require => Exec['apt-get update'],
  }
}
