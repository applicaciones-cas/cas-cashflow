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
/*Table structure for table `document_mapping` */

DROP TABLE IF EXISTS `document_mapping`;

CREATE TABLE `document_mapping` (
  `sDocCodex` char(8) NOT NULL,
  `sDescript` varchar(32) DEFAULT '(NULL)',
  `nEntryNox` smallint(6) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` varchar(10) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sDocCodex`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `document_mapping_detail` */

DROP TABLE IF EXISTS `document_mapping_detail`;

CREATE TABLE `document_mapping_detail` (
  `sDocCodex` char(8) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sFieldCde` char(9) DEFAULT '(NULL)',
  `cMultiple` char(1) DEFAULT '0',
  `cFixedVal` char(1) DEFAULT '0',
  `sFontName` varchar(32) DEFAULT '(NULL)',
  `nFontSize` smallint(6) DEFAULT NULL,
  `nTopRowxx` decimal(6,2) DEFAULT NULL,
  `nLeftColx` decimal(6,2) DEFAULT NULL,
  `nMaxLenxx` smallint(6) DEFAULT NULL,
  `nMaxRowxx` smallint(6) DEFAULT '1',
  `nRowSpace` decimal(6,2) DEFAULT '1.00',
  `nColSpace` decimal(6,2) DEFAULT '1.00',
  `nPageLocx` smallint(6) DEFAULT '1',
  `sModified` varchar(10) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sDocCodex`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
