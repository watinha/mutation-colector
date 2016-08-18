var webpage = require("webpage"),
    system = require("system"),
    page = webpage.create();

if (system.args.length < 2) {
    console.log("Arguments missing...");
    phantom.exit();
}
page.onError = function () {};
page.onInitialized = function () {
    page.evaluate(function () {
        var true_addEventListener = HTMLElement.prototype.addEventListener,
            hover_events = ["mouseover"];
        window.events = [];
        HTMLElement.prototype.addEventListener = function (ev_type) {
            if (hover_events.indexOf(ev_type) >= 0) {
                var target = this,
                    selector = "";

                while (target.parentElement != null) {
                    selector = (selector.length === 0 ?
                        target.tagName.toLowerCase() :
                        target.tagName.toLowerCase() + " > " + selector);
                    target = target.parentElement;
                }
                if (selector.length > 0)
                    window.events.push(selector);
            }
            true_addEventListener.apply(this, arguments);
        };
    });
};
page.settings.XSSAuditingEnabled = true;
page.settings.webSecurityEnabled = false;
page.open(system.args[1], function () {
    setTimeout(function () {
        console.log(page.evaluate(function () {
            // onmouseover listeners
            var all = document.querySelectorAll("*");
            for (var l = 0; l < all.length; l++) {
                if (all[l].onmouseover) {
                    var target = all[l],
                        selector = "";

                    while (target.parentElement != null) {
                        selector = (selector.length === 0 ?
                            target.tagName.toLowerCase() :
                            target.tagName.toLowerCase() + " > " + selector);
                        target = target.parentElement;
                    }
                    if (selector.length > 0)
                        window.events.push(selector);
                }
            };

            return window.events.join(",");
        }));
        phantom.exit();
    }, 10000);
});
