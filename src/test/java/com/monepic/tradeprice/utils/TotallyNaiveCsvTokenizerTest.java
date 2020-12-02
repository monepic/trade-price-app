package com.monepic.tradeprice.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TotallyNaiveCsvTokenizerTest {

    @Test
    public void testTokenizer() {

        CsvTokenizer tokenizer = new TotallyNaiveCsvTokenizer("h1,h2,h3");

        Map<String, String> expected = new HashMap<>();
        expected.put("h1", "d1");
        expected.put("h2", "d2");
        expected.put("h3", "d3");

        assertEquals(expected, tokenizer.tokenize("d1,d2,d3"));
    }
}
