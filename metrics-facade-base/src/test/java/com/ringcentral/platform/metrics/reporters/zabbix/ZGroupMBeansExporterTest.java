package com.ringcentral.platform.metrics.reporters.zabbix;

import java.util.List;
import javax.management.*;
import org.junit.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import static javax.management.MBeanServerFactory.*;
import static junit.framework.TestCase.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@SuppressWarnings("SameParameterValue")
public class ZGroupMBeansExporterTest {

    static final String OBJECT_NAME_PREFIX = "zabbix_lld:group=";
    static final String GROUP_JSON_ATTR_NAME = "groupJsonAttrName";

    MBeanServer mBeanServer = newMBeanServer();
    ZGroupJsonMapper converter = new DefaultZGroupJsonMapper();
    ZGroupMBeansExporter exporter = new ZGroupMBeansExporter(mBeanServer, OBJECT_NAME_PREFIX, converter, GROUP_JSON_ATTR_NAME);
    int beforeMBeanCount;

    @Before
    public void before() {
        beforeMBeanCount = mBeanServer.getMBeanCount();
        assertThat(mBeanServer.getMBeanCount(), is(beforeMBeanCount));
    }

    @After
    public void after() {
        exporter.close();
        assertThat(mBeanServer.getMBeanCount(), is(beforeMBeanCount));
    }

    @Test
    public void exportingZGroupMBeans() {
        assertFalse(attrExists("group_1", GROUP_JSON_ATTR_NAME));
        assertFalse(attrExists("group_2", GROUP_JSON_ATTR_NAME));

        exporter.entityAdded(new ZEntity(
            "group_1",
            List.of(new ZAttribute("service", "service_1"), new ZAttribute("server", "server_1"))));

        assertThat(stringAttrValue("group_1", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVICE}\":\"service_1\",\"{#SERVER}\":\"server_1\"}]}"));
        assertFalse(attrExists("group_2", GROUP_JSON_ATTR_NAME));

        exporter.entityAdded(new ZEntity(
            "group_1",
            List.of(new ZAttribute("service", "service_2"), new ZAttribute("server", "server_2"))));

        assertThat(
            stringAttrValue("group_1", GROUP_JSON_ATTR_NAME),
            is("{\"data\":[{\"{#SERVICE}\":\"service_1\",\"{#SERVER}\":\"server_1\"},{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));

        assertFalse(attrExists("group_2", GROUP_JSON_ATTR_NAME));

        exporter.entityAdded(new ZEntity(
            "group_2",
            List.of(new ZAttribute("server", "server_1"), new ZAttribute("port", "port_1"))));

        assertThat(
            stringAttrValue("group_1", GROUP_JSON_ATTR_NAME),
            is("{\"data\":[{\"{#SERVICE}\":\"service_1\",\"{#SERVER}\":\"server_1\"},{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));

        assertThat(stringAttrValue("group_2", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVER}\":\"server_1\",\"{#PORT}\":\"port_1\"}]}"));

        exporter.entityAdded(new ZEntity(
            "group_2",
            List.of(new ZAttribute("server", "server_2"), new ZAttribute("port", "port_2"))));

        assertThat(
            stringAttrValue("group_1", GROUP_JSON_ATTR_NAME),
            is("{\"data\":[{\"{#SERVICE}\":\"service_1\",\"{#SERVER}\":\"server_1\"},{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));

        assertThat(
            stringAttrValue("group_2", GROUP_JSON_ATTR_NAME),
            is("{\"data\":[{\"{#SERVER}\":\"server_1\",\"{#PORT}\":\"port_1\"},{\"{#SERVER}\":\"server_2\",\"{#PORT}\":\"port_2\"}]}"));

        exporter.entityRemoved(new ZEntity(
            "group_1",
            List.of(new ZAttribute("service", "service_1"), new ZAttribute("server", "server_1"))));

        assertThat(stringAttrValue("group_1", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));

        assertThat(
            stringAttrValue("group_2", GROUP_JSON_ATTR_NAME),
            is("{\"data\":[{\"{#SERVER}\":\"server_1\",\"{#PORT}\":\"port_1\"},{\"{#SERVER}\":\"server_2\",\"{#PORT}\":\"port_2\"}]}"));

        exporter.entityRemoved(new ZEntity(
            "group_2",
            List.of(new ZAttribute("server", "server_2"), new ZAttribute("port", "port_2"))));

        assertThat(stringAttrValue("group_1", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));
        assertThat(stringAttrValue("group_2", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVER}\":\"server_1\",\"{#PORT}\":\"port_1\"}]}"));

        exporter.entityRemoved(new ZEntity(
            "group_2",
            List.of(new ZAttribute("server", "server_1"), new ZAttribute("port", "port_1"))));

        assertThat(stringAttrValue("group_1", GROUP_JSON_ATTR_NAME), is("{\"data\":[{\"{#SERVICE}\":\"service_2\",\"{#SERVER}\":\"server_2\"}]}"));
        assertThat(stringAttrValue("group_2", GROUP_JSON_ATTR_NAME), is("{\"data\":[]}"));
    }

    boolean attrExists(String groupName, String mBeanAttrName, LabelValue... labelValues) {
        try {
            return mBeanServer.getAttribute(objectName(groupName, labelValues), mBeanAttrName) != null;
        } catch (InstanceNotFoundException | AttributeNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String stringAttrValue(String groupName, String mBeanAttrName, LabelValue... labelValues) {
        return (String)attrValue(groupName, mBeanAttrName, labelValues);
    }

    Object attrValue(String groupName, String mBeanAttrName, LabelValue... labelValues) {
        try {
            return mBeanServer.getAttribute(objectName(groupName, labelValues), mBeanAttrName);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    ObjectName objectName(String groupName, LabelValue... labelValues) {
        try {
            if (labelValues.length == 0) {
                return new ObjectName("zabbix_lld", "group", groupName);
            } else {
                StringBuilder builder = new StringBuilder("zabbix_lld").append(":group=").append(groupName);
                List.of(labelValues).forEach(lv -> builder.append(',').append(escape(lv.label().name())).append('=').append(escape(lv.value())));
                return new ObjectName(builder.toString());
            }
        } catch (MalformedObjectNameException exception) {
            throw new RuntimeException(exception);
        }
    }

    private String escape(String v) {
        return v.replaceAll("[\\s*?,=:\\\\]", "_");
    }
}