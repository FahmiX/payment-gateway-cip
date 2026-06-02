package com.cip.payment_gateway.service;

import com.cip.payment_gateway.client.BillerFeignClient;
import com.cip.payment_gateway.client.CoreBankFeignClient;
import com.cip.payment_gateway.dto.request.BillerRequest;
import com.cip.payment_gateway.dto.request.CoreBankRequest;
import com.cip.payment_gateway.dto.request.PaymentRequest;
import com.cip.payment_gateway.dto.response.BillerResponse;
import com.cip.payment_gateway.dto.response.CoreBankResponse;
import com.cip.payment_gateway.dto.response.PaymentResponse;
import com.cip.payment_gateway.enums.TransactionStatus;
import com.cip.payment_gateway.exception.DuplicateOrderException;
import com.cip.payment_gateway.exception.ResourceNotFoundException;
import com.cip.payment_gateway.model.Transactions;
import com.cip.payment_gateway.repository.TransactionRepository;
import com.cip.payment_gateway.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl")
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CoreBankFeignClient coreBankClient;

    @Mock
    private BillerFeignClient billerClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest validRequest;
    private Transactions existingTransaction;
    private CoreBankResponse successCoreBankResponse;
    private CoreBankResponse failedCoreBankResponse;
    private BillerResponse successBillerResponse;
    private BillerResponse failedBillerResponse;

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

        existingTransaction = Transactions.builder()
                .id(UUID.randomUUID())
                .orderId("ORDER-001")
                .status(TransactionStatus.SUCCESS)
                .corebankReference("CB-REF-001")
                .billerReference("BL-REF-001")
                .build();

        successCoreBankResponse = CoreBankResponse.builder()
                .status("SUCCESS")
                .coreBankReference("CB-REF-001")
                .build();

        failedCoreBankResponse = CoreBankResponse.builder()
                .status("FAILED")
                .build();

        successBillerResponse = BillerResponse.builder()
                .status("SUCCESS")
                .billerReference("BL-REF-001")
                .build();

        failedBillerResponse = BillerResponse.builder()
                .status("FAILED")
                .build();
    }

    /*
     * Test Scope
     * createPayment() scenarios:
     1. Test duplicate payment (orderId already exists) -> expect DuplicateOrderException
     2. Test successful payment flow (CoreBank and Biller succeed) -> payment success and expect success response
     3. Test CoreBank Failed (insufficient balance) -> transaction marked as failed, expect failed response, and won't call Biller
     4. Test Biller Failed -> transaction marked as failed, expect failed response
     5. Test CoreBank called with correct amount
     6. Test Biller called with correct payload

     * getPayment() scenarios:
     1. Test find transaction by id, it will return payment response
     2. Test find transaction by unknown id, it will return not found message
     */

    @Nested
    @DisplayName("createPayment()")
    class CreatePayment {

        @Test
        @DisplayName("When there is duplicated orderId, throw the transaction")
        void whenDuplicateOrderId_returnsExistingPaymentResponse() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.of(existingTransaction));
            try {
                paymentService.createPayment(validRequest);
            } catch (Exception ex) {
                assertThat(ex).isInstanceOf(DuplicateOrderException.class);
                assertThat(ex.getMessage()).isEqualTo("Payment already exists for orderId: ORDER-001");
            }

            // Prevent Call CoreBank and Biller
            verifyNoInteractions(coreBankClient, billerClient);
        }

        @Test
        @DisplayName("CoreBank and Biller succeed, it will return success response")
        void whenCoreBankAndBillerSucceed_returnsSuccessResponse() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.empty());
            when(coreBankClient.debit(any())).thenReturn(successCoreBankResponse);
            when(billerClient.pay(any())).thenReturn(successBillerResponse);

            when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
                Transactions trx = invocation.getArgument(0);
                if (trx.getId() == null) {
                    trx.setId(UUID.randomUUID());
                }
                return trx;
            });

            PaymentResponse response = paymentService.createPayment(validRequest);

            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getCorebankReference()).isEqualTo("CB-REF-001");
            assertThat(response.getBillerReference()).isEqualTo("BL-REF-001");
            assertThat(response.getMessage()).isEqualTo("Payment successfully");
            assertThat(response.getOrderId()).isEqualTo("ORDER-001");
        }

        @Test
        @DisplayName("If corebank fail, transaction will be marked as failed and return failed response without call biller")
        void whenCoreBankFails_returnsFailedResponseAndSkipsBiller() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.empty());
            when(coreBankClient.debit(any())).thenReturn(failedCoreBankResponse);

            when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
                Transactions trx = invocation.getArgument(0);
                if (trx.getId() == null) {
                    trx.setId(UUID.randomUUID());
                }
                return trx;
            });

            PaymentResponse response = paymentService.createPayment(validRequest);

            assertThat(response.getStatus()).isEqualTo("FAILED");
            assertThat(response.getMessage()).isEqualTo("Insufficient balance");

            // Won't call biller
            verifyNoInteractions(billerClient);

            // Save transaction with failed status
            ArgumentCaptor<Transactions> captor = ArgumentCaptor.forClass(Transactions.class);
            verify(transactionRepository, atLeast(2)).save(captor.capture());
            Transactions lastSaved = captor.getValue();
            assertThat(lastSaved.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }

        @Test
        @DisplayName("If biller fail, transaction will be marked as failed and return failed response")
        void whenBillerFails_returnsFailedResponse() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.empty());
            when(coreBankClient.debit(any())).thenReturn(successCoreBankResponse);
            when(billerClient.pay(any())).thenReturn(failedBillerResponse);
            when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
                Transactions trx = invocation.getArgument(0);
                if (trx.getId() == null) {
                    trx.setId(UUID.randomUUID());
                }
                return trx;
            });

            PaymentResponse response = paymentService.createPayment(validRequest);

            assertThat(response.getStatus()).isEqualTo("FAILED");
            assertThat(response.getMessage()).isEqualTo("Biller payment failed");

            // Transaction must be updated to failed status
            ArgumentCaptor<Transactions> captor = ArgumentCaptor.forClass(Transactions.class);
            verify(transactionRepository, atLeast(2)).save(captor.capture());
            Transactions lastSaved = captor.getValue();
            assertThat(lastSaved.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }

        @Test
        @DisplayName("Corebank are using right amount")
        void whenCallingCoreBank_sendsCorrectAccountAndAmount() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.empty());
            when(coreBankClient.debit(any())).thenReturn(successCoreBankResponse);
            when(billerClient.pay(any())).thenReturn(successBillerResponse);

            when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
                Transactions trx = invocation.getArgument(0);
                if (trx.getId() == null) {
                    trx.setId(UUID.randomUUID());
                }
                return trx;
            });

            paymentService.createPayment(validRequest);

            ArgumentCaptor<CoreBankRequest> captor = ArgumentCaptor
                    .forClass(CoreBankRequest.class);
            verify(coreBankClient).debit(captor.capture());

            assertThat(captor.getValue().getAccount()).isEqualTo("1234567890");
            assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("500000"));
        }

        @Test
        @DisplayName("Biller called with correct payload")
        void whenCallingBiller_sendsCorrectPayload() {
            when(transactionRepository.findByOrderId(any())).thenReturn(Optional.empty());
            when(coreBankClient.debit(any())).thenReturn(successCoreBankResponse);
            when(billerClient.pay(any())).thenReturn(successBillerResponse);

            when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
                Transactions trx = invocation.getArgument(0);
                if (trx.getId() == null) {
                    trx.setId(UUID.randomUUID());
                }
                return trx;
            });

            paymentService.createPayment(validRequest);

            ArgumentCaptor<BillerRequest> captor = ArgumentCaptor
                    .forClass(BillerRequest.class);
            verify(billerClient).pay(captor.capture());

            assertThat(captor.getValue().getOrderId()).isEqualTo("ORDER-001");
            assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("500000"));
            assertThat(captor.getValue().getPaymentMethod()).isEqualTo("VIRTUAL_ACCOUNT");
        }
    }

    @Nested
    @DisplayName("getPayment()")
    class GetPayment {

        @Test
        @DisplayName("Find transaction by id, it will return payment response")
        void whenTransactionExists_returnsPaymentResponse() {
            UUID id = existingTransaction.getId();
            when(transactionRepository.findById(id)).thenReturn(Optional.of(existingTransaction));

            PaymentResponse response = paymentService.getPayment(id);

            assertThat(response.getTransactionId()).isEqualTo(id.toString());
            assertThat(response.getOrderId()).isEqualTo("ORDER-001");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getCorebankReference()).isEqualTo("CB-REF-001");
            assertThat(response.getBillerReference()).isEqualTo("BL-REF-001");
            assertThat(response.getMessage()).isEqualTo("Payment found");
        }

        @Test
        @DisplayName("Find transaction by unknown id, it will return not found message")
        void whenTransactionNotFound_returnsNotFoundMessage() {
            UUID unknownId = UUID.randomUUID();

            when(transactionRepository.findById(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPayment(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Payment not found with ID: " + unknownId);
        }
    }
}
