class mepc::app {
        package {'openjdk-7-jre': 
                ensure => installed, 
                require => Exec['apt-get update'], 
        } 

	include foreman
}
