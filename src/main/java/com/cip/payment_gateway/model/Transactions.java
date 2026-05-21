package com.cip.payment_gateway.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import lombok.*;
import com.cip.payment_gateway.enums.TransactionChannel;
import com.cip.payment_gateway.enums.TransactionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transactions {

    // Total columns: 12
    // id, order_id, channel, amount, account, currency, payment_method, status,
    // corebank_reference, biller_reference, created_at, updated_at

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private TransactionChannel channel;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "account", nullable = false)
    private String account;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "IDR";

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "corebank_reference")
    private String corebankReference;

    @Column(name = "biller_reference")
    private String billerReference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
