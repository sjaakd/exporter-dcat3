package io.gdcc.spi.export.dcat3.config.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RootConfig {
    public String outputFormat = "turtle";
    public boolean trace = false;

    public Map<String, String> prefixes = new LinkedHashMap<>();

    public List<Element> elements = new ArrayList<>();
    public List<Relation> relations = new ArrayList<>();
    /**
     * Directory of the root file; used to resolve element files relative to it
     */
    public Path baseDir;
}
