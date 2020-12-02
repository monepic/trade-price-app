package com.monepic.tradeprice.utils;

import java.util.Map;

public interface CsvTokenizer {

    Map<String, String> tokenize(String input);

}
