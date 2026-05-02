/*
SQLyog Enterprise - MySQL GUI v8.05 RC 
MySQL - 5.7.44-log : Database - gcasys_dbf
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

DROP TABLE IF EXISTS pettycash;
/*Table structure for table `pettycash` */

CREATE TABLE `pettycash` (
  `sPettyIDx` varchar(7) NOT NULL,
  `sBranchCD` varchar(4) NOT NULL,
  `sDeptIDxx` varchar(3) NOT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `sIndstCdx` varchar(4) DEFAULT NULL,
  `sPettyDsc` varchar(70) DEFAULT NULL,
  `nBalancex` decimal(10,2) DEFAULT NULL,
  `nBegBalxx` decimal(10,2) DEFAULT NULL,
  `dBegDatex` date DEFAULT NULL,
  `sPettyMgr` varchar(12) DEFAULT NULL,
  `nLedgerNo` smallint(6) DEFAULT NULL,
  `dLastTran` date DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sPettyIDx`),
  UNIQUE KEY `uq_branch_dept` (`sBranchCD`,`sDeptIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `pettycash_ledger` */
DROP TABLE IF EXISTS pettycash_ledger;
CREATE TABLE `pettycash_ledger` (
  `sPettyIDx` varchar(7) NOT NULL,
  `nLedgerNo` smallint(6) unsigned DEFAULT NULL,
  `sSourceCD` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `nDebtAmtx` decimal(10,2) DEFAULT NULL,
  `nCrdtAmtx` decimal(10,2) DEFAULT NULL,
  `cReversex` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sPettyIDx`,`sSourceCD`,`sSourceNo`),
  KEY `pcl_id_no` (`sPettyIDx`,`nLedgerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS pettycash_ledger_history;

CREATE TABLE `pettycash_ledger_history` (
  `sPettyIDx` varchar(7) NOT NULL,
  `nLedgerNo` smallint(6) unsigned DEFAULT NULL,
  `sSourceCD` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `nDebtAmtx` decimal(10,2) DEFAULT NULL,
  `nCrdtAmtx` decimal(10,2) DEFAULT NULL,
  `cReversex` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sPettyIDx`,`sSourceCD`,`sSourceNo`),
  KEY `pclh_id_no` (`sPettyIDx`,`nLedgerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;