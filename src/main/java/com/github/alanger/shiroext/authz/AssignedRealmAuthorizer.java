package com.github.alanger.shiroext.authz;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

public class AssignedRealmAuthorizer extends ModularRealmAuthorizer {

    public Collection<Realm> getRealms(PrincipalCollection principals) {
        Set<Realm> assignedRealms = new LinkedHashSet<>();
        for (Realm realm : getRealms()) {
            Collection<?> assigned = principals.fromRealm(realm.getName());
            if (assigned != null && !assigned.isEmpty())
                assignedRealms.add(realm);
        }
        return assignedRealms;
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        assertRealmsConfigured();
        for (Realm realm : getRealms(principals)) {
            if (!(realm instanceof Authorizer))
                continue;
            if (((Authorizer) realm).isPermitted(principals, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        assertRealmsConfigured();
        for (Realm realm : getRealms(principals)) {
            if (!(realm instanceof Authorizer))
                continue;
            if (((Authorizer) realm).isPermitted(principals, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
        assertRealmsConfigured();
        for (Realm realm : getRealms(principals)) {
            if (!(realm instanceof Authorizer))
                continue;
            if (((Authorizer) realm).hasRole(principals, roleIdentifier)) {
                return true;
            }
        }
        return false;
    }

}
