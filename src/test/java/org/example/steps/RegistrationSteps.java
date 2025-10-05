package org.example.steps;

import io.cucumber.java.After;
import io.cucumber.java.ru.*;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Epic("Регистрация")
@Feature("Регистрация пользователя")
public class RegistrationSteps {

    private WebDriver driver;
    private WebDriverWait wait;

    private String browser;
    private String mode;
    private String selenoidUrl;

    public void getConfig() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        browser = System.getenv().getOrDefault("BROWSER", props.getProperty("browser", "chrome"));
        mode = System.getenv().getOrDefault("MODE", props.getProperty("mode", "local"));
        selenoidUrl = System.getenv().getOrDefault("SELENOID.URL", props.getProperty("selenoid.url", "http://localhost:4444/wd/hub"));
    }

    private void initDriver() throws MalformedURLException {
        if (mode.equalsIgnoreCase("remote")) {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            Map<String, Object> selenoidOptions = new HashMap<>();
            capabilities.setBrowserName(browser);
            selenoidOptions.put("enableVNC", true);
            selenoidOptions.put("enableVideo", false);
            capabilities.setCapability("selenoid:options", selenoidOptions);
            driver = new RemoteWebDriver(new URL(selenoidUrl), capabilities);
        } else {
            if ("chrome".equalsIgnoreCase(browser)) {
                System.setProperty("webdriver.chrome.driver", "/home/nikita/IdeaProjects/cucumber-tests/src/test/resources/chromedriver");
                driver = new ChromeDriver();
            } else if ("firefox".equalsIgnoreCase(browser)) {
                System.setProperty("webdriver.gecko.driver", "/home/nikita/IdeaProjects/cucumber-tests/src/test/resources/geckodriver.exe");
                driver = new FirefoxDriver();
            } else if ("edge".equalsIgnoreCase(browser)) {
                System.setProperty("webdriver.edge.driver", "/home/nikita/IdeaProjects/cucumber-tests/src/test/resources/msedgedriver");
                driver = new EdgeDriver();
            }
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Step("Открываем страницу {url}")
    @Дано("Совершен вход в магазин OpenCart {string}")
    public void openBrowser(String url) throws IOException {
        getConfig();
        initDriver();
        driver.get(url);
    }

    @Step("Заполняем форму данными")
    @Когда("Форма регистрации заполняется данными:")
    public void fillForm(DataTable table) {
        Map<String, String> data = table.asMap(String.class, String.class);
        driver.findElement(By.name("firstname")).sendKeys(data.get("firstname"));
        driver.findElement(By.name("lastname")).sendKeys(data.get("lastname"));
        driver.findElement(By.name("email")).sendKeys(data.get("email"));
        driver.findElement(By.name("password")).sendKeys(data.get("password"));
    }

    @Step("Включаем подписку на новости")
    @Когда("Включается подписка на новости")
    public void enableNewsletter() {
        WebElement newsletter = driver.findElement(By.xpath("//input[@id='input-newsletter']"));
        newsletter.click();
        Assertions.assertTrue(newsletter.isSelected());
    }

    @Step("Принимаем условия соглашения")
    @Когда("Принимаются условия соглашения")
    public void acceptPrivacyPolicy() {
        WebElement agree = driver.findElement(By.xpath("//input[@name='agree']"));
        agree.click();
        Assertions.assertTrue(agree.isSelected());
    }

    @Step("Нажимаем кнопку продолжить")
    @Когда("Нажимается кнопка Продолжить")
    public void clickContinueButton() {
        WebElement button = driver.findElement(By.xpath("//button[@class='btn btn-primary']"));
        button.click();
    }

    @Step("Проверка успешной регистрации")
    @Тогда("Выводится сообщение об успешной регистрации")
    public void SuccessMessage() {
        wait.until(ExpectedConditions.urlContains("success"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("success"));
    }

    @Step("Проверка ошибки регистрации")
    @Тогда("Отображается сообщение об ошибке")
    public void ErrorMessage() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".invalid-feedback.d-block"))
        );
        Assertions.assertTrue(error.isDisplayed(), "Регистрация прошла с неверными полями");
    }

    @After
    public void closeDriver() {
        driver.quit();
    }
}