package com.github.alanger.shiroext.web;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PermissionsAuthzFilterTest extends RolesAuthzFilterTest {

    public PermissionsAuthzFilterTest() throws Throwable {
        super();
        urlUnauthorized = "/permsOnly";
        urlForbidden = "/perms";
        urlAllowed = "/perm";
    }

}
