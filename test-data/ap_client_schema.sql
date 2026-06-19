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

/*Table structure for table `ap_client_master` */


DROP TABLE IF EXISTS `ap_client_master`;

CREATE TABLE `ap_client_master` (
  `sClientID` varchar(12) NOT NULL,
  `sAddrssID` varchar(12) DEFAULT NULL,
  `sContctID` varchar(12) DEFAULT NULL,
  `sCategrCd` varchar(7) NOT NULL,
  `dCltSince` date DEFAULT NULL,
  `dBegDatex` date DEFAULT NULL,
  `nBegBalxx` decimal(13,2) DEFAULT NULL,
  `sTermIDxx` varchar(7) DEFAULT NULL,
  `nDiscount` decimal(5,2) DEFAULT NULL,
  `nCredLimt` decimal(10,2) DEFAULT NULL,
  `nABalance` decimal(13,2) DEFAULT NULL,
  `nOBalance` decimal(13,2) DEFAULT NULL,
  `nLedgerNo` varchar(6) DEFAULT NULL,
  `cVatablex` char(1) DEFAULT '0',
  `cHoldAcct` char(1) DEFAULT NULL,
  `cAutoHold` char(1) DEFAULT NULL,
  `sRemarksx` varchar(128) DEFAULT NULL,
  `sEmailAdd` varchar(128) DEFAULT NULL,
  `cAutoSend` char(1) DEFAULT NULL,
  `cPaymOptx` char(1) DEFAULT NULL,
  `cHoldOrdr` char(1) DEFAULT NULL,
  `cVATRegis` char(1) DEFAULT NULL,
  `cPermitxx` char(1) DEFAULT NULL,
  `cBackOrdr` char(1) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '0',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sClientID`,`sCategrCd`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `ap_client_ledger` */

DROP TABLE IF EXISTS `ap_client_ledger`;

CREATE TABLE `ap_client_ledger` (
  `sClientID` varchar(12) NOT NULL,
  `sCategrCd` varchar(7) NOT NULL,
  `nLedgerNo` varchar(6) NOT NULL,
  `dTransact` date NOT NULL,
  `sSourceCd` varchar(4) NOT NULL,
  `sSourceNo` varchar(12) NOT NULL,
  `nAmountIn` decimal(10,2) DEFAULT NULL,
  `nAmountOt` decimal(10,2) DEFAULT NULL,
  `dPostedxx` date DEFAULT NULL,
  `nABalance` decimal(13,2) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sClientID`,`sCategrCd`,`sSourceCd`,`sSourceNo`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

