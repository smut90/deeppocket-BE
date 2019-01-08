package com.deep.pocket.dao;

import com.deep.pocket.model.NumberDateRange;
import com.deep.pocket.model.dao.Edition;
import com.deep.pocket.model.dao.WinnerInfo;
import com.deep.pocket.model.dao.WinnerProfile;

import java.util.List;

public interface WinnerDao {

    boolean checkWinnerRecordAvailable(String winnerId, String paymentId, Edition edition);

    WinnerInfo checkWinningNumberAvailableForDateRange(Edition edition);

    boolean saveWinner(String winnerId, String paymentId, String winningNumber, String winningDate, int edition);

    List<WinnerProfile> fetchWinners(int edition);

    WinnerProfile fetchWinnerForDashboard(int edition);

    List<WinnerInfo> fetchLastEditionWinningInfo();

    int fetchEdition();
}
