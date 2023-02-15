connect - transparently use Windows authentification
====================================================

This library provides a single interface that helps to access resources using winauth.

Use any of the `Connection.create(...)` overloads to create a connection. `openInputStream()`then creates an  `InputStream`, transparently using Windows authentification if available. Connection implements the `AutoCloseable` interface; closing the connection also closes the stream.

Changes
-------

### V 2.0.0

 - update dependencies
 - set custom header parameters

### V 1.3.0

 - update dependencies
 - logging is now done through slf4j
