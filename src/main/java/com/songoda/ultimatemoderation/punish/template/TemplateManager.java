package com.songoda.ultimatemoderation.punish.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private static final Map<Integer, Template> templates = new HashMap<>();

    public Template getTemplate(String name) {
        for (Template template : templates.values()) {
            if (formatName(template.getName()).equals(formatName(name)))
                return template;
        }
        return null;
    }

    public Template addTemplate(Template template) {
        return templates.put(template.getId(), template);
    }

    public Template removeTemplate(Template template) {
        return templates.remove(template.getId());
    }

    public Template updateTemplate(int id, Template template) {
        templates.remove(id);
        return addTemplate(template);
    }

    public Map<Integer, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    private String formatName(String name) {
        if (name == null) return null;
        name = name.toUpperCase().trim();
        name = name.replace(" ", "_");
        return name;
    }
}
