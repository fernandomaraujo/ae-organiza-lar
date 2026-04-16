-- V2__Task_Completions.sql
-- Tabela para rastrear conclusao de tarefas por data

CREATE TABLE task_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    completion_date DATE NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT uq_task_completion_date UNIQUE (task_id, completion_date)
);

CREATE INDEX idx_task_completions_task_id ON task_completions(task_id);
CREATE INDEX idx_task_completions_date ON task_completions(completion_date);

