CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY NOT NULL,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    channel VARCHAR(50) NOT NULL CONSTRAINT transactions_channel_check CHECK (
        channel IN ('MOBILE_BANKING', 'INTERNET_BANKING', 'ATM')
    ),
    amount DECIMAL(19, 2) NOT NULL,
    account VARCHAR(100) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'IDR',
    payment_method VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL CONSTRAINT transactions_status_check CHECK (
        status IN ('PENDING', 'SUCCESS', 'FAILED')
    ),
    corebank_reference VARCHAR(255),
    biller_reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
