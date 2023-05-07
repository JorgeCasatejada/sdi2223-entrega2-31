package com.uniovi.sdi2223entrega2test31.pageobjects;

import com.uniovi.sdi2223entrega2test31.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_ChatView extends PO_NavView {
    static public void createMessage(WebDriver driver, String message) {
        SeleniumUtils.waitLoadElementsBy(driver, "id","newMessage", getTimeout());
        WebElement email = driver.findElement(By.id("newMessage"));
        email.click();
        email.clear();
        email.sendKeys(message);
        //Pulsar el boton de Alta.
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }
}
