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

/*Table structure for table `po_master` */



DROP TABLE IF EXISTS `po_master`;



CREATE TABLE `po_master` (
  `sTransNox` varchar(12) NOT NULL,
  `sBranchCd` varchar(4) NOT NULL,
  `sIndstCdx` varchar(2) NOT NULL,
  `sCategrCd` varchar(7) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `sDestinat` varchar(4) DEFAULT NULL,
  `sSupplier` varchar(12) DEFAULT NULL,
  `sAddrssID` varchar(12) DEFAULT NULL,
  `sContctID` varchar(12) DEFAULT NULL,
  `sReferNox` varchar(25) DEFAULT NULL,
  `sTermCode` varchar(7) DEFAULT NULL,
  `nDiscount` decimal(5,2) DEFAULT NULL,
  `nAddDiscx` decimal(12,4) DEFAULT NULL,
  `nTranTotl` decimal(12,4) DEFAULT NULL,
  `nAmtPaidx` decimal(12,4) DEFAULT NULL,
  `cWithAddx` char(1) DEFAULT '0',
  `nDPRatexx` decimal(5,2) DEFAULT NULL,
  `nAdvAmtxx` decimal(12,4) DEFAULT NULL,
  `nNetTotal` decimal(12,4) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `dExpected` date DEFAULT NULL,
  `cEmailSnt` char(1) DEFAULT '0',
  `nEmailSnt` smallint(6) DEFAULT NULL,
  `cPrintxxx` char(1) DEFAULT '0',
  `nEntryNox` smallint(6) DEFAULT NULL,
  `sInvTypCd` char(4) DEFAULT NULL,
  `cPreOwned` char(1) DEFAULT '0',
  `cProcessd` char(1) DEFAULT '0',
  `cTranStat` char(1) DEFAULT '0',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

