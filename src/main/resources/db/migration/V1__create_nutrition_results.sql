CREATE TABLE nutrition_results (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(100) NOT NULL,
    calories DOUBLE PRECISION NOT NULL,
    proteins DOUBLE PRECISION NOT NULL,
    fats DOUBLE PRECISION NOT NULL,
    carbohydrates DOUBLE PRECISION NOT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_nutrition_results_correlation_id ON nutrition_results(correlation_id);
CREATE INDEX idx_nutrition_results_user_id ON nutrition_results(user_id);
