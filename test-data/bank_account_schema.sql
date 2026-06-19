/*

SQLyog Ultimate v8.55 
MySQL - 5.7.44-log : Database - gcasys_dbf

*********************************************************************

*/



/*!40101 SET NAMES utf8 */;



/*!40101 SET SQL_MODE=''*/;



/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*Table structure for table `bank_account_ledger` */



DROP TABLE IF EXISTS `bank_account_ledger`;



CREATE TABLE `bank_account_ledger` (
  `sBnkActID` varchar(12) NOT NULL,
  `nLedgerNo` int(11) NOT NULL,
  `sBranchCd` varchar(4) DEFAULT NULL,
  `dTransact` date DEFAULT NULL,
  `cPaymForm` char(1) DEFAULT NULL,
  `sReferNox` varchar(32) DEFAULT NULL,
  `sSourceCd` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `nAmountIn` decimal(12,2) DEFAULT NULL,
  `nAmountOt` decimal(12,2) DEFAULT NULL,
  `dPostedxx` date DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sBnkActID`,`sSourceCd`,`sSourceNo`),
  KEY `sBnkActID` (`sBnkActID`,`nLedgerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*Table structure for table `bank_account_master` */



DROP TABLE IF EXISTS `bank_account_master`;



CREATE TABLE `bank_account_master` (
  `sBnkActID` char(12) NOT NULL,
  `sIndstCdx` char(2) DEFAULT NULL,
  `sBranchCd` char(4) DEFAULT NULL,
  `sCompnyID` char(4) DEFAULT NULL,
  `sBankIDxx` char(9) DEFAULT NULL,
  `sActNumbr` char(16) DEFAULT NULL,
  `sActNamex` char(64) DEFAULT NULL,
  `sAcctCode` char(7) DEFAULT NULL,
  `cAcctType` char(1) DEFAULT NULL,
  `sPartnrAc` char(12) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `sCheckNox` char(15) DEFAULT NULL,
  `dBegBalxx` date DEFAULT NULL,
  `nOBegBalx` decimal(14,4) DEFAULT NULL,
  `nABegBalx` decimal(14,4) DEFAULT NULL,
  `nOBalance` decimal(14,4) DEFAULT NULL,
  `nABalance` decimal(14,4) DEFAULT NULL,
  `dDueDatex` date DEFAULT NULL,
  `cBankPrnt` char(1) DEFAULT NULL,
  `cMonitorx` char(1) DEFAULT NULL,
  `cDefaultx` char(1) DEFAULT NULL,
  `nClearDay` smallint(6) DEFAULT NULL,
  `nSgnatory` smallint(6) DEFAULT NULL,
  `dLastTran` date DEFAULT NULL,
  `dLastPost` date DEFAULT NULL,
  `sBranchxx` varchar(40) DEFAULT NULL,
  `sSerialNo` varchar(10) DEFAULT NULL,
  `sSlipType` varchar(2) DEFAULT 'DS',
  `cRecdStat` char(1) DEFAULT NULL,
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sBnkActID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

