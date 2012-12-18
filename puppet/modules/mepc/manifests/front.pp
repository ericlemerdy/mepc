class mepc::front {
        class {'nginx':}
        nginx::resource::vhost { 'localhost':
                ensure   => present,
                www_root => '/var/www',
                require => File['/var/www'],
                notify => Exec['reload nginx'],
        }
        file {'/var/www':
                ensure  => directory,
                owner   => 'www-data',
                mode    => 755,
        }
        exec {'reload nginx':
                command => '/usr/sbin/service nginx reload',
                unless => 'sudo netstat -tunelp |grep nginx',
                require => Exec[rebuild-nginx-vhosts],
        }
}
