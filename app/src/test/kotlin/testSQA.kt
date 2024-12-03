package com.example.myapp.tests

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.junit.Assert
import org.junit.Test
import org.openqa.selenium.remote.DesiredCapabilities
import java.net.URL

class LoginTests {
    @Test
    fun testLoginButton() {
        val capabilities = DesiredCapabilities()
        capabilities.setCapability("platformName", "Android")
        capabilities.setCapability("deviceName", "emulator-5554")
        capabilities.setCapability("app", "/ruta/a/miApp.apk")
        capabilities.setCapability("automationName", "UiAutomator2")

        val driver = AndroidDriver<MobileElement>(URL("http://127.0.0.1:4723/wd/hub"), capabilities)

        val loginButton = driver.findElementById("com.example.myapp:id/login_button")
        loginButton.click()

        // Verifica el resultado esperado
        val result = driver.findElementById("com.example.myapp:id/result_text")
        Assert.assertEquals("Bienvenido", result.text)

        driver.quit()
    }
}