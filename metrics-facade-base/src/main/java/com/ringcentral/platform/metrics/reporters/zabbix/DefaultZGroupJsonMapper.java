package com.ringcentral.platform.metrics.reporters.zabbix;

public class DefaultZGroupJsonMapper implements ZGroupJsonMapper {

    public static final DefaultZGroupJsonMapper INSTANCE = new DefaultZGroupJsonMapper();

    @Override
    public String toJson(ZGroup group) {
        StringBuilder b = new StringBuilder();
        b.append('{').append("\"data\":[");
        boolean firstEntity = true;

        for (ZEntity entity : group.entities()) {
            if (firstEntity) {
                firstEntity = false;
            } else {
                b.append(',');
            }

            b.append('{');
            boolean firstAttr = true;

            for (ZAttribute a : entity.attributes()) {
                if (firstAttr) {
                    firstAttr = false;
                } else {
                    b.append(',');
                }

                b.append("\"{#").append(a.name().toUpperCase()).append("}\":\"").append(a.value()).append('"');
            }

            b.append('}');
        }

        return b.append(']').append('}').toString();
    }
}
