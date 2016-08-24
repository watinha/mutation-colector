window.mutations_observed = [];
var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        var target = mutation.target;
        window.mutations_observed.push(target);
        if (mutation.addedNodes) {
            for (var i = 0; i < mutation.addedNodes.length; i++) {
                window.mutations_observed.push(mutation.addedNodes[i]);
            };
        }

    });
});
observer.observe(document.body, {
    attributes: true,
    childList: true,
    subtree: true
});
