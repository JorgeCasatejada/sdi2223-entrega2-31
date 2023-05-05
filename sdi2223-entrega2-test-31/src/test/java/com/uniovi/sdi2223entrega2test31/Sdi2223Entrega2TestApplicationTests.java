package com.uniovi.sdi2223entrega2test31;

import com.uniovi.sdi2223entrega2test31.pageobjects.*;
import com.uniovi.sdi2223entrega2test31.util.SeleniumUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Sdi2223Entrega2TestApplicationTests {
    //ALEX
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    static String Geckodriver = "C:\\Users\\alexr\\OneDrive\\Escritorio\\geckodriver-v0.30.0-win64.exe";
    //JORGE
//    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
//    static String Geckodriver = "C:\\Users\\jorge\\OneDrive\\Escritorio\\SDI\\Practica\\Sesión6\\PL-SDI-Sesión5-material\\PL-SDI-Sesion5-material\\geckodriver-v0.30.0-win64.exe";
    //PATRI
//    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
//    static String Geckodriver = "C:\\Users\\patri\\Desktop\\GitHub\\SDI\\grupo\\geckodriver-v0.30.0-win64.exe";
    //ENRIQUE
//    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
//    static String Geckodriver = "C:\\Program Files\\Gekodriver\\geckodriver-v0.30.0-win64.exe";

//static String Geckodriver = "/Users/USUARIO/selenium/geckodriver-v0.30.0-macos";
    //Común a Windows y a MACOSX
    static WebDriver driver = getDriver(PathFirefox, Geckodriver);
    static String URL = "http://localhost:8080";

    public static WebDriver getDriver(String PathFirefox, String Geckodriver) {
        System.setProperty("webdriver.firefox.bin", PathFirefox);
        System.setProperty("webdriver.gecko.driver", Geckodriver);
        driver = new FirefoxDriver();
        return driver;
    }

    @BeforeEach
    public void setUp() {
        driver.navigate().to(URL);
    }

    //Después de cada prueba se borran las cookies del navegador
    @AfterEach
    public void tearDown() {
        driver.manage().deleteAllCookies();
    }

    //Antes de la primera prueba
    @BeforeAll
    static public void begin() {
    }

    //Al finalizar la última prueba
    @AfterAll
    static public void end() {
        //Cerramos el navegador al finalizar las pruebas
        driver.quit();
    }

    //  [Prueba1] Registro de Usuario con datos válidos.
    @Test
    @Order(1)
    void PR01() {
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "email@email.com", "Pepe", "Perez Gonzalez",
                "2002-05-05", "123456", "123456");
        //Comprobamos que entramos en la sección privada y nos nuestra el texto a buscar
        String checkText = "Mis ofertas";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba2] Registro de Usuario con datos inválidos (email, nombre, apellidos y fecha de nacimiento vacíos).
    @Test
    @Order(2)
    public void PR02() {
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "", "", "","", "123456", "123456");
        //Comprobamos que sigue en la página de registro
        String checkText = "Registrar usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba3] Registro de Usuario con datos inválidos (repetición de contraseña inválida).
    @Test
    @Order(3)
    public void PR03() {
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "email1@email.com", "Pepe", "Perez Gonzalez",
                "2002-05-05", "123456", "654321");
        //Comprobamos que sigue en la página de registro con el mensaje de error correspondiente
        String checkText = "Registrar usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        String checkText2 = "Las contraseñas no coinciden";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "text", checkText2);
        Assertions.assertEquals(checkText2, result2.get(0).getText());
    }

    //  [Prueba4] Registro de Usuario con datos inválidos (email existente).
    @Test
    @Order(4)
    public void PR04() {
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario con user01 -> usuario existente del sistema
        PO_SignUpView.fillForm(driver, "user01@email.com", "Pepe", "Perez Gonzalez",
                "2002-05-05", "123456", "123456");
        //Comprobamos que sigue en la página de registro con el mensaje de error correspondiente
        String checkText = "Registrar usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        String checkText2 = "Este correo ya está en uso";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "text", checkText2);
        Assertions.assertEquals(checkText2, result2.get(0).getText());
    }

    //  [Prueba5] Inicio de sesión con datos válidos (administrador).
    @Test
    @Order(5)
    public void PR05() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Comprobamos que se redirige al usuario a la vista: “listado de todos los usuarios de la aplicación”
        String checkText = "Listado de usuarios";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba6] Inicio de sesión con datos válidos (usuario estándar).
    @Test
    @Order(6)
    public void PR06() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user01@email.com", "user01");
        //Comprobamos que entramos en la sección privada y nos nuestra el texto a buscar
        String checkText = "Mis ofertas";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba7] Inicio de sesión con datos inválidos (usuario estándar, email existente, pero contraseña incorrecta).
    @Test
    @Order(7)
    public void PR07() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01 contraseña incorrecta.
        PO_LoginView.fillLoginForm(driver, "user01@email.com", "incorrecta");
        //Comprobamos que seguimos en identificación de usuario y con el error correspondiente
        String checkText = "Identificación de usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        String checkText2 = "Email o password incorrecto";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "text", checkText2);
        Assertions.assertEquals(checkText2, result2.get(0).getText());
    }

    //  [Prueba8] Inicio de sesión con datos inválidos (campo email o contraseña vacíos).
    @Test
    @Order(8)
    public void PR08() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con email vacío.
        PO_LoginView.fillLoginForm(driver, "", "123456");
        //Comprobamos que seguimos en identificación de usuario
        String checkText = "Identificación de usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // [Prueba9] Hacer click en la opción de salir de sesión y comprobar que se redirige a la página de inicio
    //  de sesión (Login).
    @Test
    @Order(9)
    public void PR09() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user01@email.com", "user01");
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        //Comprobamos que volvemos a la identificación de usuario
        String checkText = "Identificación de usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        String checkText2 = "El usuario se ha desconectado correctamente";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "text", checkText2);
        Assertions.assertEquals(checkText2, result2.get(0).getText());
    }

    //  [Prueba10] Comprobar que el botón cerrar sesión no está visible si el usuario no está autenticado.
    @Test
    @Order(10)
    public void PR10() {
        // Cuando cargue la página al principio, como el usuario no está autenticado, no debe aparecer el boton de desconectar
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/nav/div/div[2]/ul[2]/li[3]/a"));
        Assertions.assertTrue(elements.isEmpty());
    }

    //  [Prueba11] Mostrar el listado de usuarios. Comprobar que se muestran todos los que existen en el
    //  sistema, contabilizando al menos el número de usuarios.
    @Test
    @Order(11)
    public void PR11() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // total de usuarios del sistema: datos de prueba(20) + Prueba1(1)
        List<String> users = getUsers();
        // checkear que existen todos los usuarios
        List<WebElement> elements = new ArrayList<>();
        int i = 0;
        int page = 1;
        for (String user: users) {
            elements.addAll(PO_View.checkElementBy(driver, "text", user));
            i++;
            if (i == 4) {
                i = 0;
                page++;
                PO_NavView.clickOption(driver, "/admin/users/?page=" + page, "@href", "/admin/users/?page=" + page);
            }
        }
        Assertions.assertEquals(users.size(), elements.size());
    }

    //  [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
    //  y dicho usuario desaparece.
    @Test
    @Order(12)
    public void PR12() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Obtener el email del usuario a borrar
        String checkPath = "/html/body/div[@class='container']/div[@class='table-responsive']/form/table/tbody/tr[1]/td[1]";
        List<WebElement> userEmail = PO_View.checkElementBy(driver, "free", checkPath);
        String email = userEmail.get(0).getText();
        //Marcar el checkBox para seleccionarlo
        String checkPath2 = "//table[@class='table table-hover']/tbody/tr[1]/td[4]/input[@type='checkbox']";
        List<WebElement> cb = PO_View.checkElementBy(driver, "free", checkPath2);
        cb.get(0).click();
        //Pulsar el boton eliminar
        String checkPath3 = "deleteButton";
        List<WebElement> btEliminar = PO_View.checkElementBy(driver, "id", checkPath3);
        btEliminar.get(0).click();
        // comprobar que el email del usuario borrado no aparece en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, email, PO_View.getTimeout());
    }

    //  [Prueba13] Ir a la lista de usuarios, borrar el último usuario de la lista, comprobar que la lista se actualiza
    //  y dicho usuario desaparece.
    @Test
    @Order(13)
    public void PR13() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Ir a la ultima pagina
        PO_NavView.clickOption(driver, "/admin/users/?page=3", "@href", "/admin/users/?page=3");
        PO_NavView.clickOption(driver, "/admin/users/?page=5", "@href", "/admin/users/?page=5");
        //Obtener el email del usuario a borrar
        String checkPath = "/html/body/div[@class='container']/div[@class='table-responsive']/form/table/tbody/tr[4]/td[1]";
        List<WebElement> userEmail = PO_View.checkElementBy(driver, "free", checkPath);
        String email = userEmail.get(0).getText();
        //Marcar el checkBox para seleccionarlo
        String checkPath2 = "//table[@class='table table-hover']/tbody/tr[4]/td[4]/input[@type='checkbox']";
        List<WebElement> cb = PO_View.checkElementBy(driver, "free", checkPath2);
        cb.get(0).click();
        //Pulsar el boton eliminar
        String checkPath3 = "deleteButton";
        List<WebElement> btEliminar = PO_View.checkElementBy(driver, "id", checkPath3);
        btEliminar.get(0).click();
        //Ir a la ultima pagina
        PO_NavView.clickOption(driver, "/admin/users/?page=3", "@href", "/admin/users/?page=3");
        PO_NavView.clickOption(driver, "/admin/users/?page=5", "@href", "/admin/users/?page=5");
        // comprobar que el email del usuario borrado no aparece en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, email, PO_View.getTimeout());
    }

    //  [Prueba14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la lista se actualiza y dichos
    //  usuarios desaparecen.
    @Test
    @Order(14)
    public void PR14() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Obtener los emails de los usuarios a borrar
        List<String> users = new ArrayList<>();
        for (int i = 1; i < 4; i++){
            String checkPath = "/html/body/div[@class='container']/div[@class='table-responsive']/form/table/tbody/tr[" + i + "]/td[1]";
            List<WebElement> userEmail = PO_View.checkElementBy(driver, "free", checkPath);
            users.add(userEmail.get(0).getText());
        }
        //Marcar los checkBoxs para seleccionarlos
        for (int i = 1; i < 4; i++){
            String checkPath2 = "//table[@class='table table-hover']/tbody/tr[" + i + "]/td[4]/input[@type='checkbox']";
            List<WebElement> cb = PO_View.checkElementBy(driver, "free", checkPath2);
            cb.get(0).click();
        }
        //Pulsar el boton eliminar
        String checkPath3 = "deleteButton";
        List<WebElement> btEliminar = PO_View.checkElementBy(driver, "id", checkPath3);
        btEliminar.get(0).click();
        // comprobar que los emails de los usuarios borrados no aparece en la vista
        for (String user: users){
            SeleniumUtils.waitTextIsNotPresentOnPage(driver, user, PO_View.getTimeout());
        }
    }

    //  [Prueba15] Intentar borrar el usuario que se encuentra en sesión y comprobar que no ha sido borrado
    //  (porque no es un usuario administrador o bien, porque, no se puede borrar a sí mismo, si está autenticado)
    @Test
    @Order(15)
    public void PR15() {

    }

    //  [Prueba16] Ir al formulario de alta de oferta, rellenarla con datos válidos y pulsar el botón Submit.
    //  Comprobar que la oferta sale en el listado de ofertas de dicho usuario.
    @Test
    @Order(16)
    public void PR16() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Articulo ejemplo", "Descripción ....", 4, false);
        //Comprobamos que esta en el listado de ofertas del usuario
        String checkText = "Articulo ejemplo";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba17] Ir al formulario de alta de oferta, rellenarla con datos inválidos (campo título vacío y precio
    //  en negativo) y pulsar el botón Submit. Comprobar que se muestra el mensaje de campo inválido.
    @Test
    @Order(17)
    public void PR17() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "a", "a", -4, false);
        //Comprobamos que salen los errores
        String checkText = "El título proporcionado es demasiado corto";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        String[] check = result.get(0).getText().split("\n");
        Assertions.assertEquals(checkText, check[0]);

        String checkText2 = "La descripción proporcionada es demasiado corta";
        Assertions.assertEquals(checkText2, check[1]);

        String checkText3 = "El precio proporcionado no es válido, debe ser positivo";
        Assertions.assertEquals(checkText3, check[2]);
    }

    //  [Prueba18] Mostrar el listado de ofertas para dicho usuario y comprobar que se muestran todas las que
    //  existen para este usuario.
    @Test
    @Order(18)
    public void PR18() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "user05");
        //Miramos que estén todas sus ofertas total = 10
        int total = 10;
        int count = 0;
        String checkText = "Descripción ejemplo";
        //Página 1
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        count += result.size();
        //Página 2
        PO_NavView.clickOption(driver, "/user/offers/?page=2", "@href", "/user/offers/?page=2");
        result = PO_View.checkElementBy(driver, "text", checkText);
        count += result.size();
        //Página 3
        PO_NavView.clickOption(driver, "/user/offers/?page=3", "@href", "/user/offers/?page=3");
        result = PO_View.checkElementBy(driver, "text", checkText);
        count += result.size();

        Assertions.assertEquals(total, count);
    }

    //  [Prueba19] Ir a la lista de ofertas, borrar la primera oferta de la lista, comprobar que la lista se actualiza
    //  y que la oferta desaparece.
    @Test
    @Order(19)
    public void PR19() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Obtener el título de la oferta a borrar
        String checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[1]";
        List<WebElement> offerTitle = PO_View.checkElementBy(driver, "free", checkPath);
        String offer = offerTitle.get(0).getText();
        //Borramos la primera oferta
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a"));
        elements.get(0).click();
        //Comprobamos que el título de la oferta a borrar no aparece en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, offer, PO_View.getTimeout());
    }

    //  [Prueba20] Ir a la lista de ofertas, borrar la última oferta de la lista, comprobar que la lista se actualiza
    //  y que la oferta desaparece.
    @Test
    @Order(20)
    public void PR20() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Ir a la ultima pagina
        PO_NavView.clickOption(driver, "/user/offers/?page=3", "@href", "/user/offers/?page=3");
        //Obtener el título de la oferta a borrar
        String checkPath = "/html/body/div/div[1]/table/tbody/tr[2]/td[1]";
        List<WebElement> offerTitle = PO_View.checkElementBy(driver, "free", checkPath);
        String offer = offerTitle.get(0).getText();
        //Borramos la última oferta
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div/div[1]/table/tbody/tr[2]/td[5]/a"));
        elements.get(0).click();
        //Ir a la ultima pagina
        PO_NavView.clickOption(driver, "/user/offers/?page=3", "@href", "/user/offers/?page=3");
        // comprobar que el email del usuario borrado no aparece en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, offer, PO_View.getTimeout());
    }

    //  [Prueba21] Ir a la lista de ofertas, borrar una oferta de otro usuario, comprobar que la oferta no se borra.
    @Test
    @Order(21)
    public void PR21() {
    }

    //  [Prueba22] Ir a la lista de ofertas, borrar una oferta propia que ha sido vendida, comprobar que la
    //  oferta no se borra.
    @Test
    @Order(22)
    public void PR22() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Compramos una oferta
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[2]/td[5]/a"));
        elements.get(0).click();
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "user05");
        //Pulsamos en borrar esa oferta
        elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a"));
        elements.get(0).click();
        //Aparece un texto informativo
        String checkText = "No puedes eliminar esta oferta";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //  [Prueba23] Hacer una búsqueda con el campo vacío y comprobar que se muestra la página que
    //  corresponde con el listado de las ofertas existentes en el sistema
    @Test
    @Order(23)
    public void PR23() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Comprobamos cuantas ofertas hay
        String checkText = "Descripción ejemplo";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        int total = result.size();
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        //Comprobamos que siguen apareciendo 5 ofertas
        checkText = "Descripción ejemplo";
        result = PO_View.checkElementBy(driver, "text", checkText);
        int totalSearch = result.size();
        Assertions.assertEquals(total, totalSearch);
    }

    //  [Prueba24] Hacer una búsqueda escribiendo en el campo un texto que no exista y comprobar que se
    //  muestra la página que corresponde, con la lista de ofertas vacía.
    @Test
    @Order(24)
    public void PR24() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que no coincida con ninguna
        String textSearch = "Esto no coincide de ninguna manera 33";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        //Comprobamos no aparecen ofertas
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, textSearch, PO_View.getTimeout());
    }

    //  [Prueba25] Hacer una búsqueda escribiendo en el campo un texto en minúscula o mayúscula y comprobar que se
    //  muestra la página que corresponde, con la lista de ofertas que contengan dicho
    //  texto, independientemente que el título esté almacenado en minúsculas o mayúscula.
    @Test
    @Order(25)
    public void PR25() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user04@email.com", "user04");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que coincida con mayusculas y minusculas
        String textSearch = "Ejemplo 60";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        //Comprobamos cuantas ofertas hay
        String checkText = "Descripción ejemplo";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        int total = result.size();
        //Ahora buscamos un titulo de oferta que coincida sin mayus ni minus
        textSearch = "eJeMpLO 60";
        search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        //Comprobamos que siguen apareciendo las ofertas
        checkText = "Descripción ejemplo";
        result = PO_View.checkElementBy(driver, "text", checkText);
        int totalSearch = result.size();
        Assertions.assertEquals(total, totalSearch);
    }


    /* Ejemplos de pruebas de llamada a una API-REST */
    /* ---- Probamos a obtener lista de canciones sin token ---- */
//    @Test
//    @Order(33)
//    public void PR33() {
//        final String RestAssuredURL = "http://localhost:8081/api/v1.0/songs";
//        Response response = RestAssured.get(RestAssuredURL);
//        Assertions.assertEquals(403, response.getStatusCode());
//    }
//
//    @Test
//    @Order(38)
//    public void PR38() {
//        final String RestAssuredURL = "http://localhost:8081/api/v1.0/users/login";
//        //2. Preparamos el parámetro en formato JSON
//        RequestSpecification request = RestAssured.given();
//        JSONObject requestParams = new JSONObject();
//        requestParams.put("email", "prueba1@prueba1.com");
//        requestParams.put("password", "prueba1");
//        request.header("Content-Type", "application/json");
//        request.body(requestParams.toJSONString());
//        //3. Hacemos la petición
//        Response response = request.post(RestAssuredURL);
//        //4. Comprobamos que el servicio ha tenido exito
//        Assertions.assertEquals(200, response.getStatusCode());
//    }

    // Métodos auxiliares
    private List<String> getUsers() {
        List<String> users = new ArrayList<>();
        users.add("email@email.com");
        String number = "";
        for (int i = 1; i <= 20; i++) {
            if (i < 10) {
                number = "0" + i;
            } else {
                number = i + "";
            }
            users.add("user" + number + "@email.com");
        }
        return users;
    }
}
