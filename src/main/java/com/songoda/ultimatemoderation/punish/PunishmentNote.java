package com.songoda.ultimatemoderation.punish;

import java.util.UUID;

public class PunishmentNote {
    private int id;

    private final String note;
    private final UUID author;
    private final UUID subject;
    private final long creationDate;

    public PunishmentNote(int id, String note, UUID author, UUID subject, long creationDate) {
        this.id = id;
        this.note = note;
        this.author = author;
        this.subject = subject;
        this.creationDate = creationDate;
    }

    public PunishmentNote(String note, UUID author, UUID subject, long creationDate) {
        this.note = note;
        this.author = author;
        this.subject = subject;
        this.creationDate = creationDate;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return this.note;
    }

    public UUID getAuthor() {
        return this.author;
    }

    public UUID getSubject() {
        return this.subject;
    }

    public long getCreationDate() {
        return this.creationDate;
    }
}
