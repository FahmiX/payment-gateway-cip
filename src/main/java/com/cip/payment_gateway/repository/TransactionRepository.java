package com.cip.payment_gateway.repository;

// Model
import com.cip.payment_gateway.model.Transactions;
// Other imports
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transactions, UUID> {
    // Find transaction by order ID
    Optional<Transactions> findByOrderId(String orderId);
}
