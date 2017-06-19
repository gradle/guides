import geb.Module

class TimelineList extends Module {
    static content = {
        rows { $('.TimelineListRow').moduleList(TimelineListRow) }
        sortSelector { $('.SortSelector .Select').module(SingleSelectModule) }
    }

    boolean isScrolledToRow(TimelineListRow row) {
        firstElement().location.y == row.firstElement().location.y
    }
}

class TimelineListRow extends Module {
    static content = {
        pathElement { $('.TimelineListRow__path') }
        path { pathElement.text() }
        outcome(required: false) { $('.TimelineListRow__outcome').text() }
        scanLink(required: false) { $('.TimelineListRow__buttons a') }
        showDetailsButton(required: false) { $('.TimelineListRow__details-button') }
        duration { $('.TimelineListRow__duration').text() }
        offset { $('.TimelineListRow__offset').text() }
        cacheableTypeCell(required: false) { $('.TimelineListRow__cacheable-type') }
        cacheabilityType(required: false) { cacheableTypeCell.text() }
        selected { hasClass('TimelineListRow--selected') }
    }

    void doubleClick() {
        interact {
            doubleClick(this)
        }
    }

    void hoverOverRow() {
        interact {
            moveToElement(pathElement)
        }
    }

    void gotoOriginalScan() {
        hoverOverRow()
        scanLink.click()
    }

    void showDetails() {
        pathElement.click()
        waitFor { hasClass('TimelineListRow--showing-task-details') }
    }
}
