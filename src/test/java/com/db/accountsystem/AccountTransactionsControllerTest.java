package com.db.accountsystem;

import com.db.accountsystem.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class AccountTransactionsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() throws Exception {
        accountsService.removeAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void shouldTransferBalances() throws Exception {

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-2\",\"balance\":1000}")).andExpect(status().isCreated());
        String requestedJson = "{\n" +
                "  \"accountFromId\": \"Id-1\",\n" +
                "  \"accountToId\": \"Id-2\",\n" +
                "  \"amount\": 100\n" +
                "}";

        this.mockMvc.perform(post("/v1/transaction").contentType(MediaType.APPLICATION_JSON)
                .content(requestedJson)).andExpect(status().isAccepted());

        com.db.accountsystem.response.Account accountFrom = accountsService.getAccount("Id-1");
        assertThat(accountFrom.getAccountId()).isEqualTo("Id-1");
        assertThat(accountFrom.getBalance()).isEqualByComparingTo("900");

        com.db.accountsystem.response.Account accountTo = accountsService.getAccount("Id-2");
        assertThat(accountTo.getAccountId()).isEqualTo("Id-2");
        assertThat(accountTo.getBalance()).isEqualByComparingTo("1100");
    }

    @Test
    public void shouldNotTransferBalancesIfFromAccountHasLessBalance() throws Exception {

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-2\",\"balance\":1000}")).andExpect(status().isCreated());
        String requestedJson = "{\n" +
                "  \"accountFromId\": \"Id-1\",\n" +
                "  \"accountToId\": \"Id-2\",\n" +
                "  \"amount\": 10000\n" +
                "}";

        this.mockMvc.perform(post("/v1/transaction").contentType(MediaType.APPLICATION_JSON)
                        .content(requestedJson)).andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertEquals("Account with id:Id-1 does not have enough balance to transfer.", result.getResolvedException().getMessage()));
    }

    @Test
    public void shouldNotTransferBalancesIfFromAccountDoesntExist() throws Exception {

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-2\",\"balance\":1000}")).andExpect(status().isCreated());
        String requestedJson = "{\n" +
                "  \"accountFromId\": \"Id-122\",\n" +
                "  \"accountToId\": \"Id-2\",\n" +
                "  \"amount\": 10000\n" +
                "}";

        this.mockMvc.perform(post("/v1/transaction").contentType(MediaType.APPLICATION_JSON)
                        .content(requestedJson)).andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertEquals("Account with id:Id-122 does not exist.", result.getResolvedException().getMessage()));
    }

    @Test
    public void shouldNotTransferBalancesIfToAccountDoesntExist() throws Exception {

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-1\",\"balance\":1000}")).andExpect(status().isCreated());
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-2\",\"balance\":1000}")).andExpect(status().isCreated());
        String requestedJson = "{\n" +
                "  \"accountFromId\": \"Id-1\",\n" +
                "  \"accountToId\": \"Id-222\",\n" +
                "  \"amount\": 10000\n" +
                "}";

        this.mockMvc.perform(post("/v1/transaction").contentType(MediaType.APPLICATION_JSON)
                        .content(requestedJson)).andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertEquals("Account with id:Id-222 does not exist.", result.getResolvedException().getMessage()));
    }
}
