package com.uniovi.sdi2223entrega2test31.pageobjects;

import com.uniovi.sdi2223entrega2test31.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class PO_PrivateView extends PO_NavView {
    static public void fillFormAddOffer(WebDriver driver, String titlep, String descriptionp,
                                        int pricep, boolean featured) {
        WebElement title = driver.findElement(By.name("title"));
        title.clear();
        title.sendKeys(titlep);
        WebElement description = driver.findElement(By.name("description"));
        description.clear();
        description.sendKeys(descriptionp);
        WebElement price = driver.findElement(By.name("price"));
        price.click();
        price.clear();
        price.sendKeys(pricep + "");
        if (featured){
            WebElement dest = driver.findElement(By.name("highlight"));
            dest.click();
        }
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }
}
