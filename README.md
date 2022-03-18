# Extension for Apache Shiro

[![Build Status](https://travis-ci.org/a-langer/shiro-ext.svg?branch=master)](https://travis-ci.org/a-langer/shiro-ext)
[![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/a-langer/shiro-ext/blob/master/LICENSE)
[![Maven JitPack](https://img.shields.io/github/tag/a-langer/shiro-ext.svg?label=maven)](https://jitpack.io/#a-langer/shiro-ext)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/shiro-ext/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.a-langer/shiro-ext)

This project implement extension for security framework [Apache Shiro][1].

## Supported features

* Additional Shiro [filters][2] classes:  
    [RolesAuthzFilter](src/main/java/com/github/alanger/shiroext/web/RolesAuthzFilter.java) - checks the need for all the listed roles:  

    ```ini
    [filters]
    roles = com.github.alanger.shiroext.web.RolesAuthzFilter
    [urls]
    # Require user must be member all roles
    /protected/** = roles[admin,user,manager]
    ```

    [RoleAuthzFilter](src/main/java/com/github/alanger/shiroext/web/RoleAuthzFilter.java) - checks the need for any one the listed roles:  

    ```ini
    [filters]
    role = com.github.alanger.shiroext.web.RoleAuthzFilter
    [urls]
    # Require user must be member any one role
    /protected/** = role[admin,user,manager]
    ```

    [PermissionsAuthzFilter](src/main/java/com/github/alanger/shiroext/web/PermissionsAuthzFilter.java) - checks the need for all the listed permissions:  

    ```ini
    [filters]
    perms = com.github.alanger.shiroext.web.PermissionsAuthzFilter
    [urls]
    # Require user must be have all permissions
    /protected/** = perms[read,write,create]
    ```

    [PermissionAuthzFilter](src/main/java/com/github/alanger/shiroext/web/PermissionAuthzFilter.java) - checks the need for any one the listed permissions:  

    ```ini
    [filters]
    perm = com.github.alanger.shiroext.web.PermissionAuthzFilter
    [urls]
    # Require user must be have any one permission
    /protected/** = perm[read,write,create]
    ```

    [FormAuthcFilter](src/main/java/com/github/alanger/shiroext/web/FormAuthcFilter.java) - for authentication through form:  

    ```ini
    [filters]
    authc = com.github.alanger.shiroext.web.FormAuthcFilter
    authc.loginUrl  = /login
    [urls]
    /** = authc
    ```

    [BasicAuthFilter](src/main/java/com/github/alanger/shiroext/web/BasicAuthcFilter.java) - for basic authentication:  

    ```ini
    [filters]
    basic = com.github.alanger.shiroext.web.BasicAuthFilter
    [urls]
    /** = basic
    ```

    [LogoutAuthcFilter](src/main/java/com/github/alanger/shiroext/web/LogoutAuthcFilter.java) - for destroy user session:  

    ```ini
    [filters]
    logout = com.github.alanger.shiroext.web.LogoutAuthcFilter
    [urls]
    /logout = logout
    ```

* All Shiro filters supported two modes:  
    1. Silent mode - not return redirect or authentication challenge and do not finished HTTP response. Silent mode configuration example (disabled by default):  

        ```ini
        [filters]
        basicSilent = com.github.alanger.shiroext.web.BasicAuthcFilter
        basicSilent.silent = true
        authcSilent = com.github.alanger.shiroext.web.FormAuthcFilter
        authcSilent.silent = true
        logoutSilent = com.github.alanger.shiroext.web.LogoutAuthcFilter
        logoutSilent.silent = true
        ```  

    2. XHR mode - not return redirect or authentication challenge and always  do finished the HTTP response. Mod activated if HTTP header contains:

        ```properties
        X-Requested-With: XMLHttpRequest
        ```

* [ActiveDirectoryRealm](src/main/java/com/github/alanger/shiroext/realm/activedirectory/ActiveDirectoryRealm.java) - security realm for Active Directory (LDAP) with additional options:  

    Common or specified suffix of principal for system username:

    ```ini
    CORP = com.github.alanger.shiroext.realm.activedirectory.ActiveDirectoryRealm
    CORP.url = ldaps://corp.company.com:636
    # Common suffix principal for all users
    CORP.principalSuffix = @CORP.COMPANY.COM
    # System username will be used common suffix principal
    CORP.systemUsername = username
    # System username will be used specified suffix principal
    # CORP.systemUsername = username@SPECIFIED.SUFFIX.COM
    CORP.systemPassword = password
    ```

    Special prefix for a more unique username of ldap realm:

    ```ini
    # User "myuser" will be translated in "CORP.myuser", after authentication
    CORP.userPrefix = CORP.
    ```

    Support of domain name:

    ```ini
    # "CORP\username" or just "username" will be the correct
    CORP = com.github.alanger.shiroext.realm.activedirectory.ActiveDirectoryRealm
    # If uncomment this, then will be correct only "CORP\username"
    # CORP.named = true
    ```

    Load of roles nested if the following is configured:

    ```ini
    CORP.roleBase = OU=Departments,OU=HUB,DC=corp,DC=company,DC=com
    CORP.roleSearch = (&(objectClass=group)(member={0}))
    CORP.roleNested = true
    ```

    Can optionally add a common role:

    ```ini
    CORP.commonRole = All_Corp_Users
    ```

    Black and white list of users:

    ```ini
    # Only matching users can be authenticated
    CORP.userWhiteList = user1|user2|user3
    # Only not matching users can be authenticated
    CORP.userBlackList = baduser1|baduser2|baduser3
    ```

    Black and white list of roles:

    ```ini
    # Only matching roles can be authorized
    CORP.roleWhiteList = role1|role2|role3
    # Only not matching roles can be authorized
    CORP.roleBlackList = badrole1|badrole2|badrole3
    ```

* [AttributeAuthenticationListener](src/main/java/com/github/alanger/shiroext/authc/AttributeAuthenticationListener.java) - if realm implements [AttributeProvider](src/main/java/com/github/alanger/shiroext/realm/AttributeProvider.java), then listener saving user attributes to `org.apache.shiro.session.Session`:  

    ```ini
    authcListener = com.github.alanger.shiroext.authc.AttributeAuthenticationListener
    securityManager = org.apache.shiro.web.mgt.DefaultWebSecurityManager
    securityManager.authenticator.authenticationListeners = $authcListener
    ```

* [AssignedRealmAuthorizer](src/main/java/com/github/alanger/shiroext/authz/AssignedRealmAuthorizer.java) - allows only roles to be applied to user from the  realm in which the authorization takes place, is used in conjunction with `org.apache.shiro.authc.pam.FirstSuccessfulStrategy`:

    ```ini
    realmAuthorizer = com.github.alanger.shiroext.authz.AssignedRealmAuthorizer
    authcStrategy   = org.apache.shiro.authc.pam.FirstSuccessfulStrategy
    securityManager = org.apache.shiro.web.mgt.DefaultWebSecurityManager
    securityManager.authenticator.authenticationStrategy = $authcStrategy
    securityManager.authorizer = $realmAuthorizer
    ```

* Simple servlet and filters (configured in descriptor `web.xml`):  
    [ScriptProcessedServlet](src/main/java/com/github/alanger/shiroext/servlets/ScriptProcessedServlet.java) - delegate processing HTTP request and response to specified script (by default JavaScript through [Nashorn][7] engine):  

    ```xml
    <servlet>
        <servlet-name>script-processed-servlet</servlet-name>
        <servlet-class>com.github.alanger.shiroext.servlets.ScriptProcessedServlet</servlet-class>
        <init-param>
            <param-name>invoke-script-text</param-name>
            <param-value>response.getOutputStream().print("text1")</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>script-processed-servlet</servlet-name>
        <url-pattern>/text1/*</url-pattern>
    </servlet-mapping>
    ```

    [ScriptProcessedFilter](src/main/java/com/github/alanger/shiroext/servlets/ScriptProcessedFilter.java) - similarly `ScriptProcessedServlet`, but implemented as filter:  

    ```xml
    <filter>
        <filter-name>script-processed-filter</filter-name>
        <filter-class>com.github.alanger.shiroext.servlets.ScriptProcessedFilter</filter-class>
        <init-param>
            <param-name>invoke-script-text</param-name>
            <param-value>response.addHeader("script-filter", "true")</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>script-processed-filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ```

    [ResponseComittedFilter](src/main/java/com/github/alanger/shiroext/servlets/ResponseComittedFilter.java) - filter not calling `doFilter` method if response `isCommitted`:  

    ```xml
    <!-- Previous filter, response may have been committed -->
    <filter>
        <filter-name>response-comitted-filter</filter-name>
        <filter-class>com.github.alanger.shiroext.servlets.ResponseComittedFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>response-comitted-filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <!-- Next filter do chain of request only if response not committed -->
    ```

    [MutableRequestFilter](src/main/java/com/github/alanger/shiroext/servlets/MutableRequestFilter.java) - makes HttpRequest object is mutable, see [MutableRequestWrapper](src/main/java/com/github/alanger/shiroext/servlets/MutableRequestWrapper.java):  

    ```xml
    <filter>
        <filter-name>mutable-request-filter</filter-name>
        <filter-class>com.github.alanger.shiroext.servlets.MutableRequestFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>mutable-request-filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ```

    [MultiReadRequestFilter](src/main/java/com/github/alanger/shiroext/servlets/MultiReadRequestFilter.java) - similarly `MutableRequestFilter` and makes HttpRequest object is multiple readable, see [MultiReadRequestWrapper](src/main/java/com/github/alanger/shiroext/servlets/MultiReadRequestWrapper.java):  

    ```xml
    <filter>
        <filter-name>multiread-request-filter</filter-name>
        <filter-class>com.github.alanger.shiroext.servlets.MultiReadRequestFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>multiread-request-filter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ```

* [RedirectView](src/main/java/org/apache/shiro/web/util/RedirectView.java) - overrides original Shiro class (only in jar without all dependencies `shiro-ext-{version}.jar`) for using code `303` in redirects if property specified:

    ```properties
    -Dorg.apache.shiro.web.util.RedirectView.ignoreHttp10Compatible=true  
    ```

## Getting the library using Maven

Add this dependency to your `pom.xml` to reference the library:

```xml
<dependency>
    <groupId>com.github.a-langer</groupId>
    <artifactId>shiro-ext</artifactId>
    <version>0.0.3</version>
</dependency>
```

Or this dependency if need all libraries in one file:

```xml
<dependency>
    <groupId>com.github.a-langer</groupId>
    <artifactId>shiro-ext</artifactId>
    <version>0.0.3</version>
    <classifier>all</classifier>
</dependency>
```

## Usage

See Apache Shiro [documentation][3] and this examples:

* [application.properties](application.properties) - configuration for authentications in two realm.
* [jenkins-example](jenkins-example) - configuration for integration Apache Shiro and [Jenkins][6] with using [UrlRewriteFilter][4](for hot reload of config support).

## Related repositories

* [buji-pac4j][8] - Security library for Shiro web applications which supports OAuth, SAML, CAS, OpenID, Google App Engine, Kerberos, JWT and more.
* [UrlRewriteFilter][4] - Java Web Filter for any J2EE compliant web application server.
* [WebDAV VFS gate][5] - WebDAV gateway for accessing to different file systems.

[1]: https://shiro.apache.org/introduction.html
[2]: https://shiro.apache.org/web.html
[3]: https://shiro.apache.org/webapp-tutorial.html
[4]: https://github.com/paultuckey/urlrewritefilter
[5]: https://github.com/a-langer/webdav-vfs-gate
[6]: https://www.jenkins.io/
[7]: https://docs.oracle.com/javase/10/nashorn/introduction.htm#JSNUG136
[8]: https://github.com/bujiio/buji-pac4j
