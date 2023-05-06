package com.uniovi.sdi2223entrega2test31.pageobjects;

import com.uniovi.sdi2223entrega2test31.util.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_HomeView extends PO_NavView {
    static public void checkWelcomeToPage(WebDriver driver) {
        //Esperamos a que se cargue el saludo de bienvenida en Español
        SeleniumUtils.waitLoadElementsBy(driver, "text", "myWallapop",
                getTimeout());
    }

    static public List<WebElement> getWelcomeMessageText(WebDriver driver, int language) {
        //Esperamos a que se cargue el saludo de bienvenida en Español
        return SeleniumUtils.waitLoadElementsBy(driver, "text", p.getString("welcome.message", language),
                getTimeout());
    }

}
