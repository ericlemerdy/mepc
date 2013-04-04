class filer {
  exec {'apt-get update':
    command => '/usr/bin/apt-get update',
  }

  package {'nginx':
    ensure => installed,
    require => Exec['apt-get update'],
  }

  file {'/etc/nginx/sites-enabled/filer':
    ensure => present,
    content => 'server {
                  server_name filer;
                  location / {
                    root /vagrant/files;
                    autoindex on;
                  }
                }',
    require => Package['nginx'],
  }

  service {'nginx':
    ensure => running,
    enable => true,
    hasstatus => true,
    hasrestart => true,
    subscribe => File['/etc/nginx/sites-enabled/filer'],
    require => Package['nginx'],
  }
}

include filer
