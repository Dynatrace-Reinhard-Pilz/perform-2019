function checkHealth() {
	var xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function() {
		if (this.readyState == 4) {
			if (this.status == 200) {
				var health = document.getElementById("health");
				if (xhr.responseText == "true") {
					health.innerHTML = "healthy";
					health.setAttribute("class", "healthy");
				} else {
					health.innerHTML = "unhealthy";
					health.setAttribute("class", "unhealthy");
				}
			}
			window.setTimeout(checkHealth, 2000);
		}
	};
	xhr.open("GET", "/status", true);
	xhr.send();	
}

window.onload = function() {
	window.setTimeout(checkHealth, 1);
};