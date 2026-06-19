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

/*Table structure for table `po_receiving_master` */



DROP TABLE IF EXISTS `po_receiving_master`;



CREATE TABLE `po_receiving_master` (
  `sTransNox` varchar(12) NOT NULL,
  `sBranchCd` varchar(4) NOT NULL,
  `sIndstCdx` varchar(2) NOT NULL,
  `sCategrCd` varchar(7) NOT NULL,
  `sDeptIDxx` varchar(4) DEFAULT NULL,
  `dTransact` date DEFAULT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `sSupplier` varchar(12) DEFAULT NULL,
  `sAddrssID` varchar(12) DEFAULT NULL,
  `sContctID` varchar(12) DEFAULT NULL,
  `sTrucking` varchar(12) DEFAULT NULL,
  `sReferNox` varchar(12) DEFAULT NULL,
  `dRefernce` date DEFAULT NULL,
  `sTermCode` varchar(7) DEFAULT NULL,
  `dTermDuex` date DEFAULT NULL,
  `dDueDatex` date DEFAULT NULL,
  `nDiscount` decimal(5,2) DEFAULT NULL,
  `nAddDiscx` decimal(12,4) DEFAULT NULL,
  `nTranTotl` decimal(12,4) DEFAULT NULL,
  `sSalesInv` varchar(15) DEFAULT NULL,
  `dSalesInv` date DEFAULT NULL,
  `cVATaxabl` char(1) DEFAULT '0',
  `nVATSales` decimal(12,4) DEFAULT NULL,
  `nVATRatex` decimal(5,2) DEFAULT NULL,
  `nVATAmtxx` decimal(12,4) DEFAULT NULL,
  `cTWithHld` char(1) DEFAULT '0',
  `nTWithHld` decimal(5,4) DEFAULT NULL,
  `nZroVATSl` decimal(12,4) DEFAULT NULL,
  `nVATExmpt` decimal(12,4) DEFAULT NULL,
  `nAmtPaidx` decimal(12,4) DEFAULT NULL,
  `nFreightx` decimal(8,2) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `cPurposex` char(1) DEFAULT NULL,
  `cPrintxxx` char(1) DEFAULT '0',
  `nEntryNox` smallint(6) DEFAULT NULL,
  `sInvTypCd` varchar(4) DEFAULT NULL,
  `cProcessd` char(1) DEFAULT '0',
  `cTranStat` char(1) DEFAULT '0',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `po_receiving_detail`;

CREATE TABLE `po_receiving_detail` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sOrderNox` varchar(12) DEFAULT NULL,
  `sStockIDx` varchar(12) NOT NULL,
  `sReplacID` varchar(12) DEFAULT NULL,
  `cUnitType` char(1) DEFAULT NULL,
  `nQuantity` decimal(8,2) DEFAULT NULL,
  `nDiscount` decimal(5,2) DEFAULT NULL,
  `nAddDiscx` decimal(8,4) DEFAULT NULL,
  `nUnitPrce` decimal(12,4) DEFAULT NULL,
  `nFreightx` decimal(8,2) DEFAULT NULL,
  `dExpiryDt` date DEFAULT NULL,
  `nWHCountx` decimal(8,2) DEFAULT NULL,
  `nOrderQty` decimal(8,2) DEFAULT NULL,
  `cWithVATx` char(1) DEFAULT NULL,
  `cSerialze` char(1) NOT NULL,
  `cReversex` char(1) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

