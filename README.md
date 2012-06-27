mepc
====

Mise En Production Continue

Content
=======

- mepc-jenkins

  The vagrant machine defintion to host the Jenkins continuous build engine. The goal of this machine is to build the project and deploy it.

- mepc-elb

  The vagrant machine defintion to host the Varnish reverse proxy. The goal of this machine is to route requests between production and pre-production environment.
