
function ajax_str(callback) {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			callback(xhttp.responseText);
		}
	}
	xhttp.open("POST", "./greeting", true);
	xhttp.send();
}

function update_td() {
	ajax_str(function (response) {
		document.querySelector("table.hits-table td").innerText = response;	
	});
}

window.onload = function() {
	window.setTimeout(update_td, 1000);
};

