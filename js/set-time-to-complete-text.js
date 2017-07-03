document.addEventListener("DOMContentLoaded", function() {
    var AVERAGE_WPM = 120;

    var content = document.querySelector("#content");

    var NUMBER_OF_WORDS = content.innerText.split(" ").length;
    var TIME_TO_COMPLETE = Math.ceil(Math.round(NUMBER_OF_WORDS / AVERAGE_WPM) / 10) * 10 + " minutes";

    var allTimeToCompleteTexts = document.querySelectorAll(".time-to-complete-text");
    [].forEach.call(allTimeToCompleteTexts, function(timeToCompleteText) {
        timeToCompleteText.textContent = TIME_TO_COMPLETE;
    });
});
