import geb.Page

class ScanTimelinePage extends Page {
    final String subPath = "timeline"
    static at = { $('.TimelinePage').hasClass('loaded') }
    static content = {
        list { $('.TimelineList').module(TimelineList) }
        taskDetails(required: false) { $('.TaskDetails').module(TimelineTaskDetailsModule) }
        adjacentScans(required: false) { $('body').module(AdjacentScanModule) }
    }
}
