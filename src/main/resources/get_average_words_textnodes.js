var target = arguments[0],
    aux = null, elements = [],
    number_of_words = 0;

elements.push(target);
while (elements.length != 0) {
    aux = elements.pop();
    for (var i = 0; i < aux.childNodes.length; i++) {
        if (aux.childNodes[i].nodeType === 3) {
            number_of_words += aux.childNodes[i].nodeValue.split(" ").length;
        }
        if (aux.childNodes[i].nodeType === 1)
            elements.push(aux.childNodes[i]);
    }
}
return number_of_words;
