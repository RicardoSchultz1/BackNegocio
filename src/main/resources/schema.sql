ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS file_hash varchar(64);
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS content_hash varchar(64);
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS status_id int;
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS total_chunks int DEFAULT 0;
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS updated_at timestamp DEFAULT now();

CREATE TABLE IF NOT EXISTS document_status (
	id serial PRIMARY KEY,
	status_name varchar(50) NOT NULL UNIQUE
);

INSERT INTO document_status (status_name)
VALUES
	('UPLOADED'),
	('PROCESSING'),
	('PROCESSED'),
	('FAILED')
ON CONFLICT (status_name) DO NOTHING;

UPDATE arquivo
SET file_hash = md5(random()::text || clock_timestamp()::text)
WHERE file_hash IS NULL;

UPDATE arquivo
SET status_id = (SELECT id FROM document_status WHERE status_name = 'UPLOADED')
WHERE status_id IS NULL;

UPDATE arquivo
SET total_chunks = 0
WHERE total_chunks IS NULL;
