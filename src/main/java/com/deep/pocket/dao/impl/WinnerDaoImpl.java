package com.deep.pocket.dao.impl;

import com.deep.pocket.dao.WinnerDao;
import com.deep.pocket.model.NumberDateRange;
import com.deep.pocket.model.dao.Edition;
import com.deep.pocket.model.dao.Payment;
import com.deep.pocket.model.dao.WinnerInfo;
import com.deep.pocket.model.dao.WinnerProfile;
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

@Repository
public class WinnerDaoImpl implements WinnerDao {

    private static final String WINNER_RELATION = "DEEP_POCKET.PREV_WINNER_INFO_TABLE";
    private static final String ACCOUNT_RELATION = "DEEP_POCKET.ACCOUNT_TABLE";
    private static final String PAYMENT_RELATION = "DEEP_POCKET.PAYMENT_INFO_TABLE";

    private final NamedParameterJdbcTemplate namedTemplate;

    private static final String INSERT_WINNER_ENTRY =
            "INSERT INTO " + WINNER_RELATION + " ( " +
                    " ACC_ID, PAYMENT_ID, WINNING_DATE, WINNING_NUMBER, EDITION " +
                    " ) " +
                    " VALUES( " +
                    " :ACC_ID, :PAYMENT_ID, :WINNING_DATE, :WINNING_NUMBER, :EDITION " +
                    " ) ";

    private static final String FETCH_WINNER_ENTRY =
            "SELECT ACC_ID FROM " + WINNER_RELATION +
                    " WHERE ACC_ID = :ACC_ID AND PAYMENT_ID = :PAYMENT_ID " +
                    " AND EDITION = :EDITION";

    private static final String FETCH_WINNER_ENTRY_ORDERED =
            "SELECT * FROM " + WINNER_RELATION +
                    " ORDER BY EDITION DESC";

    private static final String FETCH_LATEST_EDITION =
            "SELECT EDITION FROM " + WINNER_RELATION +
                    " ORDER BY EDITION DESC";

    private static final String FETCH_WINNER_ENTRY_FOR_DATE_RANGE =
            "SELECT * FROM " + WINNER_RELATION +
                    " WHERE EDITION = :EDITION";

    private static final String FETCH_WINNER_PROFILE_FOR_DATE_RANGE =
            "SELECT A.USERNAME, A.NAME, W.WINNING_NUMBER, W.WINNING_DATE, P.ID, P.PAYER_EMAIL, P.PAYER_ID, P.PAYMENT_ID, P.PAYMENT_SUCCESS, P.DATE_ADDED FROM "
                    + WINNER_RELATION + " W INNER JOIN " + ACCOUNT_RELATION + " A ON W.ACC_ID = A.UUID " +
                    " INNER JOIN " + PAYMENT_RELATION + " P ON P.ID = W.PAYMENT_ID" +
                    " WHERE W.EDITION = :EDITION ";

    private static final String FETCH_WINNER_PROFILE_FOR_DASHBOARD =
            "SELECT A.USERNAME, A.NAME, W.WINNING_NUMBER, W.WINNING_DATE, W.EDITION FROM "
                    + WINNER_RELATION + " W LEFT JOIN " + ACCOUNT_RELATION + " A ON W.ACC_ID = A.UUID " +
                    " WHERE W.EDITION = :EDITION ";

    @Autowired
    public WinnerDaoImpl(DataSource dataSource) {
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean checkWinnerRecordAvailable(String winnerId, String paymentId, Edition edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ACC_ID", winnerId);
        params.addValue("PAYMENT_ID", paymentId);
        params.addValue("DATE_ADDED_FROM", edition.getEditionFrom());
        params.addValue("DATE_ADDED_TO", edition.getEditionTo());
        params.addValue("EDITION", edition.getEdition());

        List winners = namedTemplate.queryForList(FETCH_WINNER_ENTRY, params, List.class);
        return CollectionUtils.isNotEmpty(winners);
    }

    @Override
    public WinnerInfo checkWinningNumberAvailableForDateRange(Edition edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition.getEdition());

        List<WinnerInfo> winners = namedTemplate.query(FETCH_WINNER_ENTRY_FOR_DATE_RANGE, params, new WinnerDao.WinnerInfoMapper());
        if (!CollectionUtils.isEmpty(winners)) {
            return winners.get(0);
        }
        return null;
    }

    @Override
    public boolean saveWinner(String winnerId, String paymentId, String winningNumber, String winningDate, int edition) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ACC_ID", winnerId);
        params.addValue("PAYMENT_ID", paymentId);
        params.addValue("WINNING_NUMBER", winningNumber);
        params.addValue("WINNING_DATE", winningDate);
        params.addValue("EDITION", edition);

        int records = namedTemplate.update(INSERT_WINNER_ENTRY, params);
        return records > 0;
    }

    @Override
    public List<WinnerProfile> fetchWinners(int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);

        return namedTemplate.query(FETCH_WINNER_PROFILE_FOR_DATE_RANGE, params, new WinnerDao.WinnerProfileMapper());
    }

    @Override
    public WinnerProfile fetchWinnerForDashboard(int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);

        List<WinnerProfile> winners = namedTemplate.query(FETCH_WINNER_PROFILE_FOR_DASHBOARD, params, new WinnerDao.WinnerDashboardMapper());

        if (!CollectionUtils.isEmpty(winners)){
            return winners.get(0);
        }

        return new WinnerProfile();
    }

    @Override
    public List<WinnerInfo> fetchLastEditionWinningInfo() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        return namedTemplate.query(FETCH_WINNER_ENTRY_ORDERED, params, new WinnerDao.WinnerInfoMapper());
    }

    @Override
    public int fetchEdition(){
        MapSqlParameterSource params = new MapSqlParameterSource();
        List edition = namedTemplate.queryForList(FETCH_LATEST_EDITION, params, List.class);

        if (CollectionUtils.isEmpty(edition)){
            return 0;
        }
        return (Integer) edition.get(0);
    }

    public static class WinnerDao {

        private WinnerDao() {
        }

        public static class WinnerProfileMapper implements RowMapper<WinnerProfile> {

            @Override
            public WinnerProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
                WinnerProfile winnerProfileDao = new WinnerProfile();

                Payment paymentDao = new Payment();
                paymentDao.setId(rs.getString("ID"));
                paymentDao.setPayerEmail(rs.getString("PAYER_EMAIL"));
                paymentDao.setPayerId(rs.getString("PAYER_ID"));
                paymentDao.setPaymentId(rs.getString("PAYMENT_ID"));
                paymentDao.setPaymentSuccess(rs.getString("PAYMENT_SUCCESS"));
                paymentDao.setDateAdded(rs.getString("DATE_ADDED"));

                winnerProfileDao.setUserName(rs.getString("USERNAME"));
                winnerProfileDao.setName(rs.getString("NAME"));
                winnerProfileDao.setWinningNumber(rs.getString("WINNING_NUMBER"));
                winnerProfileDao.setWinningDate(rs.getString("WINNING_DATE"));
                winnerProfileDao.setEdition(rs.getInt("EDITION"));
                winnerProfileDao.setPayment(paymentDao);

                return winnerProfileDao;
            }
        }

        public static class WinnerInfoMapper implements RowMapper<WinnerInfo> {

            @Override
            public WinnerInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                WinnerInfo winnerProfileDao = new WinnerInfo();

                winnerProfileDao.setAccId(rs.getString("ACC_ID"));
                winnerProfileDao.setPaymentId(rs.getString("PAYMENT_ID"));
                winnerProfileDao.setWinningNumber(rs.getString("WINNING_NUMBER"));
                winnerProfileDao.setWinningDate(rs.getString("WINNING_DATE"));
                winnerProfileDao.setEdition(rs.getInt("EDITION"));

                return winnerProfileDao;
            }
        }

        public static class WinnerDashboardMapper implements RowMapper<WinnerProfile> {

            @Override
            public WinnerProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
                WinnerProfile winnerProfileDao = new WinnerProfile();

                winnerProfileDao.setUserName(rs.getString("USERNAME"));
                winnerProfileDao.setName(rs.getString("NAME"));
                winnerProfileDao.setWinningNumber(rs.getString("WINNING_NUMBER"));
                winnerProfileDao.setWinningDate(rs.getString("WINNING_DATE"));
                winnerProfileDao.setEdition(rs.getInt("EDITION"));

                return winnerProfileDao;
            }
        }
    }
}
