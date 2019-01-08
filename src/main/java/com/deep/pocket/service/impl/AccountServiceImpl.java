package com.deep.pocket.service.impl;

import com.deep.pocket.dao.AccountDao;
import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.request.AccountRequest;
import com.deep.pocket.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public Account createAccount(AccountRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setUserName(request.getUserName());
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setContact(request.getContact());

        return accountDao.saveAccount(account) ? account : null;
    }

    @Override
    public Account fetchAccount(String userName) {
        return null;
    }
}
