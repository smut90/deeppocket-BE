package com.deep.pocket.dao;

import com.deep.pocket.model.dao.Account;

import java.util.List;

public interface AccountDao {

    /**
     * fetch user account info
     *
     * @param userName
     * @return
     */
    List<Account> fetchAccount(String userName);

    boolean saveAccount(Account account);

    boolean updateAccount();

    boolean deleteAccount();

}
