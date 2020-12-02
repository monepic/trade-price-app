package com.monepic.tradeprice.api;

import com.monepic.tradeprice.service.TradePriceService;
import com.monepic.tradeprice.testutils.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradePriceApi.class)
public class TradePriceApiTest {


    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TradePriceService service;

    @Test
    public void getByInstrumentShouldReturnCollection() throws Exception {
        when(service.getByInstrument("VOD"))
                .thenReturn(Collections.singletonList(TestData.VIP_1.get()));

        mockMvc.perform(get("/instrument/VOD/prices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[" + TestData.VIP_1_JSON + "]"));
    }

    @Test
    public void getByVendorShouldReturnCollection() throws Exception {
        when(service.getByVendor(1L))
                .thenReturn(Collections.singletonList(TestData.VIP_1.get()));

        mockMvc.perform(get("/vendor/1/prices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[" + TestData.VIP_1_JSON + "]"));
    }

    @Test
    public void createNewTradePrice() throws Exception {
        mockMvc.perform(post("/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.NPR_1_JSON)
        )
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

}
