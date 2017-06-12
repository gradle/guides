import geb.Browser
import geb.navigator.Navigator
import geb.waiting.DefaultWaitingSupport
import geb.waiting.Wait
import org.junit.Test
import org.openqa.selenium.OutputType
import ratpack.test.CloseableApplicationUnderTest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class TakeScreenshots {

    Path screenshotDir = Paths.get(System.getProperty('screenshot.dir'))

    private Map<String, Closure> extraActions = [:]

    TakeScreenshots() {
        extraActions.put('from-cache-origin') {
            waitFor {
                at(ScanTimelinePage)
            }

            TimelineListRow row = page.list.rows[5]
            row.hoverOverRow()
            interact {
                moveToElement(row.scanLink)
            }
        }
        extraActions.put('overlapping-outputs-timeline') {
            waitFor {
                at(ScanTimelinePage)
            }

            TimelineListRow row = page.list.rows[0]
            interact {
                moveToElement(row.cacheableTypeCell)
            }
        }

        ['first-non-cached-task', 'caching-disabled'].each {
            extraActions.put(it) {
                waitFor {
                    at(ScanTimelinePage)
                }
            }
        }

        extraActions.put('performance-task-execution') {
            waitFor {
                $('.PerformancePage').hasClass('loaded')
            }
        }

        ['task-inputs-comparison', 'overlapping-outputs-input-comparison'].each {
            extraActions.put(it) {
                waitFor {
                    $('.TaskInputs')
                }
            }
        }
    }

    @Test
    void take_screenshots() {
        def properties = new Properties()
        properties.load(getClass().getResourceAsStream("/screenshots.properties"))

        properties.each { String name, String urls ->
            def (String instance, String subUrl) = urls.split(':')
            CloseableApplicationUnderTest proxy = AuthingProxy.to(*getCredentials(instance))

            try {
                proxy.test { httpClient ->
                    def url = httpClient.applicationUnderTest.address
                    takeScreenshot("${url}${subUrl}", name)
                }
            } finally {
                proxy.close()
            }
        }
    }

    private takeScreenshot(String url, String screenshotName) {
        Browser.drive {
            go url

            waitFor(2000) { !find('.GradlephantLoadingIndicator') }
            waitForAnimation(delegate, '.LoadingWrapper', 'transition-opacity-slow')
            waitForScroll(delegate)

            if (extraActions.containsKey(screenshotName)) {
                Closure extraAction = extraActions[screenshotName].clone()
                extraAction.delegate = delegate
                extraAction.call()
            }

            def screenshot = screenshotDir.resolve("${screenshotName}.png")
            if (Files.exists(screenshot)) {
                Files.delete(screenshot)
            }
            Files.copy(delegate.driver.getScreenshotAs(OutputType.FILE).toPath(), screenshot)
        }
    }

    private static getCredentials(String instance) {
        String prefix = "scans.${instance}"
        [System.getProperty("${prefix}.host"), System.getProperty("${prefix}.username"), System.getProperty("${prefix}.password")]
    }

    static void waitForAnimation(Browser browser, String selector, String transitionName = "transition-height-and-opacity") {
        waitFor(browser) {
            !(browser.find("$selector .${transitionName}-enter-active") || browser.find("$selector .${transitionName}-leave-active"))
        }
    }

    static void waitForScroll(Browser browser) {
        AtomicInteger top = new AtomicInteger(getScrollPosition(browser))
        Thread.sleep(1000)
        new Wait().waitFor {
            int oldTop = top.get()
            def newTop = getScrollPosition(browser)
            top.set(newTop)
            newTop == oldTop
        }
    }

    static int getScrollPosition(Browser browser) {
        waitFor(browser) {
            browser.js.exec('return document.documentElement.scrollTop || document.body.scrollTop') != null
        }
        return browser.js.exec('return document.documentElement.scrollTop || document.body.scrollTop') as int
    }

    static <T> T waitFor(Browser browser, Closure<T> block) {
        new DefaultWaitingSupport(browser.config).waitFor(block)
    }

    static List<String> highlights(Navigator base) {
        base.find('mark')*.text()
    }
}
