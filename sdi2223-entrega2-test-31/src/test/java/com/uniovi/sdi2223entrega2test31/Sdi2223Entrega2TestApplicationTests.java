package com.uniovi.sdi2223entrega2test31;

import com.uniovi.sdi2223entrega2test31.pageobjects.*;
import com.uniovi.sdi2223entrega2test31.util.SeleniumUtils;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
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
    static MongoDB m;
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
        m = new MongoDB();
        m.resetMongo();
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
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "user33@email.com", "Pepe", "Perez Gonzalez",
                "2002-05-05", "123456", "123456");
        //Comprobamos que entramos en la sección privada y nos nuestra el texto a buscar
        String checkText = "Mis ofertas";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        //Miramos cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before + 1);
    }

    //  [Prueba2] Registro de Usuario con datos inválidos (email, nombre, apellidos y fecha de nacimiento vacíos).
    @Test
    @Order(2)
    public void PR02() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Vamos al formulario de registro
        PO_NavView.clickOption(driver, "signup", "@href", "/users/signup");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "", "", "","", "123456", "123456");
        //Comprobamos que sigue en la página de registro
        String checkText = "Registrar usuario";
        String checkPath = "//h2[contains(text(),'" + checkText + "')]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        Assertions.assertEquals(checkText, result.get(0).getText());
        //Miramos cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba3] Registro de Usuario con datos inválidos (repetición de contraseña inválida).
    @Test
    @Order(3)
    public void PR03() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
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
        //Miramos cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba4] Registro de Usuario con datos inválidos (email existente).
    @Test
    @Order(4)
    public void PR04() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
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
        //Miramos cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before);
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
        PO_LoginView.fillLoginForm(driver, "user01@email.com", "admin");
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
        PO_LoginView.fillLoginForm(driver, "user01@email.com", "admin");
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
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // total de usuarios del sistema: datos de prueba(20) + Prueba1(1)
        List<String> users = m.usersEmail();
        // checkear que existen todos los usuarios
        List<WebElement> elements = new ArrayList<>();
        int i = 0;
        int page = 1;
        for (String user: users) {
            if (!user.equals("admin@email.com")) {
                elements.addAll(PO_View.checkElementBy(driver, "text", user));
                i++;
                if (i == 4) {
                    i = 0;
                    page++;
                    PO_NavView.clickOption(driver, "/admin/users/?page=" + page, "@href", "/admin/users/?page=" + page);
                }
            }
        }
        Assertions.assertEquals(users.size()-1, elements.size());
        //Compara cuantos usuarios hay ahora en la BBDD
        Assertions.assertEquals(before, users.size());
        Assertions.assertEquals(before-1, elements.size());
    }

    //  [Prueba12] Ir a la lista de usuarios, borrar el primer usuario de la lista, comprobar que la lista se actualiza
    //  y dicho usuario desaparece.
    @Test
    @Order(12)
    public void PR12() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Miramos cuantas ofertas había en la BBDD
        long before2 = m.offersSize();
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
        //Mirar cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before - 1);
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after2 = m.offersSize();
        Assertions.assertNotEquals(after2, before2);
    }

    //  [Prueba13] Ir a la lista de usuarios, borrar el último usuario de la lista, comprobar que la lista se actualiza
    //  y dicho usuario desaparece.
    @Test
    @Order(13)
    public void PR13() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
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
        //Mirar cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before - 1);
    }

    //  [Prueba14] Ir a la lista de usuarios, borrar 3 usuarios, comprobar que la lista se actualiza y dichos
    //  usuarios desaparecen.
    @Test
    @Order(14)
    public void PR14() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Miramos cuantas ofertas había en la BBDD
        long before2 = m.offersSize();
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
        //Mirar cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before - 3);
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after2 = m.offersSize();
        Assertions.assertNotEquals(after2, before2);
    }

    //  [Prueba15] Intentar borrar el usuario que se encuentra en sesión y comprobar que no ha sido borrado
    //  (porque no es un usuario administrador o bien, porque, no se puede borrar a sí mismo, si está autenticado)
    @Test
    @Order(15)
    public void PR15() {
        //Miramos cuantos usuarios había en la BBDD
        long before = m.usersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con admin.
        String email = "admin@email.com";
        PO_LoginView.fillLoginForm(driver, email, "admin");
        //Mirar cuantos usuarios hay ahora en la BBDD
        long after = m.usersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba16] Ir al formulario de alta de oferta, rellenarla con datos válidos y pulsar el botón Submit.
    //  Comprobar que la oferta sale en el listado de ofertas de dicho usuario.
    @Test
    @Order(16)
    public void PR16() {
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Articulo ejemplo", "Descripción ....", 4, false);
        //Comprobamos que esta en el listado de ofertas del usuario
        String checkText = "Articulo ejemplo";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before + 1);
    }

    //  [Prueba17] Ir al formulario de alta de oferta, rellenarla con datos inválidos (campo título vacío y precio
    //  en negativo) y pulsar el botón Submit. Comprobar que se muestra el mensaje de campo inválido.
    @Test
    @Order(17)
    public void PR17() {
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
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

        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba18] Mostrar el listado de ofertas para dicho usuario y comprobar que se muestran todas las que
    //  existen para este usuario.
    @Test
    @Order(18)
    public void PR18() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user06.
        PO_LoginView.fillLoginForm(driver, "user06@email.com", "admin");
        //Miramos que estén todas sus ofertas total = 10
        int total = m.getOffersByUser("user06@email.com").size();
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
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
        //Obtener el título de la oferta a borrar
        String checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[1]";
        List<WebElement> offerTitle = PO_View.checkElementBy(driver, "free", checkPath);
        String offer = offerTitle.get(0).getText();
        //Borramos la primera oferta
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a"));
        elements.get(0).click();
        //Comprobamos que el título de la oferta a borrar no aparece en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, offer, PO_View.getTimeout());
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before - 1);
    }

    //  [Prueba20] Ir a la lista de ofertas, borrar la última oferta de la lista, comprobar que la lista se actualiza
    //  y que la oferta desaparece.
    @Test
    @Order(20)
    public void PR20() {
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
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
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before - 1);
    }

    //  [Prueba21] Ir a la lista de ofertas, borrar una oferta de otro usuario, comprobar que la oferta no se borra.
    @Test
    @Order(21)
    public void PR21() {
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        // comprobar que el no hay ningún delete en la vista
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, "delete", PO_View.getTimeout());
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba22] Ir a la lista de ofertas, borrar una oferta propia que ha sido vendida, comprobar que la
    //  oferta no se borra.
    @Test
    @Order(22)
    public void PR22() {
        //Miramos cuantas ofertas había en la BBDD
        long before = m.offersSize();
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user06.
        PO_LoginView.fillLoginForm(driver, "user06@email.com", "admin");
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Compramos una oferta
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a"));
        elements.get(0).click();
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        //Rellenamos el formulario con user05.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
        //Pulsamos en borrar esa oferta
        elements = driver.findElements(By.xpath("/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a"));
        elements.get(0).click();
        //Aparece un texto informativo
        String checkText = "No puedes eliminar esta oferta";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
        //Mirar cuantas ofertas hay ahora en la BBDD
        long after = m.offersSize();
        Assertions.assertEquals(after, before);
    }

    //  [Prueba23] Hacer una búsqueda con el campo vacío y comprobar que se muestra la página que
    //  corresponde con el listado de las ofertas existentes en el sistema
    @Test
    @Order(23)
    public void PR23() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user01.
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
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
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
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
        PO_LoginView.fillLoginForm(driver, "user05@email.com", "admin");
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

    // [Prueba26] Sobre una búsqueda determinada (a elección de desarrollador), comprar una oferta que
    //deja un saldo positivo en el contador del comprobador. Y comprobar que el contador se actualiza
    //correctamente en la vista del comprador.
    @Test
    @Order(26)
    public void PR26() {
        // CREAR OFERTA NUEVA (por si acaso esta borrada)
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "admin");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Oferta Buscar", "Descripción ....", 90, false);

        // LOGOUT Y COMPRAR OFERTA
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        // LOGIN CON OTRO USUARIO
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user09 para que pueda comprar la oferta
        PO_LoginView.fillLoginForm(driver, "user09@email.com", "admin");

        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que coincida con mayusculas y minusculas
        String textSearch = "Oferta Buscar";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        // Hacemos click en el enlace para comprar la oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a";
        List<WebElement> buyLink = PO_View.checkElementBy(driver, "free", checkPath);
        buyLink.get(0).click();
        // Comprobamos el monedero tiene ahora 10 € (100 iniciales - 90 de la oferta)
        String wallet = "Monedero: 10 €";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", wallet);
        Assertions.assertEquals(wallet, result.get(0).getText());
    }

    // [Prueba27] Sobre una búsqueda determinada (a elección de desarrollador), comprar una oferta que
    //deja un saldo 0 en el contador del comprobador. Y comprobar que el contador se actualiza
    //correctamente en la vista del comprador.
    @Test
    @Order(27)
    public void PR27() {
        // CREAR OFERTA DE 100 EUROS
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user08
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "admin");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Oferta cien", "Descripción ....", 100, false);
        // LOGOUT Y LOGIN
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        // LOGIN CON OTRO USUARIO
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user10@email.com", "admin");
        // COMPRAR OFERTA CREADA
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que coincida con mayusculas y minusculas
        String textSearch = "Oferta cien";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        // Hacemos click en el enlace para comprar la oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a";
        List<WebElement> buyLink = PO_View.checkElementBy(driver, "free", checkPath);
        buyLink.get(0).click();
        // Comprobamos que hay Monedero: 0 €
        String checkText = "Monedero: 0 €";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // [Prueba28] Sobre una búsqueda determinada (a elección de desarrollador), intentar comprar una oferta
    //que esté por encima de saldo disponible del comprador. Y comprobar que se muestra el mensaje
    //de saldo no suficiente.
    @Test
    @Order(28)
    public void PR28() {
        // CREAR OFERTA DE MAÁS DE 100 EUROS
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "admin");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Oferta muy cara", "Descripción ....", 110, false);
        // LOGOUT Y LOGIN
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        // LOGIN CON OTRO USUARIO
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user11@email.com", "admin");
        // COMPRAR OFERTA CREADA
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que coincida con mayusculas y minusculas
        String textSearch = "Oferta muy cara";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        // Hacemos click en el enlace para comprar la oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a";
        List<WebElement> buyLink = PO_View.checkElementBy(driver, "free", checkPath);
        buyLink.get(0).click();
        // Comprobamos que se muestra el mensaje de error
        String checkText = "Saldo insuficiente en la cartera";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // [Prueba29] Ir a la opción de ofertas compradas del usuario y mostrar la lista. Comprobar que aparecen
    // las ofertas que deben aparecer.
    @Test
    @Order(29)
    public void PR29() {
        // Utilizando el user11 que tiene UNA oferta comprada
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user15@email.com", "admin");
        // Vamos al menu de ofertas compradas
        PO_NavView.clickOption(driver, "/offers/purchases", "@href", "/offers/purchases");
        // Comprobamos que solo haya una fila en la tabla de ofertas compradas
        String checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRows = driver.findElements(By.xpath(checkPath + "/tr"));
        int purchases = tableBodyRows.size();
        // Vamos al listado de ofertas
        checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        // Compramos oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[2]/td[5]/a";
        List<WebElement> buyLink = PO_View.checkElementBy(driver, "free", checkPath);
        buyLink.get(0).click();
        // Vamos a la vista de ofertas compradas
        PO_NavView.clickOption(driver, "/offers/purchases", "@href", "/offers/purchases");
        //Verifica que el numero aumento en 1
        checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRowsAfter = driver.findElements(By.xpath(checkPath + "/tr"));
        Assertions.assertEquals(purchases+1, tableBodyRowsAfter.size());
    }

    // [Prueba30] Al crear una oferta, marcar dicha oferta como destacada y a continuación comprobar: i)
    //que aparece en el listado de ofertas destacadas para los usuarios y que el saldo del usuario se
    //actualiza adecuadamente en la vista del ofertante (comprobar saldo antes y después, que deberá
    //diferir en 20€).
    @Test
    @Order(30)
    public void PR30() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user12
        PO_LoginView.fillLoginForm(driver, "user12@email.com", "admin");
        // Sacar monedero
        String checkPath = "/html/body/nav/div/div[2]/ul[2]/li[2]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        int money = Integer.valueOf(result.get(0).getText().split(" ")[1]);
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Oferta destacada", "Descripción ....", 110, true);
        // mirar monedero
        checkPath = "/html/body/nav/div/div[2]/ul[2]/li[2]";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "free", checkPath);
        int afterMoney = Integer.valueOf(result2.get(0).getText().split(" ")[1]);
        // Ya estamos en la lista de mis ofertas
        // Comprobamos que la oferta está destacada
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[1]"; // comprobar titulo
        List<WebElement> result3 = PO_View.checkElementBy(driver, "free", checkPath);
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[6]"; // comprobar que aparece como destacada
        List<WebElement> result4 = PO_View.checkElementBy(driver, "free", checkPath);
        // Asertos
        // Diferencia de 20 entre el dinero que tenia antes y despues de destacar la oferta
        Assertions.assertTrue(money - afterMoney == 20);
        // El título de la primera oferta es la destacada
        Assertions.assertEquals("Oferta destacada", result3.get(0).getText());
        // El campo de la oferta destacada correspondiente a destacada es destacada
        Assertions.assertEquals("Destacada", result4.get(0).getText());
    }

    // [Prueba31] Sobre el listado de ofertas de un usuario con más de 20 euros de saldo, pinchar en el enlace
    //Destacada y a continuación comprobar: i) que aparece en el listado de ofertas destacadas para los
    //usuarios y que el saldo del usuario se actualiza adecuadamente en la vista del ofertante (comprobar
    //saldo antes y después, que deberá diferir en 20€ ).
    @Test
    @Order(31)
    public void PR31() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user13
        PO_LoginView.fillLoginForm(driver, "user13@email.com", "admin");
        // Sacar monedero
        String checkPath = "/html/body/nav/div/div[2]/ul[2]/li[2]";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        int money = Integer.valueOf(result.get(0).getText().split(" ")[1]);
        // Ya estamos en la lista de mis ofertas
        // Destacar la tercera oferta, por ejemplo
        // Sacar el titulo de la tercera oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[3]/td[1]";
        List<WebElement> titulo = PO_View.checkElementBy(driver, "free", checkPath);
        var tituloOferta = titulo.get(0).getText();
        // Clicar en la opcion de destacar
        checkPath = "/html/body/div/div[2]/table/tbody/tr[3]/td[6]/a";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "free", checkPath);
        result2.get(0).click();
        // mirar monedero
        checkPath = "/html/body/nav/div/div[2]/ul[2]/li[2]";
        List<WebElement> result3 = PO_View.checkElementBy(driver, "free", checkPath);
        int afterMoney = Integer.valueOf(result3.get(0).getText().split(" ")[1]);
        // Vamos a la lista de ofertas
        checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        // Comprobar titulo: como actualmente solo hay dos ofertas destacadas, o es la primera o es la segunda (depende del titulo)
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[1]";
        List<WebElement> opcion1 = PO_View.checkElementBy(driver, "free", checkPath);
        checkPath = "/html/body/div/div[2]/table/tbody/tr[2]/td[1]";
        List<WebElement> opcion2 = PO_View.checkElementBy(driver, "free", checkPath);
        // Asertos
        // Diferencia de 20 entre el dinero que tenia antes y despues de destacar la oferta
        Assertions.assertTrue(money - afterMoney == 20);
        // El título de la primera oferta es la destacada
        Assertions.assertTrue(tituloOferta.equals(opcion1.get(0).getText()) || tituloOferta.equals(opcion2.get(0).getText()));
    }

    // [Prueba32] Sobre el listado de ofertas de un usuario con menos de 20 euros de saldo, pinchar en el
    //enlace Destacada y a continuación comprobar que se muestra el mensaje de saldo no suficiente.
    @Test
    @Order(32)
    public void PR32() {
        // CREAR OFERTA DE 100 EUROS
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user08
        PO_LoginView.fillLoginForm(driver, "user08@email.com", "admin");
        //Vamos al apartado de añadir oferta
        PO_NavView.clickOption(driver, "/offer/add", "@href", "/offer/add");
        //Rellenamos la oferta
        PO_PrivateView.fillFormAddOffer(driver, "Oferta 32", "Descripción ....", 100, false);
        // LOGOUT Y LOGIN
        //Hacemos click en la opción de logout
        PO_NavView.clickOption(driver, "logout", "@href", "/users/login");
        // LOGIN CON OTRO USUARIO
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user04.
        PO_LoginView.fillLoginForm(driver, "user14@email.com", "admin");
        // COMPRAR OFERTA CREADA
        //Vamos a todas las ofertas
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        List<WebElement> allOffers = PO_View.checkElementBy(driver, "free", checkPath);
        allOffers.get(0).click();
        //Buscamos un titulo de oferta que coincida con mayusculas y minusculas
        String textSearch = "Oferta 32";
        WebElement search = driver.findElement(By.name("search"));
        search.click();
        search.clear();
        search.sendKeys(textSearch);
        //Hacemos clic en la búsqueda
        checkPath = "/html/body/div/div[1]/div/form/div/span/button";
        List<WebElement> searchButton = PO_View.checkElementBy(driver, "free", checkPath);
        searchButton.get(0).click();
        // Hacemos click en el enlace para comprar la oferta
        checkPath = "/html/body/div/div[2]/table/tbody/tr[1]/td[5]/a";
        List<WebElement> buyLink = PO_View.checkElementBy(driver, "free", checkPath);
        buyLink.get(0).click();

        // YA TIENE MENOS DE 20€
        // Vamos a mis ofertas
        checkPath = "/html/body/nav/div/div[2]/ul[1]/li[1]/a";
        List<WebElement> result = PO_View.checkElementBy(driver, "free", checkPath);
        result.get(0).click();
        // Clicamos en destacar la primera
        checkPath = "/html/body/div/div[1]/table/tbody/tr[1]/td[6]/a";
        List<WebElement> result2 = PO_View.checkElementBy(driver, "free", checkPath);
        result2.get(0).click();
        // Comprobamos que se muestra el mensaje de error
        // Comprobamos que se muestra el mensaje de error
        String checkText = "Saldo insuficiente para destacar la oferta";
        List<WebElement> result3 = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result3.get(0).getText());
    }

    // [Prueba33] Intentar acceder sin estar autenticado a la opción de listado de usuarios. Se deberá volver
    //al formulario de login.
    @Test
    @Order(33)
    public void PR33() {
        String listaUsuarios = "http://localhost:8080/admin/users";
        driver.navigate().to(listaUsuarios);
        String checkText = "Identificación de usuario";
        String checkNotPresent = "Listado de usuarios";
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, checkNotPresent, PO_View.getTimeout());
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //[Prueba34] Intentar acceder sin estar autenticado a la opción de listado de conversaciones
    //[REQUISITO OBLIGATORIO S5]. Se deberá volver al formulario de login.
    @Test
    @Order(34)
    public void PR34() {
//        final String RestAssuredURL = "http://localhost:8080/api/v1.0/convers/all";
//        Response response = RestAssured.get(RestAssuredURL);
//        Assertions.assertEquals(403, response.getStatusCode());

    }

    //[Prueba35] Estando autenticado como usuario estándar intentar acceder a una opción disponible solo
    //para usuarios administradores (Añadir menú de auditoria (visualizar logs)). Se deberá indicar un
    //mensaje de acción prohibida.
    @Test
    @Order(35)
    public void PR35() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user08
        PO_LoginView.fillLoginForm(driver, "user15@email.com", "admin");
        // Vamos al menú de logs (solo para administrador)
        String listaLogs = "http://localhost:8080/admin/logs";
        driver.navigate().to(listaLogs);
        String checkText = "Acción prohibida para el usuario";
        String checkNotPresent = "Listado de logs";
        SeleniumUtils.waitTextIsNotPresentOnPage(driver, checkNotPresent, PO_View.getTimeout());
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //[Prueba36] Estando autenticado como usuario administrador visualizar todos los logs generados en
    //una serie de interacciones. Esta prueba deberá generar al menos dos interacciones de cada tipo y
    //comprobar que el listado incluye los logs correspondientes.
    @Test
    @Order(36)
    public void PR36() {
        // Primera interaccion: login
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user08
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // Segunda interacción: redireccion del login a listado de usuarios
        // Tercera interacción: cambio de vista a listado de logs
        String checkPath = "/html/body/nav/div/div[2]/ul[1]/li[2]/a";
        PO_NavView.clickOption(driver, "/admin/logs", "@href", "/admin/logs");
        // Coger cantidad de logs
        checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRows = driver.findElements(By.xpath(checkPath + "/tr"));
        int sizeLogs = tableBodyRows.size();
        // Ir a listado de usuarios: un log más
        String usersPath = "http://localhost:8080/admin/users";
        driver.navigate().to(usersPath);
        // Volver a listado de logs: un log más
        PO_NavView.clickOption(driver, "/admin/logs", "@href", "/admin/logs");
        // Verificar que la cantidad de logs se incrementó en 2
        checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRowsAfter = driver.findElements(By.xpath(checkPath + "/tr"));
        int sizeLogsAfter = tableBodyRowsAfter.size();
        Assertions.assertEquals(sizeLogsAfter - sizeLogs, 2);
    }


    //[Prueba37] Estando autenticado como usuario administrador, ir a visualización de logs, pulsar el
    //botón/enlace borrar logs y comprobar que se eliminan los logs de la base de datos.
    @Test
    @Order(37)
    public void PR37() {
        //Vamos al formulario de inicio de sesión
        PO_NavView.clickOption(driver, "login", "@href", "/users/login");
        //Rellenamos el formulario con user08
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // Vamos al menú de logs (solo para administrador)
        PO_NavView.clickOption(driver, "/admin/logs", "@href", "/admin/logs");
        // Miramos el tamaño de la lista de logs
        String checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRows = driver.findElements(By.xpath(checkPath + "/tr"));
        int sizeLogs = tableBodyRows.size();
        // Borramos los logs
        checkPath = "/html/body/div/form[2]/button";
        List<WebElement> deleteButton = PO_View.checkElementBy(driver, "free", checkPath);
        deleteButton.get(0).click();
        // Comprobamos que la lista ahora solo contiene el log de la redireccion
        checkPath = "/html/body/div/div/table/tbody";
        List<WebElement> tableBodyRowsAfter = driver.findElements(By.xpath(checkPath + "/tr"));
        int sizeLogsAfter = tableBodyRowsAfter.size();
        Assertions.assertTrue(sizeLogsAfter != sizeLogs);
        Assertions.assertEquals(1, sizeLogsAfter);
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

    //[Prueba38] Inicio de sesión con datos válidos.
    @Test
    @Order(38)
    public void PR38() {
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Usuario autenticado correctamente", jsonPath.get("message"));
        Assertions.assertTrue(jsonPath.getBoolean("authenticated"));
        Assertions.assertNotNull(jsonPath.get("token"));
    }

    //[Prueba39] Inicio de sesión con datos inválidos (email existente, pero contraseña incorrecta).
    @Test
    @Order(39)
    public void PR39() {
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "noadmin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Inicio de sesión incorrecto", jsonPath.get("message"));
        Assertions.assertFalse(jsonPath.getBoolean("authenticated"));
    }

    //[Prueba40] Inicio de sesión con datos inválidos (campo email o contraseña vacíos).
    @Test
    @Order(40)
    public void PR40() {
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals("Se ha producido un error al verificar las credenciales", jsonPath.get("message"));
        Assertions.assertFalse(jsonPath.getBoolean("authenticated"));
        Assertions.assertEquals(2, jsonPath.getList("errors").size());
    }

    //[Prueba41] Mostrar el listado de ofertas para dicho usuario y comprobar que se muestran todas las que
    //existen para este usuario. Esta prueba implica invocar a dos servicios: S1 y S2.
    @Test
    @Order(41)
    public void PR41() {
        // Servicio S1
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Servicio S2
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/offers/availablefromothers";
        RequestSpecification request2 = RestAssured.given();
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));

        Response response2 = request2.get(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        Assertions.assertEquals(200, response2.getStatusCode());
        Assertions.assertEquals(m.othersOffersSize("user16@email.com"), jsonPath2.getList("offers").size());
    }

    //[Prueba42] Enviar un mensaje a una oferta. Esta prueba consistirá en comprobar que el servicio
    //almacena correctamente el mensaje para dicha oferta. Por lo tanto, el usuario tendrá que
    //identificarse (S1), enviar un mensaje para una oferta de id conocido (S3) y comprobar que el
    //mensaje ha quedado bien registrado (S4).
    @Test
    @Order(42)
    public void PR42() {
        // Servicio S1
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Servicio S3
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/messages/send";
        RequestSpecification request2 = RestAssured.given();
        JSONObject requestParams2 = new JSONObject();
        requestParams2.put("offer", m.getOneOfferIdByAuthor("user17@email.com")); // oferta de otro
        requestParams2.put("text", "hola");
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));
        request2.body(requestParams2.toJSONString());

        Response response2 = request2.post(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        // Servicio S4
        final String RestAssuredURL3 = "http://localhost:8080/api/v1.0/messages/fromconver/" + jsonPath2.get("_idConv");
        RequestSpecification request3 = RestAssured.given();
        request3.header("Content-Type", "application/json");
        request3.header("token", jsonPath.get("token"));

        Response response3 = request3.get(RestAssuredURL3);

        JsonPath jsonPath3 = response3.jsonPath();

        Assertions.assertEquals(200, response3.getStatusCode());
        Assertions.assertEquals(1, jsonPath3.getList("messages").size());
        Assertions.assertEquals("hola", jsonPath3.getList("messages.text").get(0));
    }

    //[Prueba43] Enviar un primer mensaje una oferta propia y comprobar que no se inicia la conversación.
    //En este caso de prueba, el propietario de la oferta tendrá que identificarse (S1), enviar un mensaje
    //para una oferta propia (S3) y comprobar que el mensaje no se almacena (S4).
    @Test
    @Order(43)
    public void PR43() {
        // Servicio S1
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Servicio S3
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/messages/send";
        RequestSpecification request2 = RestAssured.given();
        JSONObject requestParams2 = new JSONObject();
        requestParams2.put("offer", m.getOneOfferIdByAuthor("user16@email.com")); // oferta propia
        requestParams2.put("text", "hola");
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));
        request2.body(requestParams2.toJSONString());

        Response response2 = request2.post(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        // Servicio S4 (no se puede invocar porque hay que darle una conversación y no pueden existir conversaciones
        // de un usuario consigo mismo. Por ello, se comprueba que lo devuelto por el servicio anterior refleje el fallo)
        Assertions.assertEquals(500, response2.getStatusCode());
        Assertions.assertEquals("El usuario no puede iniciar una conversación por un producto propio.", jsonPath2.get("error"));
    }

    //[Prueba44] Obtener los mensajes de una conversación. Esta prueba consistirá en comprobar que el
    //servicio retorna el número correcto de mensajes para una conversación. El ID de la conversación
    //deberá conocerse a priori. Por lo tanto, se tendrá primero que invocar al servicio de identificación
    //(S1), y solicitar el listado de mensajes de una conversación de id conocido a continuación (S4),
    //comprobando que se retornan los mensajes adecuados.
    @Test
    @Order(44)
    public void PR44() {
        // Previo P1: Inicio de sesión con un participante de una conver ya existente
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user17@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Previo P2: Envío de un mensaje más a la conversación ya existente para probar bien
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/messages/send";
        RequestSpecification request2 = RestAssured.given();
        JSONObject requestParams2 = new JSONObject();
        String idConver = m.getOneConverIdByParticipants("user17@email.com", "user16@email.com");
        requestParams2.put("conver", idConver);
        requestParams2.put("text", "buenas");
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));
        request2.body(requestParams2.toJSONString());

        Response response2 = request2.post(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        // Servicio S1 (Nos autenticamos con el otro participante de la conver)
        final String RestAssuredURL3 = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request3 = RestAssured.given();
        JSONObject requestParams3 = new JSONObject();
        requestParams3.put("email", "user16@email.com");
        requestParams3.put("password", "admin");
        request3.header("Content-Type", "application/json");
        request3.body(requestParams3.toJSONString());

        Response response3 = request3.post(RestAssuredURL3);

        JsonPath jsonPath3 = response3.jsonPath();

        // Servicio S4
        final String RestAssuredURL4 = "http://localhost:8080/api/v1.0/messages/fromconver/" + idConver;
        RequestSpecification request4 = RestAssured.given();
        request4.header("Content-Type", "application/json");
        request4.header("token", jsonPath3.get("token"));

        Response response4 = request4.get(RestAssuredURL4);

        JsonPath jsonPath4 = response4.jsonPath();

        Assertions.assertEquals(200, response4.getStatusCode());
        Assertions.assertEquals(2, jsonPath4.getList("messages").size());
        Assertions.assertEquals("hola", jsonPath4.getList("messages.text").get(0));
        Assertions.assertEquals("buenas", jsonPath4.getList("messages.text").get(1));
    }

    //[Prueba45] Obtener la lista de conversaciones de un usuario. Esta prueba consistirá en comprobar que
    //el servicio retorna el número correcto de conversaciones para dicho usuario. Por lo tanto, se tendrá
    //primero que invocar al servicio de identificación (S1), y solicitar el listado de conversaciones a
    //continuación (S5) comprobando que se retornan las conversaciones adecuadas.
    @Test
    @Order(45)
    public void PR45() {
        // Servicio S1
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Servicio S5
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/convers/all";
        RequestSpecification request2 = RestAssured.given();
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));

        Response response2 = request2.get(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        Assertions.assertEquals(200, response2.getStatusCode());
        Assertions.assertEquals(1, jsonPath2.getList("convers").size());
    }

    //[Prueba46] Eliminar una conversación de ID conocido. Esta prueba consistirá en comprobar que se
    //elimina correctamente una conversación concreta. Por lo tanto, se tendrá primero que invocar al
    //servicio de identificación (S1), eliminar la conversación ID (S6) y solicitar el listado de
    //conversaciones a continuación (S5), comprobando que se retornan las conversaciones adecuadas.
    @Test
    @Order(46)
    public void PR46() {
        // Servicio S1
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Servicio S6
        String idConver = m.getOneConverIdByParticipants("user17@email.com", "user16@email.com");
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/convers/delete/" + idConver;
        RequestSpecification request2 = RestAssured.given();
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));

        Response response2 = request2.delete(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        // Servicio S5
        final String RestAssuredURL3 = "http://localhost:8080/api/v1.0/convers/all";
        RequestSpecification request3 = RestAssured.given();
        request3.header("Content-Type", "application/json");
        request3.header("token", jsonPath.get("token"));

        Response response3 = request3.get(RestAssuredURL3);

        JsonPath jsonPath3 = response3.jsonPath();

        Assertions.assertEquals(200, response3.getStatusCode());
        Assertions.assertEquals(0, jsonPath3.getList("convers").size());
    }

    //[Prueba47] Marcar como leído un mensaje de ID conocido. Esta prueba consistirá en comprobar que
    //el mensaje marcado de ID conocido queda marcado correctamente a true como leído. Por lo
    //tanto, se tendrá primero que invocar al servicio de identificación (S1), solicitar el servicio de
    //marcado (S7), comprobando que el mensaje marcado ha quedado marcado a true como leído (S4).
    @Test
    @Order(47)
    public void PR47() {
        // Previo P1: Inicio de sesión para crear una conver enviando un nuevo mensaje
        final String RestAssuredURL = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request = RestAssured.given();
        JSONObject requestParams = new JSONObject();
        requestParams.put("email", "user16@email.com");
        requestParams.put("password", "admin");
        request.header("Content-Type", "application/json");
        request.body(requestParams.toJSONString());

        Response response = request.post(RestAssuredURL);

        JsonPath jsonPath = response.jsonPath();

        // Previo P2: Envío del mensaje para disponer de un mensaje que marcar como leído
        final String RestAssuredURL2 = "http://localhost:8080/api/v1.0/messages/send";
        RequestSpecification request2 = RestAssured.given();
        JSONObject requestParams2 = new JSONObject();
        requestParams2.put("offer", m.getOneOfferIdByAuthor("user17@email.com")); // oferta de otro
        requestParams2.put("text", "hola");
        request2.header("Content-Type", "application/json");
        request2.header("token", jsonPath.get("token"));
        request2.body(requestParams2.toJSONString());

        Response response2 = request2.post(RestAssuredURL2);

        JsonPath jsonPath2 = response2.jsonPath();

        // Servicio S1 (Nos autenticamos con el otro participante de la conver para que pueda marcar a leído)
        final String RestAssuredURL3 = "http://localhost:8080/api/v1.0/users/login";
        RequestSpecification request3 = RestAssured.given();
        JSONObject requestParams3 = new JSONObject();
        requestParams3.put("email", "user17@email.com");
        requestParams3.put("password", "admin");
        request3.header("Content-Type", "application/json");
        request3.body(requestParams3.toJSONString());

        Response response3 = request3.post(RestAssuredURL3);

        JsonPath jsonPath3 = response3.jsonPath();

        // Servicio S7
        String idMessage = m.getOneMessageIdByAuthor("user16@email.com");
        final String RestAssuredURL4 = "http://localhost:8080/api/v1.0/messages/markasread/" + idMessage;
        RequestSpecification request4 = RestAssured.given();
        request4.header("Content-Type", "application/json");
        request4.header("token", jsonPath3.get("token"));

        Response response4 = request4.put(RestAssuredURL4);

        JsonPath jsonPath4 = response4.jsonPath();

        // Servicio S4
        String idConver = m.getOneConverIdByParticipants("user17@email.com", "user16@email.com");
        final String RestAssuredURL5 = "http://localhost:8080/api/v1.0/messages/fromconver/" + idConver;
        RequestSpecification request5 = RestAssured.given();
        request5.header("Content-Type", "application/json");
        request5.header("token", jsonPath3.get("token"));

        Response response5 = request5.get(RestAssuredURL5);

        JsonPath jsonPath5 = response5.jsonPath();

        Assertions.assertEquals(200, response5.getStatusCode());
        Assertions.assertEquals(1, jsonPath5.getList("messages").size());
        Assertions.assertEquals("hola", jsonPath5.getList("messages.text").get(0));
        Assertions.assertEquals(true, jsonPath5.getList("messages.read").get(0));
    }

}
