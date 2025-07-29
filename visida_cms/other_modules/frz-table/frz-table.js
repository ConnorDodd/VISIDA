/**
* frz-table.js is a Javascript library to facilitate HTML table-like displays with frozen rows and/or columns simultaneously.
*
* It is not prepared or intended for wider release.
*
* @author Connor Dodd <ctd681@newcastle.edu.au>
*/


function docReady(fn) {
    // see if DOM is already available
    if (document.readyState === "complete" || document.readyState === "interactive") {
        // call on next available tick
        setTimeout(fn, 1);
    } else {
        document.addEventListener("DOMContentLoaded", fn);
    }
}    

function frzDisplayTable(table, data, freezeColCount) {
	var container = table.parentElement;
	container.style.display = "none";

	var tableStarter = createTable(table.classList);

	var headerData = data[0];
	data.splice(0, 1);

	var defaultWidth = table.children[0].width;
	var colDefinitions = table.children[0].children;
	var widths = [];

	var next = colDefinitions[0];
	var count = 0, colRef = 0;
	while (widths.length < headerData.length) {
		var w = defaultWidth;

		if (next) {
			w = next.width;

			count++;
			if (!next.span || count >= parseInt(next.span)) {
				next = colDefinitions[++colRef];
				count = 0;
			}
		}

		widths.push(w);
	}
	var offset = 0;
	for (var i = 0; i < freezeColCount; i++)
		offset += parseInt(widths[i])
	offset += 'px';

	var template = table;
	var templateHTML = table.children[0].outerHTML;

	var headerFixed = tableStarter + "<thead><tr>",
		headerFree = tableStarter + "<thead><tr>";
	templateHTML += '<tr>';
	for (var c = 0; c < headerData.length; c++) {
		//Add to template
		templateHTML += '<th>' + headerData[c] + '</th>'

		//Add to actual
		if (c < freezeColCount)
			headerFixed += '<th style="max-width: ' + widths[c] + 'px; min-width: ' + widths[c] + 'px" >' + headerData[c] + "</th>"
		else
			headerFree += '<th style="max-width: ' + widths[c] + 'px; min-width: ' + widths[c] + 'px" >' + headerData[c] + "</th>"
	}
	headerFixed += "</tr></thead></table>";
	headerFree += "</tr></thead></table>";
	templateHTML += '</tr>';


	var bodyFixed = tableStarter + "<tbody>",
		bodyFree = tableStarter + "<tbody>";
	for (var r = 0; r < data.length; r++) {
		var row = data[r];
		bodyFixed += "<tr>";
		bodyFree += "<tr>";
		templateHTML += "<tr>"

		for (var c = 0; c < row.length; c++) {
			//Add to template
			templateHTML += '<td>' + row[c] + '</td>'

			//Add to actual
			if (c < freezeColCount)
				bodyFixed += '<td style="max-width: ' + widths[c] + 'px; min-width: ' + widths[c] + 'px" >' + row[c] + "</td>"
			else
				bodyFree += '<td style="max-width: ' + widths[c] + 'px; min-width: ' + widths[c] + 'px" >' + row[c] + "</td>"
		}

		bodyFixed += "</tr>";
		bodyFree += "</tr>";
		templateHTML += "</tr>"
	}
	bodyFixed += "</tbody></table>";
	bodyFree += "</tbody></table>";

	var tl = document.createElement('div');
	tl.innerHTML = headerFixed;
	tl.classList.add('frz-header-fixed');
	tl.style.width = offset;

	var tr = document.createElement('div');
	tr.innerHTML = headerFree;
	tr.classList.add('frz-header-free');
	tr.style.paddingLeft = offset;

	var headerDiv = document.createElement('div');
	headerDiv.classList.add('frz-header');
	headerDiv.appendChild(tl);
	headerDiv.appendChild(tr);

	var bl = document.createElement('div');
	bl.innerHTML = bodyFixed;
	bl.classList.add('frz-body-fixed');
	bl.style.width = offset;

	var br = document.createElement('div');
	br.innerHTML = bodyFree;
	br.classList.add('frz-body-free');
	br.style.paddingLeft = offset;
	br.onscroll = function() {
		bl.scrollTop = br.scrollTop;
		tr.scrollLeft = br.scrollLeft;
	}

	var bodyDiv = document.createElement('div');
	bodyDiv.classList.add('frz-body');
	bodyDiv.appendChild(bl);
	bodyDiv.appendChild(br);

	table.style.display = "none";
	table.innerHTML = templateHTML;

	container.textContent = '';
	container.appendChild(table);
	container.appendChild(headerDiv);
	container.appendChild(bodyDiv);

	docReady(function() {
		container.style.display = "block";
	})
}

function createTable(classList) {
	var table = '<table class="'
	for (var i = 0; i < classList.length; i++) {
		table += classList[i] + " ";
	}
	table += '">'

	return table;
}
