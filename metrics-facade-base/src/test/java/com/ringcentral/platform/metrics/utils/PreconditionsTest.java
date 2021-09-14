package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

@SuppressWarnings("ConstantConditions")
public class PreconditionsTest {

    @Test
    public void checkArgument_ok() {
        checkArgument(true, "test_errorMessage");
    }

    @Test
    public void checkArgument_fail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> checkArgument(false, "test_errorMessage"));
        assumeThat(ex.getMessage(), is("test_errorMessage"));
    }

    @Test
    public void checkState_ok() {
        checkState(true, "test_errorMessage");
    }

    @Test
    public void checkState_fail() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> checkState(false, "test_errorMessage"));
        assumeThat(ex.getMessage(), is("test_errorMessage"));
    }
}