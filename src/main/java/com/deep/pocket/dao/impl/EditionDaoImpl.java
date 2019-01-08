package com.deep.pocket.dao.impl;

import com.deep.pocket.dao.EditionDao;
import com.deep.pocket.model.dao.Edition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class EditionDaoImpl implements EditionDao {

    private static final String EDITION_RELATION = "DEEP_POCKET.EDITION_TABLE";

    private final NamedParameterJdbcTemplate namedTemplate;

    private static final String INSERT_EDITION_ENTRY =
            "INSERT INTO " + EDITION_RELATION + " ( " +
                    " EDITION_FROM, EDITION_TO, EDITION ) " +
                    " VALUES( :EDITION_FROM, :EDITION_TO, :EDITION ) ";

    private static final String FETCH_EDITION_FROM_EDITION =
            "SELECT * FROM " + EDITION_RELATION +
                    " WHERE EDITION = :EDITION " +
                    " ORDER BY EDITION";

    private static final String FETCH_EDITION_FROM_DATE_RANGE =
            "SELECT * FROM " + EDITION_RELATION +
                    " WHERE EDITION_FROM = :EDITION_FROM AND EDITION_TO = :EDITION_TO " +
                    " ORDER BY EDITION ";

    private static final String FETCH_ALL =
            "SELECT * FROM " + EDITION_RELATION + " ORDER BY EDITION DESC";

    private static final String FETCH_ALL_LIMIT =
            "SELECT * FROM " + EDITION_RELATION + " ORDER BY EDITION DESC LIMIT :EDITION_LIMIT";

    @Autowired
    public EditionDaoImpl(DataSource dataSource) {
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean saveEdition(String from, String to, int edition) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION_FROM", from);
        params.addValue("EDITION_TO", to);
        params.addValue("EDITION", edition);

        int records = namedTemplate.update(INSERT_EDITION_ENTRY, params);
        return records > 0;

    }

    @Override
    public Edition fetchEditionFromDateRange(String from, String to) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION_FROM", from);
        params.addValue("EDITION_TO", to);

        List<Edition> editionList = namedTemplate.query(FETCH_EDITION_FROM_DATE_RANGE, params, new EditionDao.EditionMapper());

        if(!CollectionUtils.isEmpty(editionList)) {
            return editionList.get(0);
        }

        return null;
    }

    @Override
    public Edition fetchEditionFromEdition(int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);

        List<Edition> editionList = namedTemplate.query(FETCH_EDITION_FROM_EDITION, params, new EditionDao.EditionMapper());

        if(!CollectionUtils.isEmpty(editionList)) {
            return editionList.get(0);
        }

        return null;
    }

    @Override
    public Edition fetchLatestEdition() {
        List<Edition> editionList = fetchAllEditions();

        if(!CollectionUtils.isEmpty(editionList)) {
            return editionList.get(0);
        }
        return null;
    }

    @Override
    public List<Edition> fetchAllEditions() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<Edition> editionList = namedTemplate.query(FETCH_ALL, params, new EditionDao.EditionMapper());

        if(!CollectionUtils.isEmpty(editionList)) {
            return editionList;
        }

        return null;
    }

    @Override
    public List<Edition> fetchEditions(int numberOfEditions) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION_LIMIT", numberOfEditions);

        List<Edition> editionList = namedTemplate.query(FETCH_ALL_LIMIT, params, new EditionDao.EditionMapper());

        if(!CollectionUtils.isEmpty(editionList)) {
            return editionList;
        }

        return null;
    }

    public static class EditionDao {

        private EditionDao() {
        }

        public static class EditionMapper implements RowMapper<Edition> {

            @Override
            public Edition mapRow(ResultSet rs, int rowNum) throws SQLException {
                Edition editionDao = new Edition();
                editionDao.setEditionFrom(rs.getString("EDITION_FROM"));
                editionDao.setEditionTo(rs.getString("EDITION_TO"));
                editionDao.setEdition(rs.getInt("EDITION"));

                return editionDao;
            }
        }
    }
}
