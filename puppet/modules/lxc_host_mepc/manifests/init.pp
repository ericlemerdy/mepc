class lxc_host_mepc {
	file {'/etc/init.d/lxcrest':
		ensure 	=> present,
		owner 	=> 'root',
		group	=> 'root',
		mode	=> 0744,
		source	=> 'puppet:///modules/lxc_host_mepc/lxcrest',
	}
}
