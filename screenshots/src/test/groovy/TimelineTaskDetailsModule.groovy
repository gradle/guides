import geb.Module
import geb.navigator.Navigator

class TimelineTaskDetailsModule extends Module {
    static content = {
        startedAfter(required: true) { $('.SummaryDetails__started-after').text() }
        duration(required: true) { $('.SummaryDetails__duration').text() }
        className(required: true) { $('.SummaryDetails__class').text() }

        notCacheableInfoIcon(required: false) { $('.BuildCacheDetails__non-cacheable-info-icon') }
        buildCacheResultRow(required: false) { $('.BuildCacheDetails__build-cache-outcome').module(TimelineTaskDetailsRow) }
        buildCacheToggleLink(required: false) { buildCacheResultRow.labelCell }
        cacheKey(required: false) { $('.BuildCacheDetails__cache-key')?.text() }

        knownUpToDateMessages(required: false) { $('.UpToDateDetails__known-up-to-date-message')*.text()*.trim() }
        unknownUpToDateMessages(required: false) { $('.UpToDateDetails__unknown-up-to-date-message')*.text()*.trim() }

        originScanButton(required: false) { $('.TaskDetailsButtons__origin-scan-button') }
        originScanPopup(required: false) { popupFor(originScanButton) }
        focusButton { $('.TaskDetailsButtons__focus-button') }
    }

    void toggleBuildCacheDetails() {
        waitFor { buildCacheToggleLink.displayed }
        buildCacheToggleLink.click()
    }

    void clickOriginScanButton() {
        waitFor { originScanButton.displayed }
        originScanButton.click()
    }

    void clickFocusButton() {
        waitFor { focusButton.displayed }
        focusButton.click()
    }

    private popupFor(Navigator element) {
        element.parent().next('.TooltipWrapper__tooltip-section').$("div")
    }
}

class TimelineTaskDetailsRow extends Module {

    static content = {
        labelCell { $('.TaskDetailsRow__label') }
        label { $('td')[0].text() }
        value(required: false) { $('td')[1].text() }
        isFailed { hasClass('TaskDetailsRow--isFailed') }
    }

}
