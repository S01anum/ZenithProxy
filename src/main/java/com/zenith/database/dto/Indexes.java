/*
 * This file is generated by jOOQ.
 */
package com.zenith.database.dto;


import com.zenith.database.dto.tables.Connections;
import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in public.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index CONNECTIONS_PLAYER_UUID_IDX = Internal.createIndex(DSL.name("connections_player_uuid_idx"), Connections.CONNECTIONS, new OrderField[]{Connections.CONNECTIONS.PLAYER_UUID}, false);
}