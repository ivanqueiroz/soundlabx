-- MySQL Script generated by MySQL Workbench
-- 01/16/16 22:30:40
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema stsdb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema stsdb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `stsdb` DEFAULT CHARACTER SET utf8 ;
USE `stsdb` ;

-- -----------------------------------------------------
-- Table `stsdb`.`CLINICIAN`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `stsdb`.`CLINICIAN` (
  `ID_CLINICIAN` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(400) NULL,
  PRIMARY KEY (`ID_CLINICIAN`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `stsdb`.`PATIENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `stsdb`.`PATIENT` (
  `ID_PATIENT` INT NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(400) NULL,
  `ID_CLINICIAN` INT NOT NULL,
  PRIMARY KEY (`ID_PATIENT`),
  INDEX `fk_PATIENT_CLINICIAN1_idx` (`ID_CLINICIAN` ASC),
  CONSTRAINT `fk_PATIENT_CLINICIAN1`
    FOREIGN KEY (`ID_CLINICIAN`)
    REFERENCES `stsdb`.`CLINICIAN` (`ID_CLINICIAN`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `stsdb`.`EXERCISE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `stsdb`.`EXERCISE` (
  `ID_EXERCISE` INT NOT NULL AUTO_INCREMENT,
  `DT_EXERCISE` DATETIME(2) NULL,
  `SAMPLES` VARCHAR(4000) NULL,
  `ID_PATIENT` INT NOT NULL,
  PRIMARY KEY (`ID_EXERCISE`),
  INDEX `fk_EXERCISE_PATIENT1_idx` (`ID_PATIENT` ASC),
  CONSTRAINT `fk_EXERCISE_PATIENT1`
    FOREIGN KEY (`ID_PATIENT`)
    REFERENCES `stsdb`.`PATIENT` (`ID_PATIENT`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
