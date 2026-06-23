-- Drop existing tables (order matters for FK constraints)
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS credit_balances;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255),
                       name VARCHAR(255),
                       created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Create subscriptions table
CREATE TABLE subscriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_fk BIGINT NOT NULL,
                               stripe_customer_id VARCHAR(255) NOT NULL UNIQUE,
                               stripe_subscription_id VARCHAR(255) NOT NULL UNIQUE,
                               status VARCHAR(50) NOT NULL,
                               current_price_id VARCHAR(255),
                               current_period_end BIGINT,
                               cancel_at_period_end BOOLEAN,
                               created_at TIMESTAMP(6),
                               updated_at TIMESTAMP(6),
                               FOREIGN KEY (user_fk) REFERENCES users(id) ON DELETE CASCADE
);

-- Create credit_balances table
CREATE TABLE credit_balances (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_fk BIGINT NOT NULL,
                                 balance INT NOT NULL DEFAULT 0,
                                 monthly_cost INT NOT NULL DEFAULT 0,
                                 last_deduction_date TIMESTAMP(6),
                                 next_deduction_date TIMESTAMP(6),
                                 active BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP(6),
                                 updated_at TIMESTAMP(6),
                                 FOREIGN KEY (user_fk) REFERENCES users(id) ON DELETE CASCADE,
                                 UNIQUE KEY uk_credit_user (user_fk)
);