package com.github.alanger.shiroext.web;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BearerAuthcFilterTest extends BasicAuthcFilterTest {

    public BearerAuthcFilterTest() throws Throwable {
        super();
        path = "/bearer";
        pathSilent = "/bearerSilent";
        authorization = BearerAuthcFilter.BEARER + " " + admin;
    }

}
