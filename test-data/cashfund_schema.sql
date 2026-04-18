/*
SQLyog Enterprise - MySQL GUI v8.05 RC 
MySQL - 5.7.44-log : Database - gcasys_dbf
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Table structure for table `cashfund` */

CREATE TABLE `cashfund` (
  `sCashFIDx` varchar(15) NOT NULL,
  `sBranchCD` varchar(4) DEFAULT NULL,
  `sDeptIDxx` varchar(3) DEFAULT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `sIndstCdx` varchar(4) DEFAULT NULL,
  `sCashFDsc` varchar(70) DEFAULT NULL,
  `nBalancex` decimal(10,2) DEFAULT NULL,
  `nBegBalxx` decimal(10,2) DEFAULT NULL,
  `dBegDatex` date DEFAULT NULL,
  `sCashFMgr` varchar(12) DEFAULT NULL,
  `nLedgerNo` smallint(6) DEFAULT NULL,
  `dLastTran` date DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` varchar(10) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sCashFIDx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cashfund_ledger` */

CREATE TABLE `cashfund_ledger` (
  `sCashFIDx` varchar(15) NOT NULL,
  `nLedgerNo` smallint(6) DEFAULT NULL,
  `sSourceCD` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `nDebtAmtx` decimal(10,2) DEFAULT NULL,
  `nCrdtAmtx` decimal(10,2) DEFAULT NULL,
  `cReversex` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sCashFIDx`,`sSourceCD`,`sSourceNo`),
  KEY `cfl_id_no` (`sCashFIDx`,`nLedgerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `cashfund_ledger_history` (
  `sCashFIDx` varchar(15) NOT NULL,
  `nLedgerNo` smallint(6) DEFAULT NULL,
  `sSourceCD` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `nDebtAmtx` decimal(10,2) DEFAULT NULL,
  `nCrdtAmtx` decimal(10,2) DEFAULT NULL,
  `cReversex` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sCashFIDx`,`sSourceCD`,`sSourceNo`),
  KEY `cflh_id_no` (`sCashFIDx`,`nLedgerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;