package com.deep.pocket.dao.impl;

import com.deep.pocket.model.dao.Account;
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
public class AccountDaoImpl implements com.deep.pocket.dao.AccountDao {

    private static final String ACCOUNT_RELATION = "DEEP_POCKET.ACCOUNT_TABLE";

    private final NamedParameterJdbcTemplate namedTemplate;

    private static final String INSERT_ACCOUNT_ENTRY =
            "INSERT INTO " + ACCOUNT_RELATION + " ( " +
                    " UUID, NAME, USERNAME, " +
                    " EMAIL, CONTACT " +
                    " ) " +
                    " VALUES( " +
                    " :UUID, :NAME, :USERNAME, " +
                    " :EMAIL, :CONTACT " +
                    " ) ";

    private static final String FETCH_ACCOUNT_ENTRY =
            "SELECT * FROM " + ACCOUNT_RELATION +
                    " WHERE USERNAME = :USERNAME";

    @Autowired
    public AccountDaoImpl(DataSource dataSource) {
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<Account> fetchAccount(String userName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("USERNAME", userName);

        return namedTemplate.query(FETCH_ACCOUNT_ENTRY, params, new AccountDao.AccountMapper());
    }

    @Override
    public boolean saveAccount(Account account) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("UUID", account.getId());
        params.addValue("NAME", account.getName());
        params.addValue("USERNAME", account.getUserName());
        params.addValue("EMAIL", account.getEmail());
        params.addValue("CONTACT", account.getContact());
        int records = namedTemplate.update(INSERT_ACCOUNT_ENTRY, params);
        return records > 0;
    }

    @Override
    public boolean updateAccount() {
        return false;
    }

    @Override
    public boolean deleteAccount() {
        return false;
    }

    public static class AccountDao {

        private AccountDao() {
        }

        public static class AccountMapper implements RowMapper<Account> {

            @Override
            public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
                Account accountDao = new Account();
                accountDao.setId(rs.getString("UUID"));
                accountDao.setName(rs.getString("NAME"));
                accountDao.setUserName(rs.getString("USERNAME"));
                accountDao.setEmail(rs.getString("EMAIL"));
                accountDao.setContact(rs.getString("CONTACT"));

                return accountDao;
            }
        }
    }
}
