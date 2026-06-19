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

/*Table structure for table `ap_payment_master` */



DROP TABLE IF EXISTS `ap_payment_master`;



CREATE TABLE `ap_payment_master` (
  `sTransNox` char(12) NOT NULL,
  `sIndstCdx` char(2) NOT NULL,
  `sBranchCd` char(4) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `sCompnyID` char(4) NOT NULL,
  `sClientID` char(12) NOT NULL,
  `sSOANoxxx` char(6) NOT NULL,
  `sIssuedTo` char(12) DEFAULT NULL,
  `sRemarksx` char(128) DEFAULT NULL,
  `nTranTotl` decimal(14,4) DEFAULT NULL,
  `nFreightx` decimal(8,2) DEFAULT NULL,
  `nDiscAmnt` decimal(8,2) DEFAULT NULL,
  `nVATAmtxx` decimal(12,4) DEFAULT NULL,
  `nVatExmpt` decimal(12,4) DEFAULT NULL,
  `nZeroRted` decimal(12,4) DEFAULT NULL,
  `nTaxAmntx` decimal(12,4) DEFAULT NULL,
  `nNetTotal` decimal(14,4) DEFAULT NULL,
  `nAmtPaidX` decimal(14,4) DEFAULT NULL,
  `nEntryNox` smallint(6) DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `cProcessd` char(1) DEFAULT '0',
  `sModified` char(30) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `ap_payment_adjustment`;

CREATE TABLE `ap_payment_adjustment` (
  `sTransNox` char(12) NOT NULL,
  `sIndstCdx` char(2) NOT NULL,
  `sBranchCd` char(4) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `sCompnyID` char(4) NOT NULL,
  `sClientID` char(12) NOT NULL,
  `sIssuedTo` char(12) DEFAULT NULL,
  `sReferNox` char(6) NOT NULL,
  `cPayerCde` char(1) DEFAULT NULL,
  `sRemarksx` char(128) DEFAULT NULL,
  `sSourceCd` char(4) DEFAULT NULL,
  `sSourceNo` char(12) DEFAULT NULL,
  `nDebitAmt` decimal(14,4) DEFAULT NULL,
  `nCredtAmt` decimal(14,4) DEFAULT NULL,
  `nAppliedx` decimal(14,4) DEFAULT NULL,
  `nTranTotl` decimal(14,4) DEFAULT NULL,
  `nFreightx` decimal(10,4) DEFAULT NULL,
  `nDiscAmnt` decimal(10,4) DEFAULT NULL,
  `nVATAmtxx` decimal(12,4) DEFAULT NULL,
  `nVatExmpt` decimal(12,4) DEFAULT NULL,
  `nZeroRted` decimal(12,4) DEFAULT NULL,
  `nTaxAmntx` decimal(12,4) DEFAULT NULL,
  `nNetTotal` decimal(14,4) DEFAULT NULL,
  `cProcessd` char(1) DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` char(30) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

