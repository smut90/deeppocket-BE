package com.deep.pocket.dao;

import com.deep.pocket.model.dao.Payment;

import java.util.List;

public interface PaymentDao {

    boolean savePayment(Payment paymentInfo, int edition);

    List<Payment> fetchPaymentByAccId(String accId, int edition);

    List<Payment> fetchPaymentById(String id, int edition);

    List<Payment> fetchPaymentByPayerEmail(String payerEmail, int edition);
}
