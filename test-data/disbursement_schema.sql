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

/*Table structure for table `check_payments` */



DROP TABLE IF EXISTS `check_payments`;



CREATE TABLE `check_payments` (
  `sTransNox` char(12) NOT NULL,
  `sBranchCd` char(4) DEFAULT NULL,
  `sIndstCdx` char(2) DEFAULT NULL,
  `dTransact` date DEFAULT NULL,
  `sBankIDxx` char(9) DEFAULT NULL,
  `sBnkActID` char(12) DEFAULT NULL,
  `sCheckNox` char(15) DEFAULT NULL,
  `dCheckDte` date DEFAULT NULL,
  `sPayorIDx` char(12) DEFAULT NULL,
  `sPayeeIDx` char(12) DEFAULT NULL,
  `nAmountxx` decimal(14,4) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `sSourceCd` char(4) DEFAULT NULL,
  `sSourceNo` char(14) DEFAULT NULL,
  `cLocation` char(1) DEFAULT NULL,
  `cIsReplcd` char(1) DEFAULT NULL,
  `cReleased` char(1) DEFAULT NULL,
  `cPayeeTyp` char(1) DEFAULT NULL,
  `cDisbMode` char(1) DEFAULT NULL,
  `cClaimant` char(1) DEFAULT NULL,
  `sAuthorze` char(70) DEFAULT NULL,
  `cIsCrossx` char(1) DEFAULT NULL,
  `cIsPayeex` char(1) DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `cProcessd` char(1) DEFAULT NULL,
  `cPrintxxx` char(1) DEFAULT NULL,
  `dPrintxxx` date DEFAULT NULL,
  `sModified` char(30) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*Table structure for table `disbursement_detail` */



DROP TABLE IF EXISTS `disbursement_detail`;



CREATE TABLE `disbursement_detail` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sSourceCd` varchar(4) DEFAULT NULL,
  `sSourceNo` varchar(12) DEFAULT NULL,
  `nDetailNo` smallint(6) DEFAULT NULL,
  `sDetlSrce` varchar(12) DEFAULT NULL,
  `sPrtclrID` varchar(12) DEFAULT NULL,
  `nAmountxx` decimal(14,4) DEFAULT NULL,
  `nAmtAppld` decimal(14,4) DEFAULT NULL,
  `cWithVATx` char(1) DEFAULT NULL,
  `nDetVatSl` decimal(12,4) DEFAULT NULL,
  `nDetVatRa` decimal(5,2) DEFAULT NULL,
  `nDetVatAm` decimal(12,4) DEFAULT NULL,
  `nDetZroVa` decimal(12,4) DEFAULT NULL,
  `nDetVatEx` decimal(12,4) DEFAULT NULL,
  `sTaxCodex` varchar(5) DEFAULT NULL,
  `nTaxRatex` decimal(5,2) DEFAULT NULL,
  `nTaxAmtxx` decimal(10,4) DEFAULT NULL,
  `dTimeStmp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*Table structure for table `disbursement_master` */



DROP TABLE IF EXISTS `disbursement_master`;



CREATE TABLE `disbursement_master` (
  `sTransNox` varchar(12) NOT NULL,
  `sIndstCdx` varchar(2) DEFAULT NULL,
  `sBranchCd` varchar(4) DEFAULT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `dTransact` date DEFAULT NULL,
  `nEntryNox` smallint(6) DEFAULT NULL,
  `sSourceCd` varchar(4) DEFAULT NULL,
  `sSourceNo` varchar(12) DEFAULT NULL,
  `sVouchrNo` varchar(8) DEFAULT NULL,
  `cDisbrsTp` char(1) DEFAULT NULL,
  `sPayeeIDx` varchar(10) DEFAULT NULL,
  `nTranTotl` decimal(14,4) DEFAULT NULL,
  `nDiscTotl` decimal(12,4) DEFAULT NULL,
  `nWTaxTotl` decimal(12,4) DEFAULT NULL,
  `nVATSales` decimal(12,4) DEFAULT NULL,
  `nVATAmtxx` decimal(12,4) DEFAULT NULL,
  `nZroVATSl` decimal(12,4) DEFAULT NULL,
  `nVatExmpt` decimal(12,4) DEFAULT NULL,
  `nNetTotal` decimal(14,4) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `sApproved` varchar(32) DEFAULT NULL,
  `cBankPrnt` char(1) DEFAULT NULL,
  `cPrintxxx` char(1) DEFAULT NULL,
  `dPrintxxx` date DEFAULT NULL,
  `cPrintBIR` char(1) DEFAULT NULL,
  `dPrintBIR` datetime DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

