package com.deep.pocket.dao;

import com.deep.pocket.model.dao.Edition;

import java.util.List;

public interface EditionDao {

    boolean saveEdition(String from, String to, int edition);

    Edition fetchEditionFromDateRange(String from, String to);

    Edition fetchEditionFromEdition(int edition);

    Edition fetchLatestEdition();

    List<Edition> fetchAllEditions();

    List<Edition> fetchEditions(int numberOfEditions);
}
