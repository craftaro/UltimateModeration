package com.craftaro.ultimatemoderation.database;

import com.craftaro.core.database.DataManager;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.core.utils.TimeUtils;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.punish.AppliedPunishment;
import com.craftaro.ultimatemoderation.punish.Punishment;
import com.craftaro.ultimatemoderation.punish.PunishmentNote;
import com.craftaro.ultimatemoderation.punish.PunishmentType;
import com.craftaro.ultimatemoderation.punish.player.PlayerPunishData;
import com.craftaro.ultimatemoderation.punish.player.PunishmentManager;
import com.craftaro.ultimatemoderation.punish.template.Template;
import com.craftaro.ultimatemoderation.punish.template.TemplateManager;
import com.craftaro.ultimatemoderation.tickets.Ticket;
import com.craftaro.ultimatemoderation.tickets.TicketResponse;
import com.craftaro.ultimatemoderation.tickets.TicketStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

public class DataHelper {
    private final DatabaseConnector databaseConnector;
    private final DataManager dataManager;
    private final UltimateModeration plugin;

    //Store highest punishment id for each type, we query for new punishment ids regular to keep data up to date
    //Table, id
    private final HashMap<String, Integer> punishmentIds = new HashMap<>();
    public final List<String> TABLES = Arrays.asList("punishments", "templates", "notes", "tickets"); //TODO: Add ticket_responses?

    public DataHelper(DataManager dataManager, UltimateModeration plugin) {
        this.dataManager = dataManager;
        this.databaseConnector = dataManager.getDatabaseConnector();
        this.plugin = plugin;
    }

    private void runAsync(Runnable runnable) {
        this.dataManager.getAsyncPool().execute(runnable);
    }

    private void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

    private String getTablePrefix() {
        return this.dataManager.getTablePrefix();
    }

    private int getHighestPunishmentId(String table) {
        return this.punishmentIds.getOrDefault(table, 0);
    }

    private void setHighestPunishmentId(String table, int id) {
        this.punishmentIds.put(table, id);
    }

    //Method is called async, no need to make it async here
    public void updateData() {
        //We query all table ids to get the highest id for each punishment type
        for (String table : TABLES) {
            try (Connection connection = this.databaseConnector.getConnection()) {
                Statement statement = connection.createStatement();
                String selectHighestId = "SELECT MAX(id) FROM " + this.getTablePrefix() + table;
                ResultSet result = statement.executeQuery(selectHighestId);
                if (result.next()) {
                    int maxId = result.getInt(1);
                    //Check if the id is higher than the current highest id we cached
                    if (maxId > this.getHighestPunishmentId(table)) {
                        int oldId = this.getHighestPunishmentId(table);
                        this.setHighestPunishmentId(table, maxId);

                        //Load all new data
                        switch (table) {
                            case "punishments":
                                PreparedStatement punishmentStatement = connection.prepareStatement("SELECT * FROM " + this.getTablePrefix() + "punishments WHERE id > ?");
                                punishmentStatement.setInt(1, oldId);
                                ResultSet punishmentResult = punishmentStatement.executeQuery();
                                while (punishmentResult.next()) {
                                    int id = punishmentResult.getInt("id");
                                    PunishmentType punishmentType = PunishmentType.valueOf(punishmentResult.getString("type"));
                                    long duration = punishmentResult.getLong("duration");
                                    String reason = punishmentResult.getString("reason");
                                    UUID victim = UUID.fromString(punishmentResult.getString("victim"));
                                    UUID punisher = UUID.fromString(punishmentResult.getString("punisher"));
                                    long expiration = punishmentResult.getLong("expiration");
                                    AppliedPunishment punishment = new AppliedPunishment(punishmentType, duration, reason, victim, punisher, expiration, id);
                                    plugin.getPunishmentManager().getPlayer(punishment.getVictim()).addPunishment(punishment);

                                    //If punishment is a BAN check if the player is online and kick them
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(punishment.getVictim());
                                    switch (punishmentType) {
                                        case BAN:
                                            if (offlinePlayer.isOnline()) {
                                                Bukkit.getScheduler().runTask(plugin, () -> offlinePlayer.getPlayer().kickPlayer(plugin.getLocale()
                                                        .getMessage("event.ban.message")
                                                        .processPlaceholder("reason", reason == null ? "" : reason)
                                                        .processPlaceholder("duration", TimeUtils.makeReadable(duration)).getMessage()));
                                            }
                                            break;
                                        case KICK:
                                            if (offlinePlayer.isOnline()) {
                                                Bukkit.getScheduler().runTask(plugin, () -> offlinePlayer.getPlayer().kickPlayer(plugin.getLocale()
                                                        .getMessage("event.kick.message")
                                                        .processPlaceholder("reason", reason == null ? "" : reason).getMessage()));
                                            }
                                            break;
                                        case MUTE:
                                            PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(offlinePlayer);
                                            if (!playerPunishData.getActivePunishments(PunishmentType.MUTE).isEmpty()) {
                                                return;
                                            }
                                            sendMessage(offlinePlayer, punishment);
                                            break;
                                        case WARNING:
                                            sendMessage(offlinePlayer, punishment);
                                    }
                                }
                                break;
                            case "templates":
                                PreparedStatement templateStatement = connection.prepareStatement("SELECT * FROM " + this.getTablePrefix() + "templates WHERE id > ?");
                                templateStatement.setInt(1, oldId);
                                ResultSet templateResult = templateStatement.executeQuery();
                                while (templateResult.next()) {
                                    int id = templateResult.getInt("id");
                                    PunishmentType punishmentType = PunishmentType.valueOf(templateResult.getString("punishment_type"));
                                    long duration = templateResult.getLong("duration");
                                    String reason = templateResult.getString("reason");
                                    String name = templateResult.getString("name");
                                    UUID creator = UUID.fromString(templateResult.getString("creator"));
                                    Template template = new Template(punishmentType, duration, reason, creator, name, id);
                                    plugin.getTemplateManager().addTemplate(template);
                                }
                                break;
                            case "notes":
                                PreparedStatement noteStatement = connection.prepareStatement("SELECT * FROM " + this.getTablePrefix() + "notes WHERE id > ?");
                                noteStatement.setInt(1, oldId);
                                ResultSet noteResult = noteStatement.executeQuery();
                                while (noteResult.next()) {
                                    int id = noteResult.getInt("id");
                                    String noteString = noteResult.getString("note");
                                    UUID author = UUID.fromString(noteResult.getString("author"));
                                    UUID subject = UUID.fromString(noteResult.getString("subject"));
                                    long creation = noteResult.getLong("creation");
                                    PunishmentNote note = new PunishmentNote(id, noteString, author, subject, creation);
                                    plugin.getPunishmentManager().getPlayer(note.getSubject()).addNotes(note);
                                }
                                break;
                            case "tickets":
                                PreparedStatement ticketStatement = connection.prepareStatement("SELECT * FROM " + this.getTablePrefix() + "tickets WHERE id > ?");
                                ticketStatement.setInt(1, oldId);
                                ResultSet ticketResult = ticketStatement.executeQuery();
                                while (ticketResult.next()) {
                                    int id = ticketResult.getInt("id");
                                    UUID victim = UUID.fromString(ticketResult.getString("victim"));
                                    String subject = ticketResult.getString("subject");
                                    String type = ticketResult.getString("type");
                                    TicketStatus status = TicketStatus.valueOf(ticketResult.getString("status"));

                                    String world = ticketResult.getString("world");
                                    double x = ticketResult.getDouble("x");
                                    double y = ticketResult.getDouble("y");
                                    double z = ticketResult.getDouble("z");
                                    float pitch = ticketResult.getFloat("pitch");
                                    float yaw = ticketResult.getFloat("yaw");

                                    Location location = Bukkit.getWorld(world) == null ? null : new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

                                    Ticket ticket = new Ticket(id, victim, subject, type, status, location);
                                    ticket.setId(id);

                                    //Query ticket responses for the ticket and load them if they exist
                                    PreparedStatement responseStatement = connection.prepareStatement("SELECT * FROM " + this.getTablePrefix() + "ticket_responses WHERE ticket_id = ?");
                                    responseStatement.setInt(1, id);
                                    ResultSet responseResult = responseStatement.executeQuery();
                                    while (responseResult.next()) {
                                        UUID author = UUID.fromString(responseResult.getString("author"));
                                        String message = responseResult.getString("message");
                                        long postedDate = responseResult.getLong("posted_date");
                                        TicketResponse ticketResponse = new TicketResponse(author, message, postedDate);
                                        ticketResponse.setTicketId(id);
                                        ticket.addResponse(ticketResponse);
                                    }

                                    plugin.getTicketManager().addTicket(ticket);
                                }
                                break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendMessage(OfflinePlayer offlineVictim, Punishment punishment) {
        if (!offlineVictim.isOnline()) {
            return;
        }
        Player victim = offlineVictim.getPlayer();
        UltimateModeration plugin = UltimateModeration.getInstance();

        String punishSuccess = plugin.getLocale()
                .getMessage("event." + punishment.getPunishmentType().name().toLowerCase() + ".message").getPrefixedMessage();

        if (punishment.getReason() != null) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason")
                    .processPlaceholder("reason", punishment.getReason()).getMessage();
        }

        if (punishment.getDuration() != -1) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.yourduration")
                    .processPlaceholder("duration", TimeUtils.makeReadable(punishment.getDuration())).getMessage();
        }

        victim.sendMessage(punishSuccess + TextUtils.formatText("&7."));
    }

    public void createTemplate(Template template) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                int nextId = this.dataManager.getNextId("templates");

                String createTemplate = "INSERT INTO " + this.getTablePrefix() + "templates (punishment_type, duration, reason, name, creator) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createTemplate);
                statement.setString(1, template.getPunishmentType().name());
                statement.setLong(2, template.getDuration());
                statement.setString(3, template.getReason());
                statement.setString(4, template.getName());
                statement.setString(5, template.getCreator().toString());
                statement.executeUpdate();

                template.setId(nextId);
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

                //Get the highest id for the table and cache it
                this.setHighestPunishmentId("templates", templates.stream().mapToInt(Template::getId).max().orElse(0));

                this.sync(() -> callback.accept(templates));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createAppliedPunishment(AppliedPunishment punishment) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                int nextId = this.dataManager.getNextId("punishments");

                String createPunishment = "INSERT INTO " + this.getTablePrefix() + "punishments (type, duration, reason, victim, punisher, expiration) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createPunishment);
                statement.setString(1, punishment.getPunishmentType().name());
                statement.setLong(2, punishment.getDuration());
                statement.setString(3, punishment.getReason());
                statement.setString(4, punishment.getVictim().toString());
                statement.setString(5, punishment.getPunisher().toString());
                statement.setLong(6, punishment.getExpiration());
                statement.executeUpdate();

                punishment.setId(nextId);
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

                //Get the highest id for the table and cache it
                this.setHighestPunishmentId("punishments", appliedPunishments.stream().mapToInt(AppliedPunishment::getId).max().orElse(0));

                this.sync(() -> callback.accept(appliedPunishments));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createNote(PunishmentNote note) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                int nextId = this.dataManager.getNextId("notes");

                String createNote = "INSERT INTO " + this.getTablePrefix() + "notes (note, author, subject, creation) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createNote);
                statement.setString(1, note.getNote());
                statement.setString(2, note.getAuthor().toString());
                statement.setString(3, note.getSubject().toString());
                statement.setLong(4, note.getCreationDate());
                statement.executeUpdate();

                note.setId(nextId);
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

                //Get the highest id for the table and cache it
                this.setHighestPunishmentId("notes", notes.stream().mapToInt(PunishmentNote::getId).max().orElse(0));

                this.sync(() -> callback.accept(notes));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createTicket(Ticket ticket) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()) {
                int nextId = this.dataManager.getNextId("tickets");

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

                ticket.setId(nextId);
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

                if (location != null) {
                    statement.setString(5, location.getWorld().getName());
                    statement.setDouble(6, location.getX());
                    statement.setDouble(7, location.getY());
                    statement.setDouble(8, location.getZ());
                    statement.setFloat(9, location.getPitch());
                    statement.setFloat(10, location.getYaw());
                } else {
                    statement.setString(5, "");
                    statement.setDouble(6, 0);
                    statement.setDouble(7, 0);
                    statement.setDouble(8, 0);
                    statement.setFloat(9, 0);
                    statement.setFloat(10, 0);
                }

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
                        ticket.setId(id);

                        tickets.put(id, ticket);
                    }

                    //Get the highest id for the table and cache it
                    this.setHighestPunishmentId("tickets", tickets.keySet().stream().max(Integer::compareTo).orElse(0));
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
