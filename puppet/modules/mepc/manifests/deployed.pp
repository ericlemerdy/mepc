class mepc::deployed {
	file {'/tmp/deployed':
		ensure => present,
	}
}
