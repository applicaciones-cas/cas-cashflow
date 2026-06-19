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

/*Table structure for table `cache_payable` */


/*Table structure for table `cache_payable_detail` */

DROP TABLE IF EXISTS `cache_payable_detail`;

CREATE TABLE `cache_payable_detail` (
  `sTransNox` char(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sTranType` char(30) NOT NULL,
  `nGrossAmt` decimal(14,4) DEFAULT NULL,
  `nDiscAmtx` decimal(10,4) DEFAULT NULL,
  `nDeductnx` decimal(10,4) DEFAULT NULL,
  `nPayables` decimal(14,4) DEFAULT NULL,
  `nRecvbles` decimal(14,4) DEFAULT NULL,
  `nAmtPaidx` decimal(14,4) DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `cache_payable_master` */

DROP TABLE IF EXISTS `cache_payable_master`;

CREATE TABLE `cache_payable_master` (
  `sTransNox` char(12) NOT NULL,
  `sIndstCdx` char(2) NOT NULL,
  `sBranchCd` char(4) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `sCompnyID` char(4) NOT NULL,
  `nEntryNox` smallint(6) DEFAULT NULL,
  `sClientID` char(12) NOT NULL,
  `dDueDatex` date DEFAULT NULL,
  `sBankIDxx` char(9) DEFAULT NULL,
  `sInsurIDx` char(9) DEFAULT NULL,
  `sSourceCd` char(4) NOT NULL,
  `sSourceNo` char(12) NOT NULL,
  `sReferNox` char(12) DEFAULT NULL,
  `cPayerCde` char(1) DEFAULT NULL,
  `nGrossAmt` decimal(14,4) DEFAULT NULL,
  `nFreightx` decimal(10,4) DEFAULT NULL,
  `nDiscAmnt` decimal(10,4) DEFAULT NULL,
  `nVATSales` decimal(10,4) DEFAULT NULL,
  `nVATRatex` decimal(10,4) DEFAULT NULL,
  `nVATAmtxx` decimal(10,4) DEFAULT NULL,
  `nVatExmpt` decimal(10,4) DEFAULT NULL,
  `nZeroRted` decimal(10,4) DEFAULT NULL,
  `nTaxAmntx` decimal(10,4) DEFAULT NULL,
  `nNetTotal` decimal(14,4) DEFAULT NULL,
  `nPayables` decimal(14,4) DEFAULT NULL,
  `nRecvbles` decimal(14,4) DEFAULT NULL,
  `nAmtPaidx` decimal(14,4) DEFAULT NULL,
  `cSectionx` char(1) DEFAULT NULL,
  `cWithSOAx` char(1) DEFAULT '0',
  `cProcessd` char(1) DEFAULT '0',
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


