package com.songoda.ultimatemoderation.punish.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private static final Map<String, Template> templates = new HashMap<>();

    public Template getTemplate(String name) {
        return templates.get(formatName(name));
    }

    public Template addTemplate(Template template) {
        return templates.put(formatName(template.getTemplateName()), template);
    }

    public Template removeTemplate(String name) {
        return templates.remove(formatName(name));
    }

    public Template updateTemplate(Template template) {
        templates.remove(formatName(template.getTemplateName()));
        return addTemplate(template);
    }

    public Map<String, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    private String formatName(String name) {
        name = name.toUpperCase().trim();
        name = name.replace(" ", "_");
        return name;
    }

}
