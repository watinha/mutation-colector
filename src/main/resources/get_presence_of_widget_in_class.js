var target = arguments[0],
    childElements = target.querySelectorAll("*"),
    classNames = "",
    widget_name = ("dropdown,drop-down,menu,tooltip,dialog,popup,pop-up").split(",");

for (var i = 0; i < childElements.length; i++) {
    classNames += " " + childElements[i].className;
};
for (var j = 0; j < widget_name.length; j++) {
    if(classNames.search(widget_name[j]) >= 0)
        return 1;
};
return 0;
