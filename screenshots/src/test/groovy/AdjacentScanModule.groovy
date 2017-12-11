import geb.Module

class AdjacentScanModule extends Module {

    static content = {
        toggleButton { $('.AdjacentScansButton .SvgIconButton') }
        popover(required: false) { $('.AdjacentScansPopover') }
        rows(required:false) { $('.AdjacentScansPopover .AdjacentScanRow').moduleList(AdjacentScanRowModule) }
    }

    def toggleAdjacentScans() {
        waitFor { toggleButton.displayed }
        toggleButton.click()
        waitFor { popover.displayed }
        waitFor { rows.size() > 1 }
    }

    def loadComparison(int idx) {
        rows[idx].compareButton.click()
    }
}

class AdjacentScanRowModule extends Module {

    static content = {
        compareButton(required: false) { $('.AdjacentScanRow__compare .HoverButton')}
    }

}
