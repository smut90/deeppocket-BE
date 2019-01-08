package com.deep.pocket.service;

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

import java.util.Set;

public interface NumberService {

    WinnerHistory winnerHistory(String dateFrom, String dateTo);

    WinnerHistoryForDashboard winnerHistoryForDashboard();

    NextDraw nextDrawDateTime();

    Edition fetchCurrentEdition();

    WinningNumber lastEditionWinner();

    WinningNumber generateWinningNumbers(int digits);

    NumberResponse generateNumbers(int digits);

    FastTrackNumberResponse generateFastTrackNumbers(String userName, int numbersToGenerate);

    Set<String> fetchTempLockedNumbers();

    AlreadyBookedNumberResponse isBookingAllowed(String userName);

    LockNumberResponse fetchLockedNumbers(String userName);

    LockNumberResponse lockNumbers(LockNumberRequest lockNumberRequest) throws DeepPocketException;

}
