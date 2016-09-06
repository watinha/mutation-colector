var target = arguments[0],
    child_elements = target.querySelectorAll("*");

for (var j = 0; j < child_elements.length; j++) {
    if(child_elements[j].type === "date")
        return 1;
};
return 0;
