document.addEventListener("DOMContentLoaded", function(event) {
    (function(i,s,o,g,r,a,m){i["GoogleAnalyticsObject"]=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,"script","https://www.google-analytics.com/analytics.js","ga");

    window.ga("create", "UA-4207603-1", "auto", "all");
    window.ga("create", "UA-4207603-11", "auto", "guides");
    window.ga("set", "transport", "beacon");
    window.ga("all.send", "pageview");
    window.ga("guides.send", "pageview");

    var timer;
    function trackReadPosition() {
        // Use timeout to process read position only when the user is done scrolling.
        if (timer) {
            window.clearTimeout(timer);
        }

        timer = window.setTimeout(function() {
            var sections = document.querySelectorAll("h2[id]");
            var windowHeight = window.innerHeight;

            // Assign active section: take advantage of fact that querySelectorAll returns elements in source order
            var activeSection = sections[0];
            Array.prototype.forEach.call(sections, function(section) {
                // NOTE: Here we are considering the content 1/3rd from the top of the window to be in "focus"
                if (Math.floor(section.offsetTop) <= (window.scrollY + (windowHeight / 3))) {
                    activeSection = section;
                }
            });

            if (activeSection != null && activeSection.hasAttribute("href")) {
                var activeHref = activeSection.getAttribute("href");

                // Send event of read section to Google Analytics
                ga("guides.send", {hitType: "event", eventCategory: document.location.pathname, eventAction: "read", eventLabel: activeHref});
            } else if (activeSection != null && activeSection.id) {
                ga("guides.send", {hitType: "event", eventCategory: document.location.pathname, eventAction: "read", eventLabel: "#" + activeSection.id});
            }
        }, 150);
    }

    /**
     * Given an event object, determine if the source element was a link, and track it with Google Analytics if it goes to another domain.
     * @param {Event} evt object that should be fired due to a link click.
     * @return boolean if link was successfully tracked.
     */
    function trackOutbound(evt) {
        var targetLink = evt.target.closest("a");
        if (!targetLink) {
            return false;
        }

        var href = targetLink.getAttribute("href");
        if (!href || href.substring(0, 4) !== "http") {
            return false;
        }

        if(href.indexOf(document.domain) === -1 || !document.domain) {
            ga("guides.send", {hitType: "event", eventCategory: document.location.pathname, eventAction: "outbound referral", eventLabel: href});
            ga("all.send", {hitType: "event", eventCategory: document.location.href, eventAction: "outbound referral", eventLabel: href});
            return true;
        }
        return false;
    }

    function trackCustomEvent(evt) {
        var eventTarget = evt.target.closest(".js-analytics-event");
        if (eventTarget !== null) {
            var eventAction = eventTarget.getAttribute("data-action");
            var eventLabel = eventTarget.getAttribute("data-label");
            ga("guides.send", {hitType: "event", eventCategory: document.location.pathname, eventAction: eventAction, eventLabel: eventLabel});
            return true;
        }
        return false;
    }

    window.addEventListener("scroll", trackReadPosition);
    window.addEventListener("click", trackReadPosition);
    trackReadPosition();
    document.addEventListener("click", trackOutbound, false);
    document.addEventListener("click", trackCustomEvent, false);

    window.piAId = '69052';
    window.piCId = '2332';
    (function() {
        function async_load() {
            var s = document.createElement('script'); s.type = "text/javascript";
            s.src = ('https:' == document.location.protocol ? 'https://pi' : 'http://cdn') + '.pardot.com/pd.js';
            var c = document.getElementsByTagName('script')[0]; c.parentNode.insertBefore(s, c);
        }
        if(window.attachEvent) { window.attachEvent('onload', async_load); }
        else { window.addEventListener('load', async_load, false); }
    })();
});
