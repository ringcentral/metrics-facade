package com.ringcentral.platform.metrics.guide.chapter03;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Listing02Test {

    @Test
    public void test() {
        // given, when, then
        assertThatThrownBy(() -> Listing02.main(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("labelValues = [LabelValue{label=Label{name='server'}, value='auth-server-1'}, LabelValue{label=Label{name='port'}, value='8080'}, LabelValue{label=Label{name='service'}, value='auth'}] do not match labels = [Label{name='service'}, Label{name='server'}, Label{name='port'}]");
    }
}
