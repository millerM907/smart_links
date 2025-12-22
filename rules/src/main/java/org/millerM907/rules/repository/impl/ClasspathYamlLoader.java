package org.millerM907.rules.repository.impl;

import org.millerM907.rules.dsl.compiler.keys.*;
import org.millerM907.rules.dsl.conditions.*;
import org.millerM907.rules.repository.base.DslLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ClasspathYamlLoader implements DslLoader {

    private final Resource resource;

    public ClasspathYamlLoader(ResourceLoader loader,
                               @Value("${rules.file:classpath:rules.yaml}") String location) {
        this.resource = loader.getResource(location);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DslRoot load() {
        try (InputStream in = resource.getInputStream()) {
            Object tree = new Yaml().load(in);
            Map<String, Object> root = (tree instanceof Map<?, ?> m) ? (Map<String, Object>) m : Map.of();

            List<Map<String, Object>> links =
                    (List<Map<String, Object>>) root.getOrDefault(RootKeys.LINKS.getName(), List.of());

            List<RuleSet> sets = new ArrayList<>();
            for (Map<String, Object> link : links) {
                String slug = asString(link.get(LinkKeys.SLUG.getName()));
                String defaultUrl = asString(link.get(LinkKeys.DEFAULT_URL.getName()));

                List<Map<String, Object>> rulesRaw =
                        (List<Map<String, Object>>) link.getOrDefault(LinkKeys.RULES.getName(), List.of());

                List<RuleDef> rules = new ArrayList<>();
                for (Map<String, Object> r : rulesRaw) {
                    String id = asString(r.get(RuleKeys.ID.getName()));
                    Integer priority = asInt(r.get(RuleKeys.PRIORITY.getName()));
                    Map<String, Object> whenNode = (Map<String, Object>) r.get(RuleKeys.WHEN.getName());
                    Map<String, Object> thenNode = (Map<String, Object>) r.get(RuleKeys.THEN.getName());

                    CondDef when = parseCond(whenNode);
                    ThenDef then = parseThen(thenNode);

                    rules.add(new RuleDef(id, priority, when, then));
                }

                sets.add(new RuleSet(slug, defaultUrl, rules));
            }

            return new DslRoot(sets);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load DSL from " + resource, e);
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Integer asInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return Integer.valueOf(String.valueOf(v));
    }

    @SuppressWarnings("unchecked")
    private static CondDef parseCond(Map<String, Object> node) {
        if (node == null) return new AllDef(List.of());

        if (node.containsKey(CondKeys.ALL.getName())) {
            List<Object> children = (List<Object>) node.get(CondKeys.ALL.getName());
            List<CondDef> cs = children.stream()
                    .map(o -> parseCond((Map<String, Object>) o))
                    .collect(Collectors.toList());
            return new AllDef(cs);
        }

        if (node.containsKey(CondKeys.ANY.getName())) {
            List<Object> children = (List<Object>) node.get(CondKeys.ANY.getName());
            List<CondDef> cs = children.stream()
                    .map(o -> parseCond((Map<String, Object>) o))
                    .collect(Collectors.toList());
            return new AnyDef(cs);
        }

        if (node.containsKey(CondKeys.NOT.getName())) {
            return new NotDef(parseCond((Map<String, Object>) node.get(CondKeys.NOT.getName())));
        }

        String path = asString(node.get(CondKeys.PATH.getName()));
        String operator = asString(node.get(CondKeys.OPERATOR.getName()));
        Object value = node.get(CondKeys.VALUE.getName());
        return new PredDef(path, operator, value);
    }

    private static ThenDef parseThen(Map<String, Object> node) {
        if (node == null) return new ThenDef(null, null, null);
        String url = asString(node.get(ThenKeys.URL.getName()));
        Integer ttl = asInt(node.get(ThenKeys.TTL.getName()));
        String reason = asString(node.get(ThenKeys.REASON.getName()));
        return new ThenDef(url, ttl, reason);
    }
}
