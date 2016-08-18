window.mutations_observed = [];
var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        window.mutations_observed.push(target);
        if (target.addedNodes) {
            for (var i = 0; i < target.addedNodes.length; i++) {
                window.mutations.push(target.addedNodes[i]);
            };
        }

    });
});
observer.observe(document.body, {
    attributes: true,
    childList: true,
    characterData: true,
    subtree: true
});
