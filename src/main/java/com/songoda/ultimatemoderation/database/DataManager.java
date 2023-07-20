package com.songoda.ultimatemoderation.database;

import com.craftaro.core.database.DataManagerAbstract;
import com.craftaro.core.database.DatabaseConnector;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {
    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void createTemplate(Template template) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createTemplate = "INSERT INTO " + this.getTablePrefix() + "templates (punishment_type, duration, reason, name, creator) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createTemplate);
                statement.setString(1, template.getPunishmentType().name());
                statement.setLong(2, template.getDuration());
                statement.setString(3, template.getReason());
                statement.setString(4, template.getName());
                statement.setString(5, template.getCreator().toString());
                statement.executeUpdate();

                int templateId = this.lastInsertedId(connection, "templates");
                template.setId(templateId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteTemplate(Template template) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteTemplate = "DELETE FROM " + this.getTablePrefix() + "templates WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(deleteTemplate);
                statement.setLong(1, template.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getTemplates(Consumer<List<Template>> callback) {
        List<Template> templates = new ArrayList<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                Statement statement = connection.createStatement();
                String selectTemplates = "SELECT * FROM " + this.getTablePrefix() + "templates";
                ResultSet result = statement.executeQuery(selectTemplates);
                while (result.next()) {
                    int id = result.getInt("id");
                    PunishmentType punishmentType = PunishmentType.valueOf(result.getString("punishment_type"));
                    long duration = result.getLong("duration");
                    String reason = result.getString("reason");
                    String name = result.getString("name");
                    UUID creator = UUID.fromString(result.getString("creator"));
                    Template template = new Template(punishmentType, duration, reason, creator, name);
                    template.setId(id);
                    templates.add(template);
                }

                this.sync(() -> callback.accept(templates));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createAppliedPunishment(AppliedPunishment punishment) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createPunishment = "INSERT INTO " + this.getTablePrefix() + "punishments (type, duration, reason, victim, punisher, expiration) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createPunishment);
                statement.setString(1, punishment.getPunishmentType().name());
                statement.setLong(2, punishment.getDuration());
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getVictim().toString());
                statement.setString(5, punishment.getPunisher().toString());
                statement.setLong(6, punishment.getExpiration());
                statement.executeUpdate();

                int punishmentId = this.lastInsertedId(connection, "punishments");
                punishment.setId(punishmentId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteAppliedPunishment(AppliedPunishment punishment) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deletePunishment = "DELETE FROM " + this.getTablePrefix() + "punishments WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(deletePunishment);
                statement.setLong(1, punishment.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateAppliedPunishment(AppliedPunishment punishment) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String updatePunishment = "UPDATE " + this.getTablePrefix() + "punishments set type = ?, duration = ?, reason = ?, victim = ?, punisher = ?, expiration = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updatePunishment);
                statement.setString(1, punishment.getPunishmentType().name());
                statement.setLong(2, punishment.getDuration());
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getVictim().toString());
                statement.setString(5, punishment.getPunisher().toString());
                statement.setLong(6, punishment.getExpiration());
                statement.setLong(7, punishment.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getAppliedPunishments(Consumer<List<AppliedPunishment>> callback) {
        List<AppliedPunishment> appliedPunishments = new ArrayList<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                Statement statement = connection.createStatement();
                String selectPunishments = "SELECT * FROM " + this.getTablePrefix() + "punishments";
                ResultSet result = statement.executeQuery(selectPunishments);
                while (result.next()) {
                    int id = result.getInt("id");
                    PunishmentType punishmentType = PunishmentType.valueOf(result.getString("type"));
                    long duration = result.getLong("duration");
                    String reason = result.getString("reason");
                    UUID victim = UUID.fromString(result.getString("victim"));
                    UUID punisher = UUID.fromString(result.getString("punisher"));
                    long expiration = result.getLong("expiration");
                    appliedPunishments.add(new AppliedPunishment(punishmentType, duration, reason, victim, punisher, expiration, id));
                }

                this.sync(() -> callback.accept(appliedPunishments));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createNote(PunishmentNote note) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createNote = "INSERT INTO " + this.getTablePrefix() + "notes (note, author, subject, creation) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createNote);
                statement.setString(1, note.getNote());
                statement.setString(2, note.getAuthor().toString());
                statement.setString(3, note.getSubject().toString());
                statement.setLong(4, note.getCreationDate());
                statement.executeUpdate();

                int noteId = this.lastInsertedId(connection, "notes");
                note.setId(noteId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteNote(PunishmentNote note) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteNote = "DELETE FROM " + this.getTablePrefix() + "notes WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(deleteNote);
                statement.setLong(1, note.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getNotes(Consumer<List<PunishmentNote>> callback) {
        List<PunishmentNote> notes = new ArrayList<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                Statement statement = connection.createStatement();
                String getNotes = "SELECT * FROM " + this.getTablePrefix() + "notes";
                ResultSet result = statement.executeQuery(getNotes);
                while (result.next()) {
                    int id = result.getInt("id");
                    String note = result.getString("note");
                    UUID author = UUID.fromString(result.getString("author"));
                    UUID subject = UUID.fromString(result.getString("subject"));
                    long creation = result.getLong("creation");
                    notes.add(new PunishmentNote(id, note, author, subject, creation));
                }

                this.sync(() -> callback.accept(notes));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createTicket(Ticket ticket) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createTicket = "INSERT INTO " + this.getTablePrefix() + "tickets (victim, subject, type, status, world, x, y, z, pitch, yaw) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createTicket);
                statement.setString(1, ticket.getVictim().toString());
                statement.setString(2, ticket.getSubject());
                statement.setString(3, ticket.getType());
                statement.setString(4, ticket.getStatus().name());

                Location location = ticket.getLocation();

                statement.setString(5, location.getWorld().getName());
                statement.setDouble(6, location.getX());
                statement.setDouble(7, location.getY());
                statement.setDouble(8, location.getZ());
                statement.setFloat(9, location.getPitch());
                statement.setFloat(10, location.getYaw());
                statement.executeUpdate();

                for (TicketResponse response : ticket.getResponses()) {
                    createTicketResponse(response);
                }

                int ticketId = this.lastInsertedId(connection, "tickets");
                ticket.setId(ticketId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteTicket(Ticket ticket) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String deleteTicket = "DELETE FROM " + this.getTablePrefix() + "tickets WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteTicket)) {
                    statement.setLong(1, ticket.getId());
                    statement.executeUpdate();
                }

                String deleteTicketResponses = "DELETE FROM " + this.getTablePrefix() + "ticket_responses WHERE ticket_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteTicketResponses)) {
                    statement.setLong(1, ticket.getId());
                    statement.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateTicket(Ticket ticket) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String updateTicket = "UPDATE " + this.getTablePrefix() + "tickets SET victim = ?, subject = ?, type = ?, status = ?, world = ?, x = ?, y = ?, z = ?, pitch = ?, yaw = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updateTicket);
                statement.setString(1, ticket.getVictim().toString());
                statement.setString(2, ticket.getSubject());
                statement.setString(3, ticket.getType());
                statement.setString(4, ticket.getStatus().name());

                Location location = ticket.getLocation();

                statement.setString(5, location.getWorld().getName());
                statement.setDouble(6, location.getX());
                statement.setDouble(7, location.getY());
                statement.setDouble(8, location.getZ());
                statement.setFloat(9, location.getPitch());
                statement.setFloat(10, location.getYaw());
                statement.setInt(11, ticket.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getTickets(Consumer<Map<Integer, Ticket>> callback) {
        Map<Integer, Ticket> tickets = new TreeMap<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    String selectTickets = "SELECT * FROM " + this.getTablePrefix() + "tickets";
                    ResultSet result = statement.executeQuery(selectTickets);
                    while (result.next()) {
                        int id = result.getInt("id");
                        UUID victim = UUID.fromString(result.getString("victim"));
                        String subject = result.getString("subject");
                        String type = result.getString("type");
                        TicketStatus status = TicketStatus.valueOf(result.getString("status"));

                        String world = result.getString("world");
                        double x = result.getDouble("x");
                        double y = result.getDouble("y");
                        double z = result.getDouble("z");
                        float pitch = result.getFloat("pitch");
                        float yaw = result.getFloat("yaw");

                        Location location = Bukkit.getWorld(world) == null ? null : new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                        Ticket ticket = new Ticket(id, victim, subject, type, status, location);
                        ticket.setId(id)
                        ;
                        tickets.put(id, ticket);
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    String selectTickets = "SELECT * FROM " + this.getTablePrefix() + "ticket_responses";
                    ResultSet result = statement.executeQuery(selectTickets);
                    while (result.next()) {
                        int id = result.getInt("ticket_id");

                        Ticket ticket = tickets.get(id);
                        if (ticket == null) continue;

                        UUID author = UUID.fromString(result.getString("author"));
                        String message = result.getString("message");
                        long postedDate = result.getLong("posted_date");

                        TicketResponse ticketResponse = new TicketResponse(author, message, postedDate);
                        ticketResponse.setTicketId(id);

                        ticket.addResponse(ticketResponse);
                    }
                }

                this.sync(() -> callback.accept(tickets));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createTicketResponse(TicketResponse ticketResponse) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                String createTicketResponse = "INSERT INTO " + this.getTablePrefix() + "ticket_responses (ticket_id, author, message, posted_date) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createTicketResponse);
                statement.setInt(1, ticketResponse.getTicketId());
                statement.setString(2, ticketResponse.getAuthor().toString());
                statement.setString(3, ticketResponse.getMessage());
                statement.setLong(4, ticketResponse.getPostedDate());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
