package com.songoda.ultimatemoderation.moderate;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.moderations.FreezeModeration;
import com.songoda.ultimatemoderation.moderate.moderations.InvSeeModeration;
import com.songoda.ultimatemoderation.moderate.moderations.ReviveModeration;
import com.songoda.ultimatemoderation.moderate.moderations.SpyModeration;
import com.songoda.ultimatemoderation.moderate.moderations.ViewEnderChestModeration;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ModerationManager {
    private static final Map<ModerationType, AbstractModeration> moderations = new TreeMap<>();

    public ModerationManager(UltimateModeration plugin) {

        addAllModerations(new FreezeModeration(plugin),
                new ReviveModeration(plugin),
                new InvSeeModeration(plugin),
                new ViewEnderChestModeration(plugin),
                new SpyModeration(plugin));
    }

    public AbstractModeration addModeration(AbstractModeration moderation) {
        return moderations.put(moderation.getType(), moderation);
    }

    public void addAllModerations(AbstractModeration... moderations) {
        for (AbstractModeration moderation : moderations) {
            ModerationManager.moderations.put(moderation.getType(), moderation);
        }
    }

    public Map<ModerationType, AbstractModeration> getModerations() {
        return Collections.unmodifiableMap(moderations);
    }
}
