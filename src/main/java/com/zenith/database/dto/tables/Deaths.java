/*
 * This file is generated by jOOQ.
 */
package com.zenith.database.dto.tables;


import com.zenith.database.dto.Public;
import com.zenith.database.dto.tables.records.DeathsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Deaths extends TableImpl<DeathsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.deaths</code>
     */
    public static final Deaths DEATHS = new Deaths();
    /**
     * The column <code>public.deaths.killer_mob</code>.
     */
    public final TableField<DeathsRecord, String> KILLER_MOB = createField(DSL.name("killer_mob"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.deaths.time</code>.
     */
    public final TableField<DeathsRecord, OffsetDateTime> TIME = createField(DSL.name("time"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false), this, "");

    /**
     * The column <code>public.deaths.death_message</code>.
     */
    public final TableField<DeathsRecord, String> DEATH_MESSAGE = createField(DSL.name("death_message"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.deaths.victim_player_name</code>.
     */
    public final TableField<DeathsRecord, String> VICTIM_PLAYER_NAME = createField(DSL.name("victim_player_name"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.deaths.victim_player_uuid</code>.
     */
    public final TableField<DeathsRecord, UUID> VICTIM_PLAYER_UUID = createField(DSL.name("victim_player_uuid"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.deaths.killer_player_name</code>.
     */
    public final TableField<DeathsRecord, String> KILLER_PLAYER_NAME = createField(DSL.name("killer_player_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.deaths.killer_player_uuid</code>.
     */
    public final TableField<DeathsRecord, UUID> KILLER_PLAYER_UUID = createField(DSL.name("killer_player_uuid"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.deaths.weapon_name</code>.
     */
    public final TableField<DeathsRecord, String> WEAPON_NAME = createField(DSL.name("weapon_name"), SQLDataType.CLOB, this, "");

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DeathsRecord> getRecordType() {
        return DeathsRecord.class;
    }

    private Deaths(Name alias, Table<DeathsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Deaths(Name alias, Table<DeathsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.deaths</code> table reference
     */
    public Deaths(String alias) {
        this(DSL.name(alias), DEATHS);
    }

    /**
     * Create an aliased <code>public.deaths</code> table reference
     */
    public Deaths(Name alias) {
        this(alias, DEATHS);
    }

    /**
     * Create a <code>public.deaths</code> table reference
     */
    public Deaths() {
        this(DSL.name("deaths"), null);
    }

    public <O extends Record> Deaths(Table<O> child, ForeignKey<O, DeathsRecord> key) {
        super(child, key, DEATHS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Deaths as(String alias) {
        return new Deaths(DSL.name(alias), this);
    }

    @Override
    public Deaths as(Name alias) {
        return new Deaths(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Deaths rename(String name) {
        return new Deaths(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Deaths rename(Name name) {
        return new Deaths(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<OffsetDateTime, String, String, UUID, String, UUID, String, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}