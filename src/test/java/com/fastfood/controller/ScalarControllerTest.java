package com.fastfood.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScalarController.class)
class ScalarControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testScalarEndpoint() throws Exception {
        mockMvc.perform(get("/scalar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("scalar"))
            .andExpect(jsonPath("$.time").exists());
    }
}
