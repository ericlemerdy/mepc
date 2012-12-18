class foreman {
        package {'rubygems': 
                ensure => installed, 
                require => Exec['apt-get update'], 
        } 
        package {'foreman': 
                ensure => installed, 
                provider => 'gem', 
                require => Package['rubygems'], 
        }
}
