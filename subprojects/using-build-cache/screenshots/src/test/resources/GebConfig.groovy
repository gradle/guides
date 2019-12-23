import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities

waiting {
    timeout = 5
}

reportsDir = new File("build/runtime_reports_dir")

driver = {
    ChromeOptions options = new ChromeOptions()
    DesiredCapabilities capabilities = DesiredCapabilities.chrome()

    String chromiumPath = "/usr/bin/chromium-browser"
    // Currently, we need chrome canary (61.0.3125.0) for screenshots to work on Mac in headless mode.
    String macChromePath = "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"

    def chromiumBinary = new File(chromiumPath)
    def isAgent = chromiumBinary.exists()
    def macChromeBinary = new File(macChromePath)
    if (isAgent) {
        options.setBinary(chromiumBinary) //Set binary using file to avoid NoClassDefFound error on mac
    } else if (macChromeBinary.exists()) {
        options.setBinary(macChromeBinary)
    }

    options.addArguments("headless", "disable-gpu")
    options.addArguments("window-size=1400,800")
    capabilities.setCapability(ChromeOptions.CAPABILITY, options)
    new ChromeDriver(capabilities)
}
