var target = arguments[0],
    table = target.querySelector("table"),
    ul = target.querySelector("ul"),
    childs, childs2;

if (table) {
    childs = table.querySelectorAll("*").length;
    childs2 = table.querySelector("a").length;
    if ((childs2/childs) > 0.8)
        return 1;
    else
        return 0;
}
if (ul) {
    childs = ul.querySelectorAll("*").length;
    childs2 = ul.querySelector("a").length;
    if ((childs2/childs) > 0.8)
        return 1;
    else
        return 0;
}
return 0;
