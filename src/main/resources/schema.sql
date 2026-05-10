ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS file_hash varchar(64);
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS content_hash varchar(64);
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS status_id int;
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS total_chunks int DEFAULT 0;
ALTER TABLE IF EXISTS arquivo ADD COLUMN IF NOT EXISTS updated_at timestamp DEFAULT now();
ALTER TABLE IF EXISTS usuario ADD COLUMN IF NOT EXISTS ativo boolean DEFAULT true;

UPDATE usuario
SET ativo = true
WHERE ativo IS NULL;

ALTER TABLE IF EXISTS usuario ALTER COLUMN ativo SET DEFAULT true;
ALTER TABLE IF EXISTS usuario ALTER COLUMN ativo SET NOT NULL;

CREATE TABLE IF NOT EXISTS document_status (
	id serial PRIMARY KEY,
	status_name varchar(50) NOT NULL UNIQUE
);

INSERT INTO document_status (id, status_name)
VALUES
	(1, 'UPLOADED'),
	(2, 'PROCESSING'),
	(3, 'PROCESSED'),
	(4, 'FAILED')
ON CONFLICT (id) DO UPDATE SET status_name = EXCLUDED.status_name;

UPDATE arquivo
SET file_hash = md5(random()::text || clock_timestamp()::text)
WHERE file_hash IS NULL;

UPDATE arquivo
SET status_id = (SELECT id FROM document_status WHERE status_name = 'UPLOADED')
WHERE status_id IS NULL;

UPDATE arquivo
SET total_chunks = 0
WHERE total_chunks IS NULL;

CREATE TABLE IF NOT EXISTS document_chunks (
	id bigserial PRIMARY KEY,
	document_id integer NOT NULL,
	chunk_index int NOT NULL,
	page_number int,
	chunk_text text NOT NULL,
	embedding vector(384),
	created_at timestamp default now(),
	CONSTRAINT fk_chunks_document
		FOREIGN KEY (document_id)
		REFERENCES arquivo(id)
		ON DELETE CASCADE
);
