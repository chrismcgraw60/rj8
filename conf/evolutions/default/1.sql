# --- !Ups

CREATE TABLE TestSuite (
  id			bigint SERIAL not null,
  uuid			varchar(64),
  className		varchar(1024),
  time			varchar(32),
  folder		varchar(256),
  file			varchar(256),
  tests			bigint,
  failures		bigint,
  errors		bigint,
  skipped		bigint,
  timestamp		timestamp,
  
  CONSTRAINT testSuiteyPK PRIMARY KEY (id)
);

CREATE TABLE TestEntry (
  id			bigint SERIAL not null,
  uuid			varchar(64),
  className		varchar(1024),
  methodName	varchar(1024),
  time			varchar(32),
  status		varchar(8),
  failException	varchar(512),
  failMessage	varchar(1024),
  failDetail	CLOB,
  suite_Id		bigint,
  
  CONSTRAINT testEntryPK PRIMARY KEY (id),
  CONSTRAINT testSuiteFK_integrity CHECK (suite_Id IS NOT NULL),
  CONSTRAINT testSuiteFK FOREIGN KEY (suite_Id)
  	REFERENCES TestSuite (id)
  	ON DELETE CASCADE
);

# --- !Downs

DROP TABLE IF EXISTS TestSuite;
DROP TABLE IF EXISTS TestEntry;
