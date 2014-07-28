# --- !Ups

CREATE TABLE Folder (
  id			bigint SERIAL not null,
  path			varchar(2056),
  status		varchar(32),
  createdOn		timestamp,
  updatedOn		timestamp,
  
  CONSTRAINT folderPK PRIMARY KEY (id),
  CONSTRAINT uniqueFolderPath UNIQUE (path)
);

CREATE TABLE TestSuite (
  id			bigint SERIAL not null,
  uuid			varchar(64),
  packageName	varchar(2056),
  className		varchar(1024),
  time			varchar(32),
  folder		varchar(2056),
  file			varchar(256),
  tests			bigint,
  failures		bigint,
  errors		bigint,
  skipped		bigint,
  timestamp		timestamp,
  folder_Id		bigint,
  
  CONSTRAINT testSuiteyPK PRIMARY KEY (id),
  CONSTRAINT folderFK_integrity CHECK (folder_Id IS NOT NULL),
  CONSTRAINT folderFK FOREIGN KEY (folder_Id)
  	REFERENCES Folder (id)
  	ON DELETE CASCADE
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

DROP TABLE IF EXISTS Folder;
DROP TABLE IF EXISTS TestSuite;
DROP TABLE IF EXISTS TestEntry;
