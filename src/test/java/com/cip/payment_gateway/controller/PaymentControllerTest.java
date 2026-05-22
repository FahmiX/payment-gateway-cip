package com.cip.payment_gateway.controller;

import com.cip.payment_gateway.dto.request.PaymentRequest;
import com.cip.payment_gateway.dto.response.PaymentResponse;
import com.cip.payment_gateway.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest validRequest;
    private PaymentResponse successResponse;
    private PaymentResponse failedResponse;
    private PaymentResponse notFoundResponse;

    private static final String BASE_URL = "/api/payments";
    private static final UUID TRANSACTION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        validRequest = PaymentRequest.builder()
                .orderId("ORDER-001")
                .channel("MOBILE_BANKING")
                .amount(new BigDecimal("500000"))
                .account("1234567890")
                .currency("IDR")
                .paymentMethod("VIRTUAL_ACCOUNT")
                .build();

        successResponse = PaymentResponse.builder()
                .transactionId(TRANSACTION_ID.toString())
                .orderId("ORDER-001")
                .status("SUCCESS")
                .corebankReference("CB-REF-001")
                .billerReference("BL-REF-001")
                .message("Payment successfully")
                .build();

        failedResponse = PaymentResponse.builder()
                .transactionId(TRANSACTION_ID.toString())
                .orderId("ORDER-001")
                .status("FAILED")
                .message("Insufficient balance")
                .build();

        notFoundResponse = PaymentResponse.builder()
                .message("Payment not found")
                .build();
    }

    @Nested
    @DisplayName("POST /api/payments")
    class CreatePayment {

        @Test
        @DisplayName("If request without authentication, it will return 403")
        void createPayment_whenNoAuth_returnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("If all request is valid, it will return success response with data")
        void createPayment_whenSuccess_returnsWithSuccessResponse() throws Exception {
            when(paymentService.createPayment(any(PaymentRequest.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(post(BASE_URL)
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
                    .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.corebankReference").value("CB-REF-001"))
                    .andExpect(jsonPath("$.billerReference").value("BL-REF-001"))
                    .andExpect(jsonPath("$.message").value("Payment successfully"));
        }

        @Test
        @DisplayName("If corebank fails, it will return failed status with message")
        void createPayment_whenCoreBankFails_returnsWithFailedStatus() throws Exception {
            when(paymentService.createPayment(any(PaymentRequest.class)))
                    .thenReturn(failedResponse);

            mockMvc.perform(post(BASE_URL)
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FAILED"))
                    .andExpect(jsonPath("$.message").value("Insufficient balance"));
        }

        @Test
        @DisplayName("If orderId is duplicate, it will return idempotently with message")
        void createPayment_whenDuplicateOrder_returnsIdempotently() throws Exception {
            PaymentResponse duplicateResponse = PaymentResponse.builder()
                    .transactionId(TRANSACTION_ID.toString())
                    .orderId("ORDER-001")
                    .status("SUCCESS")
                    .corebankReference("CB-REF-001")
                    .billerReference("BL-REF-001")
                    .message("Payment has been processed")
                    .build();

            when(paymentService.createPayment(any())).thenReturn(duplicateResponse);

            mockMvc.perform(post(BASE_URL)
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment has been processed"));
        }

        @Test
        @DisplayName("If content type is wrong (Not application/json), it will return unsupported media type")
        void createPayment_whenWrongContentType() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .with(jwt())
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("raw text"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("If request body is empty, it will return bad request")
        void createPayment_whenEmptyBody() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{id}")
    class GetPayment {
        @Test
        @DisplayName("If transaction found, it will return payment response with data")
        void getPayment_whenFound_returnsWithData() throws Exception {
            when(paymentService.getPayment(TRANSACTION_ID)).thenReturn(successResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", TRANSACTION_ID)
                    .with(jwt()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
                    .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.corebankReference").value("CB-REF-001"))
                    .andExpect(jsonPath("$.billerReference").value("BL-REF-001"))
                    .andExpect(jsonPath("$.message").value("Payment successfully"));
        }

        @Test
        @DisplayName("If transaction not found, it will return not found message")
        void getPayment_whenNotFound_returnsWithNotFoundMessage() throws Exception {
            UUID unknownId = UUID.randomUUID();
            when(paymentService.getPayment(unknownId)).thenReturn(notFoundResponse);

            mockMvc.perform(get(BASE_URL + "/{id}", unknownId)
                    .with(jwt()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment not found"))
                    .andExpect(jsonPath("$.transactionId").doesNotExist());
        }

        @Test
        @DisplayName("If UUID is invalid, it will return bad request")
        void getPayment_whenInvalidUUID_returnsBadRequest() throws Exception {
            mockMvc.perform(get(BASE_URL + "/invalid-UUID")
                    .with(jwt()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("If request without authentication, it will return unauthorized")
        void getPayment_whenNoAuth_returnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", TRANSACTION_ID))
                    .andExpect(status().isUnauthorized());
        }
    }
}