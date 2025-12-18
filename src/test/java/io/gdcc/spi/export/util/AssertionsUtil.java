package io.gdcc.spi.export.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.gdcc.spi.export.dcat3.config.model.NodeTemplate;
import io.gdcc.spi.export.dcat3.config.model.ValueSource;

public final class AssertionsUtil {

    private AssertionsUtil() {}

    public static void assertValueSource(
            ValueSource vs,
            String as,
            String predicate,
            String lang,
            String datatype,
            String json,
            String constValue,
            String nodeRef,
            boolean multi) {
        assertThat(vs).as("ValueSource should not be null").isNotNull();
        assertThat(vs.as).as("as").isEqualTo(as);
        assertThat(vs.predicate).as("predicate").isEqualTo(predicate);
        assertThat(vs.lang).as("lang").isEqualTo(lang);
        assertThat(vs.datatype).as("datatype").isEqualTo(datatype);
        assertThat(vs.json).as("json").isEqualTo(json);
        assertThat(vs.constValue).as("constValue").isEqualTo(constValue);
        assertThat(vs.nodeRef).as("nodeRef").isEqualTo(nodeRef);
        assertThat(vs.multi).as("multi").isEqualTo(multi);
    }

    public static void assertNodeTemplate(
            NodeTemplate nodeTemplate, String id, String kind, String iriConst, String type) {
        assertThat(nodeTemplate).as("NodeTemplate should not be null").isNotNull();
        assertThat(nodeTemplate.id).as("id").isEqualTo(id);
        assertThat(nodeTemplate.kind).as("kind").isEqualTo(kind);
        assertThat(nodeTemplate.iriConst).as("iriConst").isEqualTo(iriConst);
        assertThat(nodeTemplate.type).as("type").isEqualTo(type);
    }
}
