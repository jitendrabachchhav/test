DROP TABLE IF EXISTS trade;

CREATE TABLE trade(
id VARCHAR2(20) NOT NULL,
version INTEGER NOT NULL,
counterPartyId VARCHAR2(20) NOT NULL,
bookId VARCHAR2(20) NULL,
maturityDate DATE NOT NULL,
createdOn DATE NOT NULL,
expired BOOLEAN NOT NULL,
PRIMARY KEY (id, version)
);

CREATE INDEX createdOnIndex ON trade(createdOn);