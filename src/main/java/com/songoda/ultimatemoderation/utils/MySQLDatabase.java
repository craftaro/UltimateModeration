package com.songoda.ultimatemoderation.utils;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.storage.StorageItem;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class MySQLDatabase {

    private final UltimateModeration instance;

    private Connection connection;

    public MySQLDatabase(UltimateModeration instance) {
        this.instance = instance;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + instance.getConfig().getString("Database.IP") + ":" + instance.getConfig().getString("Database.Port") + "/" + instance.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, instance.getConfig().getString("Database.Username"), instance.getConfig().getString("Database.Password"));

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    private void createTables() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "templates` (\n" +
                    "\t`uuid` TEXT NULL,\n" +
                    "\t`type` TEXT NULL,\n" +
                    "\t`duration` BIGINT NULL,\n" +
                    "\t`reason` TEXT NULL,\n" +
                    "\t`name` TEXT NULL,\n" +
                    "\t`creator` TEXT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "punishments` (\n" +
                    "\t`uuid` TEXT NULL,\n" +
                    "\t`type` TEXT NULL,\n" +
                    "\t`duration` BIGINT NULL,\n" +
                    "\t`reason` TEXT NULL,\n" +
                    "\t`victim` TEXT NULL,\n" +
                    "\t`punisher` TEXT NULL,\n" +
                    "\t`expiration` BIGINT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "notes` (\n" +
                    "\t`uuid` TEXT NULL,\n" +
                    "\t`note` TEXT NULL,\n" +
                    "\t`author` TEXT NULL,\n" +
                    "\t`subject` TEXT NULL,\n" +
                    "\t`creation` BIGINT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "tickets` (\n" +
                    "\t`id` INT NULL,\n" +
                    "\t`player` TEXT NULL,\n" +
                    "\t`subject` TEXT NULL,\n" +
                    "\t`status` TEXT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "ticketresponses` (\n" +
                    "\t`posted` BIGINT NULL,\n" +
                    "\t`ticketid` INT NULL,\n" +
                    "\t`author` TEXT NULL,\n" +
                    "\t`message` TEXT NULL\n" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}