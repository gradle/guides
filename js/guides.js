// Polyfill Element.matches()
if (!Element.prototype.matches) {
    Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
}
// Polyfill Element.closest()
if (!Element.prototype.closest) {
    Element.prototype.closest = function(s) {
        var el = this;
        if (!document.documentElement.contains(el)) return null;
        do {
            if (typeof el.matches === "function" && el.matches(s)) return el;
            el = el.parentElement || el.parentNode;
        } while (el !== null);
        return null;
    };
}

function registerNavigationActions() {
    var navigationButton = document.querySelector(".site-header__navigation-button");
    var navigationCollapsible = document.querySelector(".site-header__navigation-collapsible");

    if (navigationButton) {
        navigationButton.addEventListener("click", function () {
            navigationCollapsible.classList.toggle("site-header__navigation-collapsible--collapse");
        });
    }

    var allSubmenus = document.querySelectorAll(".site-header__navigation-submenu-section");
    Array.prototype.forEach.call(allSubmenus, function (submenu) {
        var focusinOpensSubmenu = false;

        document.addEventListener("focusout", function (event) {
            if (submenu.contains(event.target)) {
                focusinOpensSubmenu = false;
            }
        });

        document.addEventListener("focusin", function () {
            if (submenu.contains(document.activeElement)) {
                submenu.classList.add("open");
                focusinOpensSubmenu = true;
            } else {
                submenu.classList.remove("open");
            }
        });

        document.addEventListener("click", function (event) {
            if (!focusinOpensSubmenu) {
                if (submenu.contains(event.target)) {
                    submenu.classList.toggle("open");
                } else {
                    submenu.classList.remove("open");
                }
            } else {
                focusinOpensSubmenu = false;
            }
        });
    });
}

// Add "active" class to TOC link corresponding to subsection at top of page
function setActiveSubsection(activeHref) {
    var tocLinkToActivate = document.querySelector(".toc a[href$='"+activeHref+"']");
    var currentActiveTOCLink = document.querySelector(".toc a.active");
    if (tocLinkToActivate != null) {
        if (currentActiveTOCLink != null && currentActiveTOCLink !== tocLinkToActivate) {
            currentActiveTOCLink.classList.remove("active");
        }
        tocLinkToActivate.classList.add("active");
    }
}

function calculateActiveSubsectionFromLink(event) {
    var closestLink = event.target.closest("a[href]");
    if (closestLink) {
        setActiveSubsection(closestLink.getAttribute("href"));
    }
}

function calculateActiveSubsectionFromScrollPosition() {
    var subsections = document.querySelectorAll("h2[id] > a.link");

    // Assign active section: take advantage of fact that querySelectorAll returns elements in source order
    var activeSection = subsections[0];
    Array.prototype.forEach.call(subsections, function(section) {
        if (Math.floor(section.offsetTop) <= (window.scrollY + 50)) {
            activeSection = section;
        }
    });

    if (activeSection != null && activeSection.hasAttribute("href")) {
        setActiveSubsection(activeSection.getAttribute("href"));
    }
}

function postProcessNavigation() {
    [].forEach.call(document.querySelectorAll(".docs-navigation a[href$='"+ window.location.pathname +"']"), function(link) {
        // Add "active" to all links same as current URL
        link.classList.add("active");

        // Expand all parent navigation
        var parentListEl = link.closest("li");
        while (parentListEl !== null) {
            var dropDownEl = parentListEl.querySelector(".nav-dropdown");
            if (dropDownEl !== null) {
                dropDownEl.classList.add("expanded");
            }
            parentListEl = parentListEl.parentNode.closest("li");
        }
    });

    function throttle(fn, periodMs) {
        var time = Date.now();
        var context = this;
        var args = Array.prototype.slice.call(arguments);
        return function() {
            if ((time + periodMs - Date.now()) < 0) {
                fn.apply(context, args);
                time = Date.now();
            }
        }
    }

    window.addEventListener("click", calculateActiveSubsectionFromLink);
    window.addEventListener("scroll", throttle(calculateActiveSubsectionFromScrollPosition, 50));
    calculateActiveSubsectionFromScrollPosition();
}

document.addEventListener("DOMContentLoaded", function() {
    (function(i,s,o,g,r,a,m){i["GoogleAnalyticsObject"]=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,"script","https://www.google-analytics.com/analytics.js","ga");

    window.ga("create", "UA-4207603-1", "auto", "all", {
        "allowLinker": true
    });
    window.ga("all.require", "linker");
    window.ga("all.linker:autoLink", ["gradle.com", "gradle.org"], false, true);
    window.ga("create", "UA-4207603-11", "auto", "guides");
    window.ga("all.set", "transport", "beacon");
    window.ga("guides.set", "transport", "beacon");
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

    // Newsletter Submission
    var newsletterForm = document.getElementById('newsletter-form')
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function() { track('Newsletter', 'Subscribed', 'Guides Footer') }, false);
    }

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

    registerNavigationActions();
    postProcessNavigation();
});


