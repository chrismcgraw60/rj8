# --- !Ups

create table TESTENTRY (
  id		bigint not null,
  title		varchar(255))
;

insert into testEntry (id, title) values (1, 'testFoo');
insert into testEntry (id, title) values (2, 'testBar');
insert into testEntry (id, title) values (3, 'testMinx');
insert into testEntry (id, title) values (4, 'testManchu');

# --- !Downs

drop table if exists testEntry;