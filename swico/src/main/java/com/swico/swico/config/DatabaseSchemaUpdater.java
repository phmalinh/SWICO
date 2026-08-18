package com.swico.swico.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("!test")
public class DatabaseSchemaUpdater {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void updateSchema() {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS product_process ALTER COLUMN line_code TYPE varchar(1000)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS product_process ALTER COLUMN machine_code TYPE varchar(1000)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS product_process ALTER COLUMN process TYPE varchar(1000)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS daily_production_reports ADD COLUMN IF NOT EXISTS responsibility numeric(6,4)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS daily_production_reports ALTER COLUMN responsibility TYPE numeric(6,4) USING NULLIF(responsibility::text, '')::numeric");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS daily_production_reports ADD COLUMN IF NOT EXISTS deduction_percent numeric(6,4)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS daily_production_reports ALTER COLUMN deduction_percent TYPE numeric(6,4)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS daily_production_reports ADD COLUMN IF NOT EXISTS responsible_leader varchar(100)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS employee_skills ADD COLUMN IF NOT EXISTS team varchar(100)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS employee_skills ADD COLUMN IF NOT EXISTS user_id bigint");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS employee_skills ADD COLUMN IF NOT EXISTS product_id bigint");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS employee_skills ADD COLUMN IF NOT EXISTS process_id bigint");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS downtime_reasons ADD COLUMN IF NOT EXISTS reason_category_code varchar(20)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS job_title varchar(255)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS team varchar(100)");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS hire_date date");
    }
}
