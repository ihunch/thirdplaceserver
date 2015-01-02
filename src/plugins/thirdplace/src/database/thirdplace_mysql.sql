#As OpenFire plugin requirement, need insert this into the sqltable
INSERT INTO ofVersion (name, version) VALUES ('thirdplace',0);

CREATE TABLE IF NOT EXISTS `openfire`.`thirdplaceHangout` (
  `hangoutid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `createUser` VARCHAR(64) NOT NULL COMMENT 'createUser links to OfUserID',
  `createDate` CHAR(15) NOT NULL,
  `closed` TINYINT(4) NOT NULL,
  `timeconfirmed` TINYINT(4) NOT NULL,
  `locationconfirmed` TINYINT(4) NOT NULL,
  PRIMARY KEY (`hangoutid`),
  UNIQUE INDEX `hangoutid_UNIQUE` (`hangoutid` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `openfire`.`thirdplaceHangoutLocation` (
  `locationid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `foursquareLocationid` BIGINT(20) NULL DEFAULT NULL COMMENT 'Four Square Location ID\n',
  `locationconfirm` TINYINT(4) NOT NULL,
  `hangoutid` BIGINT(20) NOT NULL,
  `createUser` VARCHAR(64) NOT NULL,
  `updateUser` VARCHAR(64) NULL,
  `createTime` CHAR(15) NOT NULL,
  `updateTime` CHAR(15) NULL,
  PRIMARY KEY (`locationid`),
  UNIQUE INDEX `locationid_UNIQUE` (`locationid` ASC),
  INDEX `FK_Location_Hangout_idx` (`hangoutid` ASC),
  CONSTRAINT `FK_Location_Hangout`
    FOREIGN KEY (`hangoutid`)
    REFERENCES `openfire`.`thirdplaceHangout` (`hangoutid`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `openfire`.`thirdplaceHangoutMessage` (
  `messageid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `content` MEDIUMTEXT NULL DEFAULT NULL,
  `createTime` CHAR(15) NOT NULL,
  `createUser` VARCHAR(64) NOT NULL,
  `updateTime` CHAR(15) NULL DEFAULT NULL,
  `updateUser` VARCHAR(64) NULL DEFAULT NULL,
  `hangoutid` BIGINT(20) NOT NULL,
  PRIMARY KEY (`messageid`),
  UNIQUE INDEX `messageid_UNIQUE` (`messageid` ASC),
  INDEX `FK_Message_Hangout_idx` (`hangoutid` ASC),
  CONSTRAINT `FK_Message_Hangout`
    FOREIGN KEY (`hangoutid`)
    REFERENCES `openfire`.`thirdplaceHangout` (`hangoutid`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `openfire`.`thirdplaceHangoutTime` (
  `hangouttimeid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `timeDescription` VARCHAR(64) NOT NULL,
  `startdate` CHAR(15) NOT NULL,
  `enddate` CHAR(15) NULL DEFAULT NULL,
  `createTime` CHAR(15) NOT NULL,
  `updateTime` CHAR(15) NULL DEFAULT NULL,
  `createUser` VARCHAR(64) NOT NULL,
  `updateUser` VARCHAR(64) NULL DEFAULT NULL,
  `timeConfirmed` TINYINT(4) NOT NULL,
  `hangoutid` BIGINT(20) NOT NULL,
  PRIMARY KEY (`hangoutimeid`),
  UNIQUE INDEX `hangoutimeid_UNIQUE` (`hangoutimeid` ASC),
  INDEX `hangoutid_idx` (`hangoutid` ASC),
  CONSTRAINT `FK_Time_Hangout`
    FOREIGN KEY (`hangoutid`)
    REFERENCES `openfire`.`thirdplaceHangout` (`hangoutid`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `openfire`.`thirdplaceHangoutUser` (
  `hangoutuserid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `jid` VARCHAR(1024) NOT NULL,
  `goingstatus` VARCHAR(20) NOT NULL,
  `hangoutid` BIGINT(20) NOT NULL,
  PRIMARY KEY (`hangouruserid`),
  UNIQUE INDEX `hangouruserid_UNIQUE` (`hangouruserid` ASC),
  INDEX `hangoutid_idx` (`hangoutid` ASC),
  CONSTRAINT `FK_User_Hangout`
    FOREIGN KEY (`hangoutid`)
    REFERENCES `openfire`.`thirdplaceHangout` (`hangoutid`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;