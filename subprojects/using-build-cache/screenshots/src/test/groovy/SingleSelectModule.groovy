import geb.Module

class SingleSelectModule extends Module {

    static content = {
        hasValue { hasClass('has-value')  }
        selectedLabel { hasValue ? $('.Select-value-label .SortSelector__label').text() : null }
        arrow { $('.Select-arrow') }
    }

    void select(String label) {
        arrow.click()
        waitFor { $('.Select-menu .Select-option').find { it.text() == label } }.click()
        waitFor { selectedLabel == label }
    }

}
