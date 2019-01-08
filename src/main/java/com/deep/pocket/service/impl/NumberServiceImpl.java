package com.deep.pocket.service.impl;

import com.deep.pocket.controller.NumbersController;
import com.deep.pocket.dao.AccountDao;
import com.deep.pocket.dao.EditionDao;
import com.deep.pocket.dao.NumberDao;
import com.deep.pocket.dao.PaymentDao;
import com.deep.pocket.dao.WinnerDao;
import com.deep.pocket.exception.DeepPocketException;
import com.deep.pocket.model.NumberDateRange;
import com.deep.pocket.model.dao.Account;
import com.deep.pocket.model.dao.Edition;
import com.deep.pocket.model.dao.Number;
import com.deep.pocket.model.dao.Payment;
import com.deep.pocket.model.dao.WinnerInfo;
import com.deep.pocket.model.dao.WinnerProfile;
import com.deep.pocket.model.request.AccountRequest;
import com.deep.pocket.model.request.LockNumberRequest;
import com.deep.pocket.model.response.AlreadyBookedNumberResponse;
import com.deep.pocket.model.response.FastTrackNumberResponse;
import com.deep.pocket.model.response.LockNumberResponse;
import com.deep.pocket.model.response.NextDraw;
import com.deep.pocket.model.response.NumberResponse;
import com.deep.pocket.model.response.WinnerHistory;
import com.deep.pocket.model.response.WinnerHistoryForDashboard;
import com.deep.pocket.model.response.WinnerInfoForDashboard;
import com.deep.pocket.model.response.WinningNumber;
import com.deep.pocket.service.AccountService;
import com.deep.pocket.service.NumberService;
import com.sun.javafx.binding.StringFormatter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NumberServiceImpl implements NumberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberServiceImpl.class);

    private Set<String> tempLockedNumbers = new HashSet<>();

    @Autowired
    private NumberDao numberDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private WinnerDao winnerDao;

    @Autowired
    private EditionDao editionDao;

    @Autowired
    private AccountService accountService;

    private static final String WINNER_SELECTION_TIME = "T17:00:00";
    private static final String BOOKING_START_TIME = "T17:30:00";
    private static final String BOOKING_END_TIME = "T16:45:00";
    private static final String BOOKING_START_DATE = "2018-12-25";
    private static final String APPEND_MIDNIGHT = "T00:00:00";
    private static final int EDITION_START_NUMBER = 1;
    private static final int EDITION_DAYS_INCREMENT = 2;
    private static final int EDITION_INCREMENT = 1;

    @Override
    public WinnerHistory winnerHistory(String dateFrom, String dateTo) {
        Edition edition = editionDao.fetchEditionFromDateRange(dateFrom, dateTo);
        if (edition != null) {
            List<WinnerProfile> winnerProfiles = winnerDao.fetchWinners(edition.getEdition());
            return new WinnerHistory(winnerProfiles);
        }
        return new WinnerHistory();
    }

    @Override
    public WinnerHistoryForDashboard winnerHistoryForDashboard() {
        int numbersOfEditions = 8;
        List<Edition> editions = editionDao.fetchEditions(numbersOfEditions);
        int latestEditionNumber = editionDao.fetchLatestEdition().getEdition();

        List<WinnerInfoForDashboard> winners = new ArrayList<>();

        if (!CollectionUtils.isEmpty(editions)) {

            for(Edition edition: editions){
                if (edition.getEdition() != latestEditionNumber){
                    WinnerProfile profile = winnerDao.fetchWinnerForDashboard(edition.getEdition());

                    WinnerInfoForDashboard dashboard = new WinnerInfoForDashboard();
                    dashboard.setName(profile.getName());
                    dashboard.setEdition(edition);
                    dashboard.setWinningDate(profile.getWinningDate());
                    dashboard.setWinningNumber(profile.getWinningNumber());
                    winners.add(dashboard);
                }
            }

            return new WinnerHistoryForDashboard(winners);
        }
        return new WinnerHistoryForDashboard();
    }

    @Override
    public NextDraw nextDrawDateTime() {
        Edition edition = findNewEditionRange();
        LocalDateTime latestEditionEndTime = LocalDateTime.parse(edition.getEditionTo());
        String drawTime = latestEditionEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + WINNER_SELECTION_TIME;

        return new NextDraw(drawTime, edition);
    }

    @Override
    public Edition fetchCurrentEdition() {
        return findNewEditionRange();
    }

    @Override
    public WinningNumber lastEditionWinner() {

        List<WinnerInfo> winners = winnerDao.fetchLastEditionWinningInfo();
        if (!CollectionUtils.isEmpty(winners)){
            WinnerInfo winner = winners.get(0);
            Edition prevWinnerEdition = editionDao.fetchEditionFromEdition(winner.getEdition());

            return new WinningNumber(prevWinnerEdition.getEdition(),
                    prevWinnerEdition.getEditionFrom(),
                    prevWinnerEdition.getEditionTo(),
                    winner.getWinningNumber(),
                    null);
        }

        return new WinningNumber(0,
                "",
                "",
                "",
                null);
    }

    @Override
    public WinningNumber generateWinningNumbers(int digits) {
        Edition edition = findNewEditionRange();
        NextDraw nextDrawDateTime = nextDrawDateTime();

        WinnerInfo winnerExist = winnerDao.checkWinningNumberAvailableForDateRange(edition);

        try {
            checkCutOffTimeForWinnerSelection(nextDrawDateTime);
        } catch (DeepPocketException e) {

            WinnerInfo winner = winnerDao.fetchLastEditionWinningInfo().get(0);
            Edition prevWinnerEdition = editionDao.fetchEditionFromEdition(winner.getEdition());
            Account winningAccountInfo = numberDao.fetchWinningAccount(winner.getWinningNumber(),
                    edition.getEdition());

            return new WinningNumber(prevWinnerEdition.getEdition(),
                    prevWinnerEdition.getEditionFrom(),
                    prevWinnerEdition.getEditionTo(),
                    winner.getWinningNumber(),
                    winningAccountInfo);
        }

        if (winnerExist == null) {

            NumberResponse numbers = generateNumbers(digits);
            Map<String, List<String>> numberMap = numbers.getNumbers();
            List<String> keysAsArray = new ArrayList<>(numberMap.keySet());
            Random r = new Random();
            List<String> elementsOfRandKey = numberMap.get(keysAsArray.get(r.nextInt(keysAsArray.size())));

            String selectedNumber = elementsOfRandKey.get(r.nextInt(elementsOfRandKey.size()));
            Number winningNumber = numberDao.fetchWinningNumber(selectedNumber, edition.getEdition());
            Account winningAccountInfo = numberDao.fetchWinningAccount(selectedNumber, edition.getEdition());

            //save to prev winner info table
            if (winningAccountInfo == null) {
                winnerDao.saveWinner("NONE",
                        "NONE",
                        selectedNumber,
                        getFormattedDateTime(),
                        edition.getEdition());
            } else {
                winnerDao.saveWinner(winningAccountInfo.getId(),
                        winningNumber.getPaymentId(),
                        selectedNumber,
                        getFormattedDateTime(),
                        edition.getEdition());
            }

            clearTempBookedNumberList();
            return new WinningNumber(edition.getEdition(),
                    edition.getEditionFrom(),
                    edition.getEditionTo(),
                    selectedNumber,
                    winningAccountInfo);

        } else {
            String selectedNumber = winnerExist.getWinningNumber();
            Account winningAccountInfo = numberDao.fetchWinningAccount(selectedNumber, edition.getEdition());

            clearTempBookedNumberList();
            return new WinningNumber(edition.getEdition(),
                    edition.getEditionFrom(),
                    edition.getEditionTo(),
                    selectedNumber,
                    winningAccountInfo);
        }
    }

    private void clearTempBookedNumberList() {
        tempLockedNumbers = new HashSet<>();
    }

    @Override
    public NumberResponse generateNumbers(int digits) {
        NumberResponse response = new NumberResponse();
        Map<String, List<String>> numberMap = numberListMapWithStartDigit(digits);

        response.setNumbers(numberMap);
        return response;
    }

    @Override
    public FastTrackNumberResponse generateFastTrackNumbers(String userName, int numbersToGenerate) {

        try {
            checkCutOffTimeForBookingAllowed();
        } catch (DeepPocketException e) {
            LOGGER.error("Error occurred in generating fast track numbers for user {} | error {}", userName , e);
            new FastTrackNumberResponse(userName,
                    0,
                    "16:45:00",
                    "17:30:00", new HashSet<>());
        }

        int numberDigits = 4;
        Edition edition = findNewEditionRange();
        List<String> allNumbers = generateNumberList(numberDigits);

        List<Number> lokedNumberList = numberDao.fetchLockedNumbers(edition.getEdition());
        Set<String> alreadyLockedNumbers = lokedNumberList
                .stream()
                .map(Number::getNumber)
                .collect(Collectors.toSet());

        Set<String> tempLockedNumbers = fetchTempLockedNumbers();

        List<String> filterNumberList = allNumbers
                .stream()
                .filter(n -> !alreadyLockedNumbers.contains(n) && !tempLockedNumbers.contains(n))
                .collect(Collectors.toList());

        Collections.shuffle(filterNumberList);

        Set<String> fastTrackNumbers = new HashSet<>(filterNumberList.subList(0, numbersToGenerate));

        return new FastTrackNumberResponse(userName,
                edition.getEdition(),
                edition.getEditionFrom(),
                edition.getEditionTo(),
                fastTrackNumbers);
    }

    @Override
    public Set<String> fetchTempLockedNumbers() {

        try {
            checkCutOffTimeForBookingAllowed();
        } catch (DeepPocketException e) {
            LOGGER.error("Error occurred in fetching temp locked numbers ", e);
            clearTempBookedNumberList();
            return tempLockedNumbers;
        }

        if (CollectionUtils.isEmpty(tempLockedNumbers)){
            Edition edition = findNewEditionRange();
            List<Number> allLockedNumbers = numberDao.fetchLockedNumbers(edition.getEdition());

            if (!CollectionUtils.isEmpty(allLockedNumbers)) {
                Set<String> numbers = allLockedNumbers
                        .stream()
                        .map(Number::getNumber)
                        .collect(Collectors.toSet());

                addToTempLockedNumbers(numbers);
                return numbers;
            }
        }
        return tempLockedNumbers;
    }

    @Override
    public AlreadyBookedNumberResponse isBookingAllowed(String userName) {

        try {
            checkCutOffTimeForBookingAllowed();
        } catch (DeepPocketException e) {
            LOGGER.error("Error occurred in checking is booking allowed ", e);
            return new AlreadyBookedNumberResponse(userName,
                    -1,
                    "16:45:00",
                    "17:30:00",
                    false,
                    0);
        }

        int maxAllowedAmount = 10;
        boolean isBookingAllowed = true;
        Edition edition = findNewEditionRange();
        LockNumberResponse response = fetchLockedNumbers(userName);
        int bookedAmount = response.getNumbers().size();
        if (bookedAmount == maxAllowedAmount) {
            isBookingAllowed = false;
        }
        int numbersAllowedToBook = maxAllowedAmount - bookedAmount;
        return new AlreadyBookedNumberResponse(userName,
                edition.getEdition(),
                edition.getEditionFrom(),
                edition.getEditionTo(),
                isBookingAllowed,
                numbersAllowedToBook);
    }

    @Override
    public LockNumberResponse fetchLockedNumbers(String userName) {

        try {
            checkCutOffTimeForBookingAllowed();
        } catch (DeepPocketException e) {
            LOGGER.error("Error occurred in fetching locked numbers ", e);
            LockNumberResponse response = new LockNumberResponse();
            response.setUserName(userName);
            response.setAlreadyLocked(false);
            response.setLocked(false);
            response.setNumbers(new HashSet<>());
            response.setDimesPurchaseHistory(String.valueOf(0));
            response.setEdition(-1);
            response.setEditionFrom("16:45:00");
            response.setEditionTo("17:30:00");
            return response;
        }

        Edition edition = findNewEditionRange();
        if (StringUtils.isEmpty(userName)) {
            List<Number> allLockedNumbers = numberDao.fetchLockedNumbers(edition.getEdition());

            if (!CollectionUtils.isEmpty(allLockedNumbers)) {
                Set<String> numbers = allLockedNumbers
                        .stream()
                        .map(Number::getNumber)
                        .collect(Collectors.toSet());

                LockNumberResponse response = new LockNumberResponse();
                response.setAlreadyLocked(true);
                response.setNumbers(numbers);
                response.setEdition(edition.getEdition());
                response.setEditionFrom(edition.getEditionFrom());
                response.setEditionTo(edition.getEditionTo());
                response.setDimesPurchaseHistory(String.valueOf(numbers.size()));

                return response;
            }

        } else {
            List<Account> accounts = accountDao.fetchAccount(userName);
            if (!CollectionUtils.isEmpty(accounts)) {
                List<Number> userLockedNumbers = numberDao.fetchLockedNumbersForUser(accounts.get(0).getId(), edition.getEdition());

                if (!CollectionUtils.isEmpty(userLockedNumbers)) {
                    Set<String> numbers = userLockedNumbers
                            .stream()
                            .map(Number::getNumber)
                            .collect(Collectors.toSet());

                    LockNumberResponse response = new LockNumberResponse();
                    response.setUserName(userName);
                    response.setAlreadyLocked(true);
                    response.setNumbers(numbers);
                    response.setEdition(edition.getEdition());
                    response.setEditionFrom(edition.getEditionFrom());
                    response.setEditionTo(edition.getEditionTo());
                    response.setDimesPurchaseHistory(String.valueOf(numbers.size()));

                    return response;
                }
            }
        }
        LockNumberResponse response = new LockNumberResponse();
        response.setUserName(userName);
        response.setAlreadyLocked(false);
        response.setNumbers(new HashSet<>());
        response.setDimesPurchaseHistory(String.valueOf(0));
        return response;
    }

    // todo next draw date set to (now + 1) if now > nowT17:30:00z
    // last draw date = nowT17:30:00z | next draw date = (now + 1)T16:45:00z
    // create edition table with edition and last draw, next draw columns
    private NumberDateRange getNumberDateRange() {
//        Edition latestEdition = fetchLatestEdition();
//
//        if (latestEdition == null){
//            int startingEdition = 1;
//            String from = getFormattedDateForRange(1, "yyyy-MM-dd");
//            String to = getFormattedDateForRange(0, "yyyy-MM-dd");
//            String lastDrawDate = from + BOOKING_START_TIME;
//            String nextDrawDate = to + BOOKING_END_TIME;
//
//            Edition edition = new Edition(lastDrawDate, nextDrawDate, startingEdition);
//            saveEdition(edition);
//            return new NumberDateRange(lastDrawDate, nextDrawDate);
//        }

        Edition newEditionRange = findNewEditionRange();

//        String lastDrawDate = getFormattedDateForRange(1, "yyyy-MM-dd");
//        String nextDrawDate = getFormattedDateForRange(0, "yyyy-MM-dd");
//        String from = lastDrawDate + BOOKING_START_TIME;
//        String to = nextDrawDate + BOOKING_END_TIME;
//        return new NumberDateRange(from, to);
        return new NumberDateRange(newEditionRange.getEditionFrom(), newEditionRange.getEditionTo());
    }

    /**
     *  Start dateTime: 2018-12-25T17:30:00z
     *  2018-12-25T17:30:00z - 2018-12-27T16:45:00z | edition 1 (curtime > 2018-12-25T17:30:00z && curTime < 2018-12-27T16:45:00z)
     *  2018-12-27T17:30:00z - 2018-12-29T16:45:00z | edition 2
     *  2018-12-29T17:30:00z - 2018-12-31T16:45:00z | edition 3
     *  2018-12-31T17:30:00z - 2019-01-02T16:45:00z | edition 4
     *  2019-01-02T17:30:00z - 2018-01-04T16:45:00z | edition 5
     */
    private Edition findNewEditionRange() {
        LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
        String firstEditionFrom = BOOKING_START_DATE + BOOKING_START_TIME;
        LocalDateTime firstEditionStartDateTime = LocalDateTime.parse(BOOKING_START_DATE + BOOKING_START_TIME);
        LocalDateTime firstEditionEndDate = LocalDateTime.parse(BOOKING_START_DATE + APPEND_MIDNIGHT).plusDays(EDITION_DAYS_INCREMENT);
        String firstEditionEndDateInString = firstEditionEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_END_TIME;
        LocalDateTime firstEditionEndDateTime = LocalDateTime.parse(firstEditionEndDateInString);

        Edition latestSavedEdition = fetchLatestEdition();

        if (latestSavedEdition == null && currentTime.isBefore(firstEditionStartDateTime)) {
            // before 2018-12-25T17:30:00z
            Edition edition = new Edition(firstEditionFrom, firstEditionEndDateInString, EDITION_START_NUMBER);
            saveEdition(edition);

            return edition;

        } else if (latestSavedEdition == null && currentTime.isAfter(firstEditionEndDateTime)) {
            // currentT17:30:00z
            LocalDateTime currentDateTimeWithStartTimeAppend = LocalDateTime.parse(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_START_TIME);
            // currentT16:45:00z
            LocalDateTime currentDateTimeWithEndTimeAppend = LocalDateTime.parse(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_END_TIME);

            //current date time is after
            // < currentT16:45:00z
            if (currentTime.isBefore(currentDateTimeWithEndTimeAppend)) {

                String start = currentTime.minusDays(EDITION_DAYS_INCREMENT).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_START_TIME;
                String end = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_END_TIME;

                Edition edition = new Edition(start, end, EDITION_START_NUMBER);
                if (fetchEditionFromEdition(edition.getEdition()) == null){
                    saveEdition(edition);
                }
                return new Edition(start, end, EDITION_START_NUMBER);

            // > currentT17:30:00z
            } else if (currentTime.isAfter(currentDateTimeWithStartTimeAppend)) {

                String start = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_END_TIME;
                String end = currentTime.plusDays(EDITION_DAYS_INCREMENT).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + BOOKING_START_TIME;

                Edition edition = new Edition(start, end, EDITION_START_NUMBER);
                if (fetchEditionFromEdition(edition.getEdition()) == null){
                    saveEdition(edition);
                }
                return new Edition(start, end, EDITION_START_NUMBER);
            }

        } else if (latestSavedEdition != null && currentTime.isBefore(LocalDateTime.parse(latestSavedEdition.getEditionTo()))){

            return latestSavedEdition;

        } else if (latestSavedEdition != null && currentTime.isAfter(firstEditionEndDateTime)){
            // after 2018-12-27T16:45:00z
            LocalDateTime latestSavedEditionFrom = LocalDateTime.parse(latestSavedEdition.getEditionFrom());
            LocalDateTime latestSavedEditionTo = LocalDateTime.parse(latestSavedEdition.getEditionTo());
            int latestEdition = latestSavedEdition.getEdition();

            String nextEditionStartDate = latestSavedEditionTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nextEditionStartDateTime = nextEditionStartDate + BOOKING_START_TIME;
            String nextEditionEndDateTime = latestSavedEditionTo
                    .plusDays(EDITION_DAYS_INCREMENT)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    + BOOKING_END_TIME;

            int nextEdition = latestEdition + EDITION_INCREMENT;
            Edition edition = new Edition(nextEditionStartDateTime, nextEditionEndDateTime, nextEdition);

            if (fetchEditionFromEdition(edition.getEdition()) == null){
                saveEdition(edition);
            }
            return edition;
        }

        return new Edition(firstEditionFrom, firstEditionEndDateInString, EDITION_START_NUMBER);
    }

    private boolean saveEdition(Edition edition){
        return editionDao.saveEdition(edition.getEditionFrom(),
                edition.getEditionTo(),
                edition.getEdition());
    }

    private Edition fetchEditionFromDateRange(String from, String to){
        return editionDao.fetchEditionFromDateRange(from, to);
    }

    private Edition fetchEditionFromEdition(int edition){
        return editionDao.fetchEditionFromEdition(edition);
    }

    private Edition fetchLatestEdition(){
        return editionDao.fetchLatestEdition();
    }

    //TODO add payment savings
    @Override
    public LockNumberResponse lockNumbers(LockNumberRequest lockNumberRequest) {
        String userName = lockNumberRequest.getUser().getUserName();

        try {
            checkCutOffTimeForBookingAllowed();
        } catch (DeepPocketException e) {
            LOGGER.error("Error occurred in fetching locked numbers ", e);
            LockNumberResponse response = new LockNumberResponse();
            response.setUserName(userName);
            response.setAlreadyLocked(false);
            response.setLocked(false);
            response.setNumbers(new HashSet<>());
            response.setDimesPurchaseHistory(String.valueOf(0));
            response.setEdition(-1);
            response.setEditionFrom("16:45:00");
            response.setEditionTo("17:30:00");
            return response;
        }

        Edition edition = findNewEditionRange();
        Set<String> numberList = new HashSet<>(lockNumberRequest.getNumberList());

        List<Account> accounts = accountDao.fetchAccount(userName);

        if (validateNumbers(numberList)) {
            Set<String> alreadyLockedNumbers = checkAlreadyLocked(numberList);

            if (!CollectionUtils.isEmpty(alreadyLockedNumbers)){
                //save already not locked numbers
                List<String> numbersToLock = numberList
                        .stream()
                        .filter(n -> !alreadyLockedNumbers.contains(n))
                        .collect(Collectors.toList());

                lockNumberRequest.setNumberList(numbersToLock);
            }

            if (CollectionUtils.isEmpty(accounts)) {
                //create account
                AccountRequest accountRequest = new AccountRequest();
                accountRequest.setUserName(lockNumberRequest.getUser().getUserName());
                accountRequest.setName(lockNumberRequest.getUser().getName());
                Account createdAccount = accountService.createAccount(accountRequest);

                addToTempLockedNumbers(numberList);

                Payment payment = savePayment(lockNumberRequest, createdAccount, edition.getEdition());

                return numberDao.lockNumbers(lockNumberRequest, createdAccount, payment.getId(), edition.getEdition())
                        ? new LockNumberResponse(userName,
                        numberList,
                        edition.getEdition(),
                        edition.getEditionFrom(),
                        edition.getEditionTo(),
                        String.valueOf(numberList.size()),
                        false,
                        true)
                        : new LockNumberResponse(userName,
                        numberList,
                        edition.getEdition(),
                        edition.getEditionFrom(),
                        edition.getEditionTo(),
                        String.valueOf(numberList.size()),
                        true,
                        false);
            } else {

                addToTempLockedNumbers(numberList);
                Payment payment = savePayment(lockNumberRequest, accounts.get(0), edition.getEdition());

                return numberDao.lockNumbers(lockNumberRequest, accounts.get(0), payment.getId(), edition.getEdition())
                        ? new LockNumberResponse(userName,
                        numberList,
                        edition.getEdition(),
                        edition.getEditionFrom(),
                        edition.getEditionTo(),
                        String.valueOf(numberList.size()),
                        false,
                        true)
                        : new LockNumberResponse(userName,
                        numberList,
                        edition.getEdition(),
                        edition.getEditionFrom(),
                        edition.getEditionTo(),
                        String.valueOf(numberList.size()),
                        true,
                        false);

            }
        }
        LockNumberResponse response = new LockNumberResponse();
        response.setUserName(userName);
        response.setLocked(false);
        response.setNumbers(numberList);
        addToTempLockedNumbers(numberList);

        return response;
    }

    private void checkCutOffTimeForBookingAllowed() throws DeepPocketException {
        LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
        Edition edition = findNewEditionRange();
        LocalDateTime cutoffTimeEnds = LocalDateTime.parse(edition.getEditionFrom());
        LocalDateTime cutoffTimeStart = cutoffTimeEnds.minusMinutes(45);

        if (currentTime.isAfter(cutoffTimeStart) && currentTime.isBefore(cutoffTimeEnds)){
            throw new DeepPocketException("Cannot book numbers during 16:45:00 - 17:30:00 UTC hrs");
        }
    }

    private void checkCutOffTimeForWinnerSelection(NextDraw nextDrawDateTime) throws DeepPocketException {
        LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime nextDrawTime = LocalDateTime.parse(nextDrawDateTime.getNextDrawDateTime());

        LocalDateTime cutoffTimeLowerEnd = nextDrawTime.minusMinutes(15);
        LocalDateTime cutoffTimeUpperEnd = nextDrawTime.plusMinutes(15);

        if (currentTime.isBefore(cutoffTimeLowerEnd) || currentTime.isAfter(cutoffTimeUpperEnd)) {
            String msg = MessageFormat.format("Should select winning number between {0} - {1}",
                    cutoffTimeLowerEnd,
                    cutoffTimeUpperEnd);
            throw new DeepPocketException(msg);
        }

    }

    private Payment savePayment(LockNumberRequest lockNumberRequest, Account account, int edition) {
        Payment paymentDaoModel = new Payment();
        com.deep.pocket.model.request.Payment paymentRequestModel = lockNumberRequest.getPayment();

        paymentDaoModel.setId(UUID.randomUUID().toString());
        paymentDaoModel.setAccId(account.getId());
        paymentDaoModel.setDateAdded(getFormattedDateTime());
        paymentDaoModel.setPayerId(paymentRequestModel.getPayerId());
        paymentDaoModel.setPayerEmail(paymentRequestModel.getPayerEmail());
        paymentDaoModel.setPaymentId(paymentRequestModel.getPaymentId());
        paymentDaoModel.setPaymentSuccess(paymentRequestModel.getPaymentSuccess());
        paymentDaoModel.setPurchasedNumberCount(lockNumberRequest.getPurchasedNumberCount());

        paymentDao.savePayment(paymentDaoModel, edition);

        return paymentDaoModel;
    }

    private void addToTempLockedNumbers(Set<String> numberList) {
        tempLockedNumbers.addAll(numberList);
    }

    private Set<String> checkAlreadyLocked(Set<String> numberList) {
        Edition edition = findNewEditionRange();
        return numberDao.checkAlreadyLocked(numberList, edition.getEdition());
    }

    private boolean validateNumbers(Set<String> numberList) {
        return numberList
                .stream()
                .allMatch(n -> n.matches("[0-9]{4,5}"));
    }

    private List<String> generateNumberList(int digits){
        int end = 9;
        String format = "%01d";
        List<String> numbers = new ArrayList<>();

        if (digits == 4){
            end = 9999;
            format = "%04d";
        } else if (digits == 5) {
            end = 99999;
            format = "%05d";
        }

        for (int i=0; i <= end; i++) {
            String number = String.format(format, i);
            numbers.add(number);
        }
        return numbers;
    }

    private Map<String, List<String>> numberListMapWithStartDigit(int digits) {
        return generateNumberList(digits)
                .stream()
                .collect(Collectors.groupingBy(li -> String.valueOf(li.charAt(0))));
    }

    private String getFormattedDateTime() {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private String getFormattedDateForRange(int offset, String pattern) {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC()).minusDays(offset);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return now.format(formatter);
    }
}
