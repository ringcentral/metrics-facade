package com.ringcentral.platform.metrics.reporters.zabbix;

import java.util.*;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DefaultZGroupJsonMapperTest {

    @Test
    public void toJson() {
        LinkedHashSet<ZEntity> entities = new LinkedHashSet<>();
        entities.add(new ZEntity("groupName", List.of(new ZAttribute("attrName_1", "attrName_1_v_1"), new ZAttribute("attrName_2", "attrName_2_v_1"))));
        entities.add(new ZEntity("groupName", List.of(new ZAttribute("attrName_1", "attrName_1_v_2"), new ZAttribute("attrName_2", "attrName_2_v_2"))));
        ZGroup group = new ZGroup("groupName", entities);
        DefaultZGroupJsonMapper converter = new DefaultZGroupJsonMapper();

        assertThat(
            converter.toJson(group),
            is("{\"data\":[{\"{#ATTRNAME_1}\":\"attrName_1_v_1\",\"{#ATTRNAME_2}\":\"attrName_2_v_1\"},{\"{#ATTRNAME_1}\":\"attrName_1_v_2\",\"{#ATTRNAME_2}\":\"attrName_2_v_2\"}]}"));
    }
}