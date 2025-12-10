package io.gdcc.spi.export.dcat3.config;


// PropertiesMappingLoader.java

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesMappingLoader {

    public MappingModel.Config load(InputStream in) throws Exception {
        Properties p = new Properties();
        p.load(in);

        MappingModel.Config cfg = new MappingModel.Config();

        // Subject
        cfg.subject.iriConst    = p.getProperty("subject.iri.const");
        cfg.subject.iriTemplate = p.getProperty("subject.iri.template");
        cfg.subject.iriJson     = p.getProperty("subject.iri.json");

        // Props
        Pattern propPat = Pattern.compile("^props\\.([^.]+)\\.(.+)$");
        for (String k : p.stringPropertyNames()) {
            Matcher m = propPat.matcher(k);
            if (!m.matches()) continue;
            String id = m.group(1);
            String tail = m.group(2);
            MappingModel.ValueSource vs = cfg.props.computeIfAbsent(id, _k -> new MappingModel.ValueSource());
            applyValue(vs, tail, p.getProperty(k));
        }

        // Nodes
        Pattern nodePat = Pattern.compile("^nodes\\.([^.]+)\\.(.+)$");
        Pattern nodePropPat = Pattern.compile("^props\\.([^.]+)\\.(.+)$");
        for (String k : p.stringPropertyNames()) {
            Matcher m = nodePat.matcher(k);
            if (!m.matches()) continue;
            String nodeId = m.group(1);
            String tail = m.group(2);
            MappingModel.NodeTemplate nt = cfg.nodes.computeIfAbsent(nodeId, _k -> {
                MappingModel.NodeTemplate t = new MappingModel.NodeTemplate();
                t.id = nodeId;
                return t;
            });

            if (tail.equals("kind")) nt.kind = p.getProperty(k);
            else if (tail.equals("iri.const")) nt.iriConst = p.getProperty(k);
            else if (tail.equals("type")) nt.type = p.getProperty(k);
            else {
                Matcher mp = nodePropPat.matcher(tail);
                if (mp.matches()) {
                    String propId = mp.group(1);
                    String propTail = mp.group(2);
                    MappingModel.ValueSource vs = nt.props.computeIfAbsent(propId, _k -> new MappingModel.ValueSource());
                    applyValue(vs, propTail, p.getProperty(k));
                }
            }
        }

        return cfg;
    }

    private void applyValue(MappingModel.ValueSource vs, String keyTail, String value) {
        switch (keyTail) {
            case "predicate": vs.predicate = value; break;
            case "as": vs.as = value; break;
            case "lang": vs.lang = value; break;
            case "datatype": vs.datatype = value; break;
            case "json": vs.json = value; break;
            case "const": vs.constValue = value; break;
            case "node": vs.nodeRef = value; break;
            case "multi": vs.multi = Boolean.parseBoolean(value); break;
            case "when": vs.when = value; break;
            default:
                if (keyTail.startsWith("map.")) {
                    String k = keyTail.substring("map.".length());
                    vs.map.put(k, value);
                }
        }
    }
}
