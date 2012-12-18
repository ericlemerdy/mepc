class mepc::lxc_host {
	file {'/etc/init.d/lxcrest':
		ensure 	=> present,
		owner 	=> 'root',
		group	=> 'root',
		mode	=> 0744,
		source	=> 'puppet:///modules/lxc_host_mepc/lxcrest',
	}

	package {'maven':
		ensure => installed,
	}

	package {'openjdk-7-jdk':
		ensure => installed,
		notify => Exec['update java-7'],
	}

	exec {'update java-7':
		command => '/usr/sbin/update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java',
		refreshonly => true,
	}

	package {'telnet':
		ensure => installed,
	}
	
	include foreman
}
