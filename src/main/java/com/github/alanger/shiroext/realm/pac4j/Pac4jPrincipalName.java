package com.github.alanger.shiroext.realm.pac4j;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;

// See https://github.com/bujiio/buji-pac4j/blob/master/src/main/java/io/buji/pac4j/subject/Pac4jPrincipal.java
public class Pac4jPrincipalName implements Principal, Serializable {

    private final String principalNameAttribute;
    private final List<? extends UserProfile> profiles;

    private boolean byName = true;

    public Pac4jPrincipalName(final List<? extends UserProfile> profiles) {
        this.profiles = profiles;
        this.principalNameAttribute = null;
    }

    public Pac4jPrincipalName(final List<? extends UserProfile> profiles, String principalNameAttribute) {
        this.profiles = profiles;
        this.principalNameAttribute = CommonHelper.isBlank(principalNameAttribute) ? null
                : principalNameAttribute.trim();
    }

    public boolean isByName() {
        return byName;
    }

    public void setByName(boolean byname) {
        this.byName = byname;
    }

    public UserProfile getProfile() {
        return flatIntoOneProfile(this.profiles).orElseThrow();
    }

    // Compatibility with buji-pac4j 4.1.1
    public static <U extends UserProfile> Optional<U> flatIntoOneProfile(final Collection<U> profiles) {
        final Optional<U> profile = profiles.stream().filter(p -> p != null && !(p instanceof AnonymousProfile))
                .findFirst();
        if (profile.isPresent()) {
            return profile;
        } else {
            return profiles.stream().filter(Objects::nonNull).findFirst();
        }
    }

    public List<? extends UserProfile> getProfiles() {
        return this.profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final Pac4jPrincipalName that = (Pac4jPrincipalName) o;
        return profiles != null ? profiles.equals(that.profiles) : that.profiles == null;
    }

    @Override
    public int hashCode() {
        return profiles != null ? profiles.hashCode() : 0;
    }

    @Override
    public String getName() {
        final UserProfile profile = this.getProfile();
        if (null == principalNameAttribute) {
            return profile.getId();
        }
        final Object attrValue = profile.getAttribute(principalNameAttribute);
        return (null == attrValue) ? null : String.valueOf(attrValue);
    }

    @Override
    public String toString() {
        if (isByName())
            return getName();
        else
            return CommonHelper.toNiceString(this.getClass(), "profiles", getProfiles());
    }

}
