package com.deep.pocket.controller;

import com.deep.pocket.exception.DeepPocketException;
import com.deep.pocket.model.dao.Edition;
import com.deep.pocket.model.request.LockNumberRequest;
import com.deep.pocket.model.response.AlreadyBookedNumberResponse;
import com.deep.pocket.model.response.FastTrackNumberResponse;
import com.deep.pocket.model.response.LockNumberResponse;
import com.deep.pocket.model.response.NextDraw;
import com.deep.pocket.model.response.NumberResponse;
import com.deep.pocket.model.response.WinnerHistory;
import com.deep.pocket.model.response.WinnerHistoryForDashboard;
import com.deep.pocket.model.response.WinningNumber;
import com.deep.pocket.service.NumberService;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping(value = "/numbers", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "numbers", tags = "numbers")
public class NumbersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumbersController.class);

    @Autowired
    private NumberService numberService;

//    private static final String FE_URL = "http://ec2-3-86-106-25.compute-1.amazonaws.com:5000";
    private static final String FE_URL = "http://localhost:3000";

    /**
     * 0. Create Account
     * 1. Get numbers 0000 - 9999 | 00000 - 99999
     * 2. Lock 0-50 number 1234 | 12345
     */

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Generate Numbers")
    @RequestMapping(method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public NumberResponse generateNumbers(@RequestParam("digits") int digits) {
        LOGGER.info("Request received to fetch numbers. digits {}", digits);
        return numberService.generateNumbers(digits);
    }


    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Generate Fast Track Numbers")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/fasttrack", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public FastTrackNumberResponse generateFastTrackNumbers(@RequestParam("userName") String userName, int numbersToGenerate) {
        LOGGER.info("Request received to generate fast track numbers. user {} | digits {}", userName, numbersToGenerate);
        return numberService.generateFastTrackNumbers(userName, numbersToGenerate);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Get Locked Numbers")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/lock", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public LockNumberResponse getLockedNumbers(@RequestParam(required = false, value = "userName") String userName) {
        LOGGER.info("Request received to fetch lock numbers. userName {}", userName);
        return numberService.fetchLockedNumbers(userName);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Get Locked Numbers")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/lock/allowed", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public AlreadyBookedNumberResponse isBookingAllowed(@RequestParam(value = "userName") String userName) {
        LOGGER.info("Request received to fetch is Booking allowed. userName {}", userName);
        return numberService.isBookingAllowed(userName);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Get temp Locked Numbers")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/lock/cache", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public Set<String> getTempLockedNumbers() {
        LOGGER.info("Request received to fetch temp lock numbers.");
        return numberService.fetchTempLockedNumbers();
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Lock Numbers")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/lock", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public ResponseEntity<LockNumberResponse> lockNumbers(@RequestBody LockNumberRequest lockNumberRequest) throws DeepPocketException {
        LOGGER.info("Request received to lock numbers {}", lockNumberRequest);

        LockNumberResponse response = numberService.lockNumbers(lockNumberRequest);
        return ResponseEntity.created(URI.create("/lock")).body(response);
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Fetch last edition Winning number and account")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/winners/prev", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public WinningNumber lastEditionWinner() {
        LOGGER.info("Request received to fetch last edition winner");

        WinningNumber winningAcc = numberService.lastEditionWinner();

        LOGGER.info("Last edition winning account info {}", winningAcc);
        return winningAcc;
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Fetch winner history with payment info")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/winners/history", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public WinnerHistory WinnerHistory(@RequestParam(value = "dateFrom") String dateFrom,
                                       @RequestParam(value = "dateTo") String dateTo) {
        LOGGER.info("Request received to fetch winner history, dateFrom: {} | dateTo: {}", dateFrom, dateTo);

        WinnerHistory winnerHistory = numberService.winnerHistory(dateFrom, dateTo);

        LOGGER.info("Last edition winning account info {}", winnerHistory);
        return winnerHistory;
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Fetch winner history for Dashboard")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/winners/history/dashboard", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public WinnerHistoryForDashboard WinnerHistoryForDashboard() {
        LOGGER.info("Request received to fetch winner history for dashboard");

        WinnerHistoryForDashboard winnerHistory = numberService.winnerHistoryForDashboard();

        LOGGER.info("Last edition winning account info {}", winnerHistory);
        return winnerHistory;
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Fetch next draw date time")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/winners/draw", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public NextDraw fetchNextDrawTime() {
        LOGGER.info("Request received to fetch next draw time");

        NextDraw nextDrawDateTime = numberService.nextDrawDateTime();

        LOGGER.info("Next draw date time {}", nextDrawDateTime);
        return nextDrawDateTime;
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Fetch next draw date time")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/edition", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public Edition fetchCurrentEdition() {
        LOGGER.info("Request received to fetch current edition");

        Edition currentEdition = numberService.fetchCurrentEdition();

        LOGGER.info("Current edition {}", currentEdition);
        return currentEdition;
    }

    /**
     *
     * @return ResponseEntity<>
     */
    @ApiOperation(value = "Select Winning number and account")
    @CrossOrigin(origins = FE_URL)
    @RequestMapping(value = "/winners", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Order notification signature invalid error"),
            @ApiResponse(code = 404, message = "Order not found"),
            @ApiResponse(code = 500, message = "Unexpected error consuming order notification"),
    })
    public WinningNumber winner(@RequestParam("digits") int digits) {
        LOGGER.info("Request received to generate winning numbers");

        WinningNumber winningAcc = numberService.generateWinningNumbers(digits);

        LOGGER.info("Winning account info {}", winningAcc);
        return winningAcc;
    }

}
