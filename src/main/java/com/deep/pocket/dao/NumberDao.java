package com.deep.pocket.dao;

import com.deep.pocket.model.NumberDateRange;
import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.dao.Number;
import com.deep.pocket.model.request.LockNumberRequest;
import com.deep.pocket.model.response.LockNumberResponse;

import java.util.List;
import java.util.Set;

public interface NumberDao {

    /**
     * Save lock numbers
     *
     * @param numberRequest
     * @return
     */
    boolean lockNumbers(LockNumberRequest numberRequest, Account account, String paymentId, int edition);

    /**
     * check number already locked
     *
     * @return
     */
    List<Number> fetchLockedNumbers(int edition);

    /**
     * check number already locked
     *
     * @return
     */
    Set<String> checkAlreadyLocked(Set<String> numbers, int edition);


    List<Number> fetchLockedNumbersForUser(String accId, int edition);

    Account fetchWinningAccount(String winningNumber, int edition);

    Number fetchWinningNumber(String winningNumber, int edition);

    Number checkWinningNumberAvailable(int edition);

    /**
     * fetch list of locked numbers for a particular date
     *
     * @return
     */
    LockNumberResponse getLockedNumbers();

    /**
     * delete number entry
     *
     * @return
     */
    boolean deleteNumbers();

//    String getNow();
}
