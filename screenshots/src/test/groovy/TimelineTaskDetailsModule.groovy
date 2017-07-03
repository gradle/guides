import geb.Module

class TimelineTaskDetailsModule extends Module {
    static content = {
        startedAfter(required: true) { $('.TaskDetails__started-after').text() }
        duration(required: true) { $('.TaskDetails__duration').text() }
        className(required: true) { $('.TaskDetails__class').text() }
        cacheKey(required: false) { $('.TaskDetails__cache-key')?.text() }
        knownUpToDateMessages(required: false) { $('.TaskDetails__known-up-to-date-message')*.text()*.trim() }
        unknownUpToDateMessages(required: false) { $('.TaskDetails__unknown-up-to-date-message')*.text()*.trim() }

        originScanButton(required: false) { $('.TaskDetails__origin-scan-button') }
        focusButton { $('.TaskDetails__focus-button') }
    }

    void clickOriginScanButton() {
        waitFor { originScanButton.displayed }
        originScanButton.click()
    }

    void clickFocusButton() {
        waitFor { focusButton.displayed }
        focusButton.click()
    }
}
