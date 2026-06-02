/* Remove constraint status_enum_check on transaction table and create new enum_status table to store enum values */
ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_status_check;
    
/* Add Removed status to constraint */
ALTER TABLE transactions
    ADD CONSTRAINT transactions_status_check CHECK (
        status IN ('PENDING', 'SUCCESS', 'FAILED', 'REMOVED')
    );


