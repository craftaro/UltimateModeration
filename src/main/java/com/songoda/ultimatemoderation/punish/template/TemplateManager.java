package com.songoda.ultimatemoderation.punish.template;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TemplateManager {
    private static final List<Template> templates = new LinkedList<>();

    public Template getTemplate(String name) {
        for (Template template : templates) {
            if (formatName(template.getName()).equals(formatName(name))) {
                return template;
            }
        }
        return null;
    }

    public Template addTemplate(Template template) {
        templates.add(template);
        return template;
    }

    public Template removeTemplate(Template template) {
        templates.remove(template);
        return template;
    }

    public List<Template> getTemplates() {
        return Collections.unmodifiableList(templates);
    }

    private String formatName(String name) {
        if (name == null) {
            return null;
        }
        name = name.toUpperCase().trim();
        name = name.replace(" ", "_");
        return name;
    }
}
