package com.deep.pocket.controller;

import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.request.AccountRequest;
import com.deep.pocket.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(value = "/accounts")
@Api(value = "accounts", tags = "accounts")
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "accounts")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public ResponseEntity getAccount() {
        LOGGER.info("get account {}");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "accounts")
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest account) {
        LOGGER.info("create account {}");

        Account response = accountService.createAccount(account);

        return ResponseEntity.created(URI.create("/lock")).body(response);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "accounts")
    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public ResponseEntity updateAccount(@RequestBody String account) {
        LOGGER.info("update account {}");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "accounts")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public ResponseEntity deleteAccount() {
        LOGGER.info("update account {}");

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
