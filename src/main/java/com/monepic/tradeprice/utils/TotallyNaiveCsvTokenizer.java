package com.monepic.tradeprice.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This doesn't take account of things that might be found in a real-life CSV file,
 * such as quotes, escapes, wrong number of fields etc
 * and so is only suitable for this demo purpose
 */
public class TotallyNaiveCsvTokenizer implements CsvTokenizer {

    private final String[] headers;

    public TotallyNaiveCsvTokenizer(String headers) {
        this.headers = headers.split(",");
    }

    @Override
    public Map<String, String> tokenize(String input) {
        Map<String, String> result = new HashMap<>();
        String[] toks = input.split(",");

        if (toks.length != headers.length) {
            throw new CsvParseException("headers/input length mismatch");
        }

        for (int i = 0; i < toks.length; i++) {
            result.put(headers[i], toks[i]);
        }
        return result;
    }

    public static class CsvParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CsvParseException(String msg) { super(msg); }
    }
}
