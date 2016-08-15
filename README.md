# spring-sample-app-server-auth
Simple example of user authorization via application server in Spring.
You can add application user and authorized with it's credentials. Or you can use inner application user ``inner:password``.

- ``/``       - page with text; will only be available for authorized user
- ``/login``  - opens login page
- ``/logout`` - logout authorized user

Tested on [Wildfly 10.0](http://wildfly.org/).
