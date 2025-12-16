package io.gdcc.spi.export.dcat3.config.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceConfig {
    public Subject subject = new Subject();
    public Map<String, ValueSource> props = new LinkedHashMap<>();
    public Map<String, NodeTemplate> nodes = new LinkedHashMap<>();
}
