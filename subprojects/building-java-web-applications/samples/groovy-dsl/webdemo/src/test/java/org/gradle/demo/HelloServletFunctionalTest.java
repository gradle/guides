package org.gradle.demo;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.Assert.assertEquals;

public class HelloServletFunctionalTest {
    private WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        ChromeDriverManager.getInstance().setup(); // <1>
    }

    @Before
    public void setUp() {
        driver = new ChromeDriver();               // <2>
    }

    @After
    public void tearDown() {
        if (driver != null)
            driver.quit();                         // <3>
    }

    @Test
    public void sayHello() throws Exception {      // <4>
        driver.get("http://localhost:8080/webdemo");

        driver.findElement(By.id("say-hello-text-input")).sendKeys("Dolly");
        driver.findElement(By.id("say-hello-button")).click();

        assertEquals("Hello Page", driver.getTitle());
        assertEquals("Hello, Dolly!", driver.findElement(By.tagName("h2")).getText());
    }
}
