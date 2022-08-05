create table if not exists health.patient
  (id               uuid primary key not null,
   fname            text             not null,
   lname            text             not null,
   mname            text,
   address          text,
   gender           text             not null,
   insurance_policy text,
   birth_date       timestamp,
   created_at       timestamp        not null default now(),
   updated_at       timestamp        not null default now()
  );
--;;

comment on table health.patient is 'Stores patient information';
--;;

create index on health.patient (id);
--;;
