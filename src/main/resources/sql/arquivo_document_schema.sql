create table if not exists document_status (
    id serial primary key,
    status_name varchar(50) not null unique
);

insert into document_status (id, status_name)
values
    (1, 'UPLOADED'),
    (2, 'PROCESSING'),
    (3, 'PROCESSED'),
    (4, 'FAILED')
on conflict (id) do update set status_name = excluded.status_name;

alter table arquivo
    add column if not exists file_hash varchar(64),
    add column if not exists content_hash varchar(64),
    add column if not exists status_id int,
    add column if not exists total_chunks int default 0,
    add column if not exists updated_at timestamp default now();

update arquivo
set status_id = (
    select id from document_status where status_name = 'UPLOADED'
)
where status_id is null;

update arquivo
set file_hash = md5(random()::text || clock_timestamp()::text)
where file_hash is null;

alter table arquivo
    alter column file_hash set not null,
    alter column status_id set not null;

alter table arquivo
    add constraint if not exists uk_arquivo_file_hash unique (file_hash);

alter table arquivo
    add constraint if not exists fk_document_status
        foreign key (status_id)
        references document_status(id);

create table if not exists document_chunks (
    id bigserial primary key,
    document_id integer not null,
    chunk_index int not null,
    page_number int,
    chunk_text text not null,
    embedding vector(384),
    created_at timestamp default now(),
    constraint fk_chunks_document
        foreign key (document_id)
        references arquivo(id)
        on delete cascade
);
