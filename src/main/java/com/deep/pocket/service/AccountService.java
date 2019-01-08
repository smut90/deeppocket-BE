package com.deep.pocket.service;

import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.request.AccountRequest;

public interface AccountService {

    /**
     * save account
     */
    Account createAccount(AccountRequest account);

    /**
     * fetch account for given username
     *
     * @param userName
     * @return
     */
    Account fetchAccount(String userName);
}
