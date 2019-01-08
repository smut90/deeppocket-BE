package com.deep.pocket.dao.impl;

import com.deep.pocket.dao.NumberDao;
import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.dao.Number;
import com.deep.pocket.model.request.LockNumberRequest;
import com.deep.pocket.model.response.LockNumberResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class NumberDaoImpl implements NumberDao {

    private static final String NUMBER_RELATION = "DEEP_POCKET.NUMBER_TABLE";
    private static final String ACCOUNT_RELATION = "DEEP_POCKET.ACCOUNT_TABLE";

    private final NamedParameterJdbcTemplate namedTemplate;

    private static final String INSERT_NUMBER_ENTRY =
            "INSERT INTO " + NUMBER_RELATION + " ( " +
                    " DATE_ADDED, ACC_ID, " +
                    " NUMBER, PAYMENT_ID, EDITION " +
                    " ) " +
                    " VALUES( " +
                    " :DATE_ADDED, :ACC_ID, " +
                    " :NUMBER, :PAYMENT_ID, :EDITION " +
                    " ) ";

    private static final String FETCH_LOCKED_NUMBERS =
            "SELECT * FROM " + NUMBER_RELATION +
                    " WHERE EDITION = :EDITION ";

    private static final String FETCH_LOCKED_NUMBERS_FOR_USER =
            "SELECT * FROM " + NUMBER_RELATION +
                    " WHERE ACC_ID = :ACC_ID AND EDITION = :EDITION";

    private static final String CHECK_ALREADY_LOCKED =
            "SELECT * FROM " + NUMBER_RELATION +
                    " WHERE NUMBER IN (:NUMBERS) AND EDITION = :EDITION";

    private static final String FETCH_WINNING_ACC =
            "SELECT A.UUID, A.NAME, A.USERNAME, A.EMAIL, A.CONTACT FROM " + ACCOUNT_RELATION +
                    " A INNER JOIN " + NUMBER_RELATION + " N ON A.UUID = N.ACC_ID AND N.NUMBER = :WINNING_NUMBER " +
                    " AND N.EDITION = :EDITION";

    private static final String FETCH_WINNING_NUMBER =
            "SELECT * FROM " + NUMBER_RELATION +
                    " WHERE NUMBER = :WINNING_NUMBER AND EDITION = :EDITION ";


    private static final String CHECK_WINNING_NUMBER_AVAILABLE =
            "SELECT * FROM " + NUMBER_RELATION +
                    " WHERE EDITION = :EDITION";


    @Autowired
    public NumberDaoImpl(DataSource dataSource) {
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean lockNumbers(LockNumberRequest numberRequest, Account account, String paymentId, int edition) {
        List<Map<String, Object>> batchValues = new ArrayList<>();
        for (String no : numberRequest.getNumberList()) {
            batchValues.add(
                    new MapSqlParameterSource("DATE_ADDED", getFormattedDateTime())
                            .addValue("ACC_ID", account.getId())
                            .addValue("NUMBER", no)
                            .addValue("PAYMENT_ID", paymentId)
                            .addValue("EDITION", edition)
                            .getValues());
        }

        int[] updateCounts = namedTemplate.batchUpdate(INSERT_NUMBER_ENTRY,
                batchValues.toArray(new Map[numberRequest.getNumberList().size()]));

        return updateCounts.length > 0;
    }

    private String getFormattedDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(formatter);
    }

    @Override
    public List<Number> fetchLockedNumbers(int edition){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);

        return namedTemplate.query(FETCH_LOCKED_NUMBERS, params, new NumberDao.NumberMapper());
    }

    @Override
    public List<Number> fetchLockedNumbersForUser(String accId, int edition){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);
        params.addValue("ACC_ID", accId);

        return namedTemplate.query(FETCH_LOCKED_NUMBERS_FOR_USER, params, new NumberDao.NumberMapper());
    }

    @Override
    public Set<String> checkAlreadyLocked(Set<String> numbers, int edition){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);
        params.addValue("NUMBERS", numbers);

        List<Number> numberList = namedTemplate.query(CHECK_ALREADY_LOCKED, params, new NumberDao.NumberMapper());

        if (!CollectionUtils.isEmpty(numberList)){
            return numberList
                    .stream()
                    .map(Number::getNumber)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Override
    public Account fetchWinningAccount(String winningNumber, int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);
        params.addValue("WINNING_NUMBER", winningNumber);

        List<Account> accounts = namedTemplate.query(FETCH_WINNING_ACC,
                params,
                new AccountDaoImpl.AccountDao.AccountMapper());

        if (!CollectionUtils.isEmpty(accounts)) {
            return accounts.get(0);
        }
        return null;
    }

    @Override
    public Number fetchWinningNumber(String winningNumber, int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);
        params.addValue("WINNING_NUMBER", winningNumber);

        List<Number> numberList = namedTemplate.query(FETCH_WINNING_NUMBER, params, new NumberDao.NumberMapper());

        if (!CollectionUtils.isEmpty(numberList)) {
            return numberList.get(0);
        }
        return null;
    }

    @Override
    public Number checkWinningNumberAvailable(int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("EDITION", edition);

        List<Number> numberList = namedTemplate.query(CHECK_WINNING_NUMBER_AVAILABLE, params, new NumberDao.NumberMapper());

        if (!CollectionUtils.isEmpty(numberList)) {
            return numberList.get(0);
        }
        return null;
    }

    @Override
    public LockNumberResponse getLockedNumbers() {
        return null;
    }

    @Override
    public boolean deleteNumbers() {
        return false;
    }

    public static class NumberDao {

        private NumberDao() {
        }

        public static class NumberMapper implements RowMapper<Number> {

            @Override
            public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
                Number numberDao = new Number();
                numberDao.setDateAdded(rs.getString("DATE_ADDED"));
                numberDao.setAccId(rs.getString("ACC_ID"));
                numberDao.setNumber(rs.getString("NUMBER"));
                numberDao.setPaymentId(rs.getString("PAYMENT_ID"));
                numberDao.setEdition(rs.getInt("EDITION"));

                return numberDao;
            }
        }
    }
}
