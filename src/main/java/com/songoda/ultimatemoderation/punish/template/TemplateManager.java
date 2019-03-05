package com.songoda.ultimatemoderation.punish.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemplateManager {

    private static final Map<UUID, Template> templates = new HashMap<>();

    public Template getTemplate(String name) {
        for (Template template : templates.values()) {
            if (formatName(template.getTemplateName()).equals(formatName(name)))
                return template;
        }
        return null;
    }

    public Template addTemplate(Template template) {
        return templates.put(template.getUUID(), template);
    }

    public Template removeTemplate(UUID uuid) {
        return templates.remove(uuid);
    }

    public Template updateTemplate(UUID uuid, Template template) {
        templates.remove(uuid);
        return addTemplate(template);
    }

    public Map<UUID, Template> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    private String formatName(String name) {
        if (name == null) return null;
        name = name.toUpperCase().trim();
        name = name.replace(" ", "_");
        return name;
    }

}
