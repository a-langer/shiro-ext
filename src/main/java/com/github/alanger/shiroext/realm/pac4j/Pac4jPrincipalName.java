package com.github.alanger.shiroext.realm.pac4j;

import java.util.List;

import org.pac4j.core.profile.UserProfile;

import io.buji.pac4j.subject.Pac4jPrincipal;

// See https://github.com/bujiio/buji-pac4j/blob/master/src/main/java/io/buji/pac4j/subject/Pac4jPrincipal.java
public class Pac4jPrincipalName extends Pac4jPrincipal {

    public Pac4jPrincipalName(List<UserProfile> profiles) {
        super(profiles);
    }

    public Pac4jPrincipalName(List<UserProfile> profiles, String principalNameAttribute) {
        super(profiles, principalNameAttribute);
    }

    @Override
    public String toString() {
        // Short name
        return super.getName();
    }

}
