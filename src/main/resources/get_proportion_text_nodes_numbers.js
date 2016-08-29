var target = arguments[0],
    aux = null, elements = [],
    number_of_numbers = 0;

elements.push(target);
while (elements.length != 0) {
    aux = elements.pop();
    for (var i = 0; i < aux.childNodes.length; i++) {
        if (aux.childNodes[i].nodeType === 3) {
            if (!isNaN(parseInt(aux.childNodes[i].nodeValue)))
                number_of_numbers++;
        }
        if (aux.childNodes[i].nodeType === 1)
            elements.push(aux.childNodes[i]);

    }
}
return number_of_numbers;
