package com.zenith.discord.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.discord.command.Command;
import com.zenith.discord.command.CommandContext;
import com.zenith.discord.command.CommandUsage;
import discord4j.rest.util.Color;

import java.util.List;

import static com.zenith.util.Constants.CONFIG;
import static com.zenith.util.Constants.DATABASE_MANAGER;
import static java.util.Arrays.asList;

public class DatabaseCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.full("database",
                "Configures what 2b2t server data is collected by the proxy. No database logs personal data.",
                asList(
                        "queue on/off",
                        "publicChat on/off",
                        "joinLeave on/off",
                        "deathMessages on/off"
                ),
                aliases());
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("database")
                .then(literal("queue")
                        .then(literal("on").executes(c -> {
                            CONFIG.database.queueWait.enabled = true;
                            DATABASE_MANAGER.startQueueWaitDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Queue Wait Database On!")
                                    .color(Color.CYAN);
                        }))
                        .then(literal("off").executes(c -> {
                            CONFIG.database.queueWait.enabled = false;
                            DATABASE_MANAGER.stopQueueWaitDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Queue Wait Database Off!")
                                    .color(Color.CYAN);
                        })))
                .then(literal("publicchat")
                        .then(literal("on").executes(c -> {
                            CONFIG.database.chats.enabled = true;
                            DATABASE_MANAGER.startChatsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Public Chat Database On!")
                                    .color(Color.CYAN);
                        }))
                        .then(literal("off").executes(c -> {
                            CONFIG.database.chats.enabled = false;
                            DATABASE_MANAGER.stopChatsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Public Chat Database Off!")
                                    .color(Color.CYAN);
                        })))
                .then(literal("joinleave")
                        .then(literal("on").executes(c -> {
                            CONFIG.database.connections.enabled = true;
                            DATABASE_MANAGER.startConnectionsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Join/Leave Connections Database On!")
                                    .color(Color.CYAN);
                        }))
                        .then(literal("off").executes(c -> {
                            CONFIG.database.connections.enabled = false;
                            DATABASE_MANAGER.stopConnectionsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Join/Leave Connections Database Off!")
                                    .color(Color.CYAN);
                        })))
                .then(literal("deathmessages")
                        .then(literal("on").executes(c -> {
                            CONFIG.database.deaths.enabled = true;
                            DATABASE_MANAGER.startDeathsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Death Messages Database On!")
                                    .color(Color.CYAN);
                        }))
                        .then(literal("off").executes(c -> {
                            CONFIG.database.deaths.enabled = false;
                            DATABASE_MANAGER.stopDeathsDatabase();
                            c.getSource().getEmbedBuilder()
                                    .title("Death Messages Database Off!")
                                    .color(Color.CYAN);
                        })));
    }

    @Override
    public List<String> aliases() {
        return asList("db");
    }
}
