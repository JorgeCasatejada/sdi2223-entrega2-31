package com.uniovi.sdi2223entrega2test31.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_SignUpView extends PO_NavView {
    static public void fillForm(WebDriver driver, String emailp, String namep, String surnamep, String
            datep, String passwordp, String passwordconfp) {
        WebElement email = driver.findElement(By.name("email"));
        email.click();
        email.clear();
        email.sendKeys(emailp);
        WebElement name = driver.findElement(By.name("name"));
        name.click();
        name.clear();
        name.sendKeys(namep);
        WebElement surname = driver.findElement(By.name("surname"));
        surname.click();
        surname.clear();
        surname.sendKeys(surnamep);
        WebElement date = driver.findElement(By.name("date"));
        date.click();
        date.clear();
        date.sendKeys(datep);
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);
        WebElement passwordConfirm = driver.findElement(By.name("passwordConfirm"));
        passwordConfirm.click();
        passwordConfirm.clear();
        passwordConfirm.sendKeys(passwordconfp);
        //Pulsar el boton de Alta.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }

}
