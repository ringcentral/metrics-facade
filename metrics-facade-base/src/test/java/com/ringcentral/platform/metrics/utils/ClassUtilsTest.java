package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import java.util.Set;

import static com.ringcentral.platform.metrics.utils.ClassUtils.interfacesOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class ClassUtilsTest {

    interface TestInterfaceA {}
    interface TestInterfaceB extends TestInterfaceA {}
    interface TestInterfaceC {}

    static class TestClassA implements TestInterfaceA {}
    static class TestClassB implements TestInterfaceA, TestInterfaceB {}
    static class TestClassC extends TestClassB implements TestInterfaceC {}

    @Test
    public void interfaces_Null() {
        assertNull(interfacesOf(null));
    }

    @Test
    public void interfaces_Object() {
        assertTrue(interfacesOf(Object.class).isEmpty());
    }

    @Test
    public void interfaces_SingleInterface() {
        assertThat(interfacesOf(TestClassA.class), is(Set.of(TestInterfaceA.class)));
    }

    @Test
    public void interfaces_MultipleInterfaces() {
        assertThat(interfacesOf(TestClassB.class), is(Set.of(TestInterfaceA.class, TestInterfaceB.class)));
    }

    @Test
    public void interfaces_InheritedInterfaces() {
        assertThat(interfacesOf(TestClassC.class), is(Set.of(TestInterfaceA.class, TestInterfaceB.class, TestInterfaceC.class)));
    }
}