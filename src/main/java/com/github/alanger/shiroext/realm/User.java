package com.github.alanger.shiroext.realm;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

public class User implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String dn;
    private final String password;
    private List<String> roles;
    private final String userRoleId;

    public User(String username, String dn, String password, List<String> roles, String userRoleId) {
        this.username = username;
        this.dn = dn;
        this.password = password;
        if (roles == null) {
            this.roles = Collections.emptyList();
        } else {
            this.roles = Collections.unmodifiableList(roles);
        }
        this.userRoleId = userRoleId;
    }

    @Override
    public String getName() {
        return username;
    }

    public String getUserName() {
        return getName();
    }

    public String getDN() {
        return dn;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        if (roles == null) {
            this.roles = Collections.emptyList();
        } else {
            this.roles = roles;
        }
    }

    public String getUserRoleId() {
        return userRoleId;
    }

}
