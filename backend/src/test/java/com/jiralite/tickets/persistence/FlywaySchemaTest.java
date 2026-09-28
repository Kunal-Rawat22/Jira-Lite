package com.jiralite.tickets.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.jiralite.tickets.IntegrationTest;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class FlywaySchemaTest extends IntegrationTest {
    @Autowired
    DataSource dataSource;

    @Test
    void tablesExist() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Integer products = jdbc.queryForObject("select count(*) from products", Integer.class);
        assertThat(products).isGreaterThanOrEqualTo(1);
    }
}
