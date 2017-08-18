# errai-spring-boot-sample

This is a sample project to illustrate integrating Errai with Springboot using the errai-spring-server library <https://github.com/expansel/errai-spring-server>.

The main point of interest is the code, but the user interface can help seeing the flow of a specific piece of functionality.

## Compiling and running
Make sure nothing else is running on port 8080.

```shell
mvn clean compile gwt:compile spring-boot:run
```

## Login to UI
After compiling and running, the web application will can be accessed at this url: <http://localhost:8080/login>

You will first be presented with a login page and you can use the following users.

User role:
* username: user
* password: 11

Admin role:
* username: admin
* password: 11

After login there will be buttons that illustrate the different Errai @Service implementations like MessageCallback, @Command, RPC and initiating named messages sent back to the client side. Many of them just result in a log statement.

There is one button called "ADMIN ONLY RPC: trigger" which illustates use of Errai security's @RestrictedAccess annotation:

```java
    @RestrictedAccess(roles = { "admin" })
    public void triggerMessage();
```
