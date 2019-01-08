package com.deep.pocket.dao.impl;

import com.deep.pocket.dao.PaymentDao;
import com.deep.pocket.model.NumberDateRange;
import com.deep.pocket.model.dao.Payment;
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
public class PaymentDaoImpl implements PaymentDao {

    private static final String PAYMENT_RELATION = "DEEP_POCKET.PAYMENT_INFO_TABLE";

    private final NamedParameterJdbcTemplate namedTemplate;

    private static final String INSERT_PAYMENT_ENTRY =
            "INSERT INTO " + PAYMENT_RELATION + " ( " +
                    " ID, ACC_ID, DATE_ADDED, EDITION, PAYER_EMAIL, PAYER_ID, PAYMENT_ID," +
                    " PAYMENT_SUCCESS, PURCHASED_NUMBER_COUNT " +
                    " ) " +
                    " VALUES( " +
                    " :ID, :ACC_ID, :DATE_ADDED, :EDITION, :PAYER_EMAIL, :PAYER_ID, :PAYMENT_ID," +
                    " :PAYMENT_SUCCESS, :PURCHASED_NUMBER_COUNT " +
                    " ) ";

    private static final String FETCH_PAYMENT_ENTRY_FROM_ACC =
            "SELECT * FROM " + PAYMENT_RELATION +
                    " WHERE ACC_ID = :ACC_ID " +
                    " AND EDITION = :EDITION ";

    private static final String FETCH_PAYMENT_ENTRY_FROM_ID =
            "SELECT * FROM " + PAYMENT_RELATION +
                    " WHERE ID = :ID " +
                    " AND EDITION = :EDITION";

    private static final String FETCH_PAYMENT_ENTRY_FROM_PAYER_EMAIL =
            "SELECT * FROM " + PAYMENT_RELATION +
                    " WHERE PAYER_EMAIL = :PAYER_EMAIL " +
                    " AND EDITION = :EDITION";

    @Autowired
    public PaymentDaoImpl(DataSource dataSource) {
        this.namedTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean savePayment(Payment paymentInfo, int edition) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ID", paymentInfo.getId());
        params.addValue("ACC_ID", paymentInfo.getAccId());
        params.addValue("DATE_ADDED", paymentInfo.getDateAdded());
        params.addValue("EDITION", edition);
        params.addValue("PAYER_EMAIL", paymentInfo.getPayerEmail());
        params.addValue("PAYER_ID", paymentInfo.getPayerId());
        params.addValue("PAYMENT_ID", paymentInfo.getPaymentId());
        params.addValue("PAYMENT_SUCCESS", paymentInfo.getPaymentSuccess());
        params.addValue("PURCHASED_NUMBER_COUNT", paymentInfo.getPurchasedNumberCount());
        int records = namedTemplate.update(INSERT_PAYMENT_ENTRY, params);
        return records > 0;

    }

    @Override
    public List<Payment> fetchPaymentByAccId(String accId, int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ACC_ID", accId);
        params.addValue("EDITION", edition);

        return namedTemplate.query(FETCH_PAYMENT_ENTRY_FROM_ACC, params, new PaymentDao.PaymentMapper());
    }

    @Override
    public List<Payment> fetchPaymentById(String id, int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ID", id);
        params.addValue("EDITION", edition);

        return namedTemplate.query(FETCH_PAYMENT_ENTRY_FROM_ID, params, new PaymentDao.PaymentMapper());
    }

    @Override
    public List<Payment> fetchPaymentByPayerEmail(String payerEmail, int edition) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("PAYER_EMAIL", payerEmail);
        params.addValue("EDITION", edition);

        return namedTemplate.query(FETCH_PAYMENT_ENTRY_FROM_PAYER_EMAIL, params, new PaymentDao.PaymentMapper());
    }

    public static class PaymentDao {

        private PaymentDao() {
        }

        public static class PaymentMapper implements RowMapper<Payment> {

            @Override
            public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
                Payment payment = new Payment();
                payment.setId(rs.getString("ID"));
                payment.setAccId(rs.getString("ACC_ID"));
                payment.setDateAdded(rs.getString("DATE_ADDED"));
                payment.setEdition(rs.getInt("EDITION"));
                payment.setPayerEmail(rs.getString("PAYER_EMAIL"));
                payment.setPayerId(rs.getString("PAYER_ID"));
                payment.setPaymentId(rs.getString("PAYMENT_ID"));
                payment.setPaymentSuccess(rs.getString("PAYMENT_SUCCESS"));
                payment.setPurchasedNumberCount(rs.getString("PURCHASED_NUMBER_COUNT"));

                return payment;
            }
        }
    }
}
