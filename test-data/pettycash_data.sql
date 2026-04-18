/*
SQLyog Enterprise - MySQL GUI v8.05 RC 
MySQL - 5.7.44-log : Database - gcasys_dbf
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Data for the table `pettycash` */

insert  into `pettycash`(`sPettyIDx`,`sBranchCD`,`sDeptIDxx`,`sCompnyID`,`sIndstCdx`,`sPettyDsc`,`nBalancex`,`nBegBalxx`,`dBegDatex`,`sPettyMgr`,`nLedgerNo`,`dLastTran`,`cTranStat`,`sModified`,`dModified`,`dTimeStmp`) values ('GCO1026','GCO1','026',NULL,NULL,'Rsie Test','1000.00','1000.00','2026-04-06','GGC_BGCO1',2,NULL,'0','M001000001','2026-04-06 09:18:10','2026-04-17 11:26:56');

/*Data for the table `pettycash_ledger` */

insert  into `pettycash_ledger`(`sPettyIDx`,`nLedgerNo`,`sSourceCD`,`sSourceNo`,`dTransact`,`nDebtAmtx`,`nCrdtAmtx`,`cReversex`,`dModified`,`dTimeStmp`) values ('GCO1026',1,'PDsb','01','2026-04-07','1000.00','0.00','+',NULL,'2026-04-16 11:20:58'),('GCO1026',2,'PDsb','02','2026-04-07','0.00','1000.00','+',NULL,'2026-04-16 11:20:59');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;