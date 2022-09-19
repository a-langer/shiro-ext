package com.github.alanger.shiroext.authz.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

// https://github.com/apache/shiro/blob/main/core/src/test/java/org/apache/shiro/authz/permission/AntPermissionTest.java
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AntPermissionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new AntPermission(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        new AntPermission("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlank() {
        new AntPermission("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyDelimiters() {
        new AntPermission("::,,::,:");
    }

    @Test
    public void testNamed() {
        AntPermission p1, p2;

        // Case insensitive, same
        p1 = new AntPermission("something");
        p2 = new AntPermission("something");
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));

        // Case insensitive, different case
        p1 = new AntPermission("something");
        p2 = new AntPermission("SOMETHING");
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));

        // Case insensitive, different word
        p1 = new AntPermission("something");
        p2 = new AntPermission("else");
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));

        // Case sensitive same
        p1 = new AntPermission("BLAHBLAH", false);
        p2 = new AntPermission("BLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));

        // Case sensitive, different case
        p1 = new AntPermission("BLAHBLAH", false);
        p2 = new AntPermission("bLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));

        // Case sensitive, different word
        p1 = new AntPermission("BLAHBLAH", false);
        p2 = new AntPermission("whatwhat", false);
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));
    }

    @Test
    public void testAntNamed() {
        AntPermission p1, p2;

        // Case insensitive, same
        p1 = new AntPermission("s?met?ing");
        p2 = new AntPermission("something");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new AntPermission("s?met?ing2");
        assertFalse(p1.implies(p2));

        // Case insensitive, different case
        p1 = new AntPermission("s?met?ing");
        p2 = new AntPermission("SOMETHING");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new AntPermission("s?met?ing2");
        assertFalse(p1.implies(p2));

        // Case insensitive, different word
        p1 = new AntPermission("s?met?ing*");
        p2 = new AntPermission("else");
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));

        // Case sensitive same
        p1 = new AntPermission("BL?HBL?H*", false);
        p2 = new AntPermission("BLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new AntPermission("BL?HBL?H*2");
        assertFalse(p1.implies(p2));

        // Case sensitive, different case
        p1 = new AntPermission("BL?HBL?H*", false);
        p2 = new AntPermission("bLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));

        // Case sensitive, different word
        p1 = new AntPermission("BL?HBL?H*", false);
        p2 = new AntPermission("whatwhat", false);
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));
    }

    @Test
    public void testLists() {
        AntPermission p1, p2, p3;

        p1 = new AntPermission("one,two");
        p2 = new AntPermission("one");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));

        p1 = new AntPermission("one,two,three");
        p2 = new AntPermission("one,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));

        p1 = new AntPermission("one,two:one,two,three");
        p2 = new AntPermission("one:three");
        p3 = new AntPermission("one:two,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p3));
        assertTrue(p3.implies(p2));

        p1 = new AntPermission("one,two,three:one,two,three:one,two");
        p2 = new AntPermission("one:three:two");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));

        p1 = new AntPermission("one");
        p2 = new AntPermission("one:two,three,four");
        p3 = new AntPermission("one:two,three,four:five:six:seven");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p1));
        assertFalse(p3.implies(p1));
        assertTrue(p2.implies(p3));
    }

    @Test
    public void testAntLists() {
        AntPermission p1, p2, p3;

        p1 = new AntPermission("one*,two");
        p2 = new AntPermission("one");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new AntPermission("one*2,two");
        assertFalse(p1.implies(p2));

        p1 = new AntPermission("one*,t?o,th?ee");
        p2 = new AntPermission("one,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new AntPermission("one*,t?o,three??");
        assertFalse(p1.implies(p2));

        p1 = new AntPermission("o?e,tw?:o?e,two*,t?ree");
        p2 = new AntPermission("one:three");
        p3 = new AntPermission("one:two,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p3));
        assertTrue(p3.implies(p2));

        p1 = new AntPermission("o?e,tw?,thr?e:?ne,tw?,thr?e:o?e,?wo");
        p2 = new AntPermission("one:three:two");
        p3 = new AntPermission("ope:three:two");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        assertTrue(p1.implies(p3));

        p1 = new AntPermission("on?");
        p2 = new AntPermission("one:two,three,four");
        p3 = new AntPermission("one:two,three,four:five:six:seven");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p1));
        assertFalse(p3.implies(p1));
        p2 = new AntPermission("o?e:two,three,four");
        assertTrue(p2.implies(p3));
    }

    /**
     * Validates AntPermissions with that contain the same list parts are equal.
     */
    @Test
    public void testListDifferentOrder() {
        AntPermission p6 = new AntPermission("one,two:three,four");
        AntPermission p6DiffOrder = new AntPermission("two,one:four,three");
        assertEquals(p6DiffOrder, p6);
    }

    @Test
    public void testAntListDifferentOrder() {
        AntPermission p6 = new AntPermission("o?e,two:thr?e,?our");
        AntPermission p6DiffOrder = new AntPermission("two,o?e:?our,thr?e");
        assertEquals(p6DiffOrder, p6);
    }

    @Test
    public void testWildcards() {
        AntPermission p1, p2, p3, p4, p5, p6, p7, p8, p9;

        p1 = new AntPermission("*");
        p2 = new AntPermission("one");
        p3 = new AntPermission("one:two");
        p4 = new AntPermission("one,two:three,four");
        p5 = new AntPermission("one,two:three,four,five:six:seven,eight");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));

        p1 = new AntPermission("newsletter:*");
        p2 = new AntPermission("newsletter:read");
        p3 = new AntPermission("newsletter:read,write");
        p4 = new AntPermission("newsletter:*");
        p5 = new AntPermission("newsletter:*:*");
        p6 = new AntPermission("newsletter:*:read");
        p7 = new AntPermission("newsletter:write:*");
        p8 = new AntPermission("newsletter:read,write:*");
        p9 = new AntPermission("newsletter");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("newsletter:*:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("newsletter:*:*:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("newsletter");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("newsletter:*:read");
        p2 = new AntPermission("newsletter:123:read");
        p3 = new AntPermission("newsletter:123,456:read,write");
        p4 = new AntPermission("newsletter:read");
        p5 = new AntPermission("newsletter:read,write");
        p6 = new AntPermission("newsletter:123:read:write");
        assertTrue(p1.implies(p2));
        assertFalse(p1.implies(p3));
        assertFalse(p1.implies(p4));
        assertFalse(p1.implies(p5));
        assertTrue(p1.implies(p6));

        p1 = new AntPermission("newsletter:*:read:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p6));
    }

    @Test
    public void testAntWildcards() {
        AntPermission p1, p2, p3, p4, p5, p6, p7, p8, p9;

        p1 = new AntPermission("**");
        p2 = new AntPermission("one");
        p3 = new AntPermission("one:two");
        p4 = new AntPermission("one,two:three,four");
        p5 = new AntPermission("one,two:three,four,five:six:seven,eight");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));

        p1 = new AntPermission("ne?sle?ter:**");
        p2 = new AntPermission("newsletter:read");
        p3 = new AntPermission("newsletter:read,write");
        p4 = new AntPermission("newsletter:*");
        p5 = new AntPermission("newsletter:*:*");
        p6 = new AntPermission("newsletter:*:read");
        p7 = new AntPermission("newsletter:write:*");
        p8 = new AntPermission("newsletter:read,write:*");
        p9 = new AntPermission("newsletter");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("ne?sle?ter:r*ad,wr?te");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertFalse(p1.implies(p4));
        assertFalse(p1.implies(p5));
        assertFalse(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertFalse(p1.implies(p9));

        p1 = new AntPermission("ne?sle?ter:**:**");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("ne?sle?ter:**:**:**");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("ne?sle*r");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        assertTrue(p1.implies(p9));

        p1 = new AntPermission("ne*slette?:12*:rea?");
        p2 = new AntPermission("newsletter:123:read");
        p3 = new AntPermission("newsletter:123,456:read,write");
        p4 = new AntPermission("newsletter:read");
        p5 = new AntPermission("newsletter:read,write");
        p6 = new AntPermission("newsletter:123:read:write");
        assertTrue(p1.implies(p2));
        assertFalse(p1.implies(p3));
        assertFalse(p1.implies(p4));
        assertFalse(p1.implies(p5));
        assertTrue(p1.implies(p6));

        p1 = new AntPermission("ne*slette?:**:rea?:**");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p6));

        p1 = new AntPermission("ne*slette?:**:read?:**");
        assertFalse(p1.implies(p2));
        assertFalse(p1.implies(p6));
    }

    @Test
    public void testToString() {
        AntPermission p1 = new AntPermission("*");
        AntPermission p2 = new AntPermission("one");
        AntPermission p3 = new AntPermission("one:two");
        AntPermission p4 = new AntPermission("one,two:three,four");
        AntPermission p5 = new AntPermission("one,two:three,four,five:six:seven,eight");

        assertEquals("*", p1.toString());
        assertEquals(p1, new AntPermission(p1.toString()));
        assertEquals("one", p2.toString());
        assertEquals(p2, new AntPermission(p2.toString()));
        assertEquals("one:two", p3.toString());
        assertEquals(p3, new AntPermission(p3.toString()));
        assertEquals("one,two:three,four", p4.toString());
        assertEquals(p4, new AntPermission(p4.toString()));
        assertEquals("one,two:three,four,five:six:seven,eight", p5.toString());
        assertEquals(p5, new AntPermission(p5.toString()));
    }

    @Test
    public void testAntToString() {
        AntPermission p1 = new AntPermission("**");
        AntPermission p2 = new AntPermission("on?");
        AntPermission p3 = new AntPermission("on?:two");
        AntPermission p4 = new AntPermission("on?,two:three,four");
        AntPermission p5 = new AntPermission("on?,two:three,four,five:six:seven,eight");

        assertEquals("**", p1.toString());
        assertEquals(p1, new AntPermission(p1.toString()));
        assertEquals("on?", p2.toString());
        assertEquals(p2, new AntPermission(p2.toString()));
        assertEquals("on?:two", p3.toString());
        assertEquals(p3, new AntPermission(p3.toString()));
        assertEquals("on?,two:three,four", p4.toString());
        assertEquals(p4, new AntPermission(p4.toString()));
        assertEquals("on?,two:three,four,five:six:seven,eight", p5.toString());
        assertEquals(p5, new AntPermission(p5.toString()));
    }

    @Test
    public void testWildcardLeftTermination() {
        AntPermission p1, p2, p3, p4;

        p1 = new AntPermission("one");
        p2 = new AntPermission("one:*");
        p3 = new AntPermission("one:*:*");
        p4 = new AntPermission("one:read");

        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));

        assertTrue(p2.implies(p1));
        assertTrue(p2.implies(p3));
        assertTrue(p2.implies(p4));

        assertTrue(p3.implies(p1));
        assertTrue(p3.implies(p2));
        assertTrue(p3.implies(p4));

        assertFalse(p4.implies(p1));
        assertFalse(p4.implies(p2));
        assertFalse(p4.implies(p3));
    }

    @Test
    public void testAntWildcardLeftTermination() {
        AntPermission p1, p2, p3, p4;

        p1 = new AntPermission("on?");
        p2 = new AntPermission("on?:**");
        p3 = new AntPermission("on?:**:**");
        p4 = new AntPermission("on?:rea?");

        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));

        assertTrue(p2.implies(p1));
        assertTrue(p2.implies(p3));
        assertTrue(p2.implies(p4));

        assertTrue(p3.implies(p1));
        assertTrue(p3.implies(p2));
        assertTrue(p3.implies(p4));

        assertFalse(p4.implies(p1));
        assertFalse(p4.implies(p2));
        assertFalse(p4.implies(p3));
    }
}