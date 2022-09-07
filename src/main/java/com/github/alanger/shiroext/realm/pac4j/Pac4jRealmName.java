package com.github.alanger.shiroext.realm.pac4j;

import static com.github.alanger.shiroext.realm.RealmUtils.asList;

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

import com.github.alanger.shiroext.realm.ICommonPermission;
import com.github.alanger.shiroext.realm.ICommonRole;
import com.github.alanger.shiroext.realm.IPrincipalName;
import com.github.alanger.shiroext.realm.IUserPrefix;

// See https://github.com/bujiio/buji-pac4j/blob/master/src/main/java/io/buji/pac4j/realm/Pac4jRealm.java
public class Pac4jRealmName extends Pac4jRealm implements ICommonPermission, ICommonRole, IUserPrefix, IPrincipalName {

    private String commonRole = null;
    private String commonPermission = null;
    private String userPrefix = "";

    @Override
    public String getCommonRole() {
        return commonRole;
    }

    @Override
    public void setCommonRole(String commonRole) {
        this.commonRole = commonRole;
    }

    @Override
    public String getCommonPermission() {
        return commonPermission;
    }

    @Override
    public void setCommonPermission(String commonPermission) {
        this.commonPermission = commonPermission;
    }

    @Override
    public String getUserPrefix() {
        return userPrefix;
    }

    @Override
    public void setUserPrefix(String userPrefix) {
        this.userPrefix = userPrefix;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {

        final Pac4jToken token = (Pac4jToken) authenticationToken;
        // Compatibility with buji-pac4j 4.1.1
        final List<? extends UserProfile> profiles = token.getProfiles();

        final Pac4jPrincipalName principal = new Pac4jPrincipalName(profiles, getPrincipalNameAttribute());
        principal.setUserPrefix(getUserPrefix());
        final PrincipalCollection principalCollection = new SimplePrincipalCollection(principal, getName());
        return new SimpleAuthenticationInfo(principalCollection, profiles.hashCode());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        final Set<String> roles = new HashSet<>();
        final Set<String> permissions = new HashSet<>();
        final Pac4jPrincipalName principal = principals.oneByType(Pac4jPrincipalName.class);
        if (principal != null) {
            roles.addAll(asList(commonRole));
            permissions.addAll(asList(commonPermission));

            // Compatibility with buji-pac4j 4.1.1
            final List<? extends UserProfile> profiles = principal.getProfiles();
            for (final UserProfile profile : profiles) {
                if (profile != null) {
                    roles.addAll(profile.getRoles());
                    profile.addRoles(asList(commonRole));

                    permissions.addAll(profile.getPermissions());
                    profile.addPermissions(asList(commonPermission));
                }
            }
        }

        final SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRoles(roles);
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }
}
