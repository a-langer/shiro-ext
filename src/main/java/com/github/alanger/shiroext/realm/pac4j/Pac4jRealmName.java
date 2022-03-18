package com.github.alanger.shiroext.realm.pac4j;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.token.Pac4jToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.pac4j.core.profile.UserProfile;

import java.util.*;

// See https://github.com/bujiio/buji-pac4j/blob/master/src/main/java/io/buji/pac4j/realm/Pac4jRealm.java
public class Pac4jRealmName extends Pac4jRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {

        final Pac4jToken token = (Pac4jToken) authenticationToken;
        // Compatibility with buji-pac4j 4.1.1
        final List<? extends UserProfile> profiles = token.getProfiles();

        final Pac4jPrincipalName principal = new Pac4jPrincipalName(profiles, getPrincipalNameAttribute());
        final PrincipalCollection principalCollection = new SimplePrincipalCollection(principal, getName());
        return new SimpleAuthenticationInfo(principalCollection, profiles.hashCode());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        final Set<String> roles = new HashSet<>();
        final Set<String> permissions = new HashSet<>();
        final Pac4jPrincipalName principal = principals.oneByType(Pac4jPrincipalName.class);
        if (principal != null) {
            // Compatibility with buji-pac4j 4.1.1
            final List<? extends UserProfile> profiles = principal.getProfiles();
            for (final UserProfile profile : profiles) {
                if (profile != null) {
                    roles.addAll(profile.getRoles());
                    permissions.addAll(profile.getPermissions());
                }
            }
        }

        final SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRoles(roles);
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }
}
