![logo](../images/header_icon.png "VISIDA")  


Files related to the web client are included in visida_cms. The client is developed in Angular.js, and requires Node.js to be installed.

## Installation
Install Node.js. Angular.js documentation provides a guide on this. https://docs.angularjs.org/tutorial 

Open a terminal in the visida_cms folder and use the Node.js package manager to install the required packages.
``` bash
npm install
```

The client must be served from a HTTP web server. For development purposes, the http-server package works well. Install it with:
``` bash
npm install --global http-server
```

You can then serve the client with:
``` bash
http-server -o
```

## Config
The client must be set up to point at the API. Config settings can be found in /package.json. Edit baseUrl inside the configSettings constant to the address of your API, retaining the /api suffix.
``` JavaScript
	app.constant('configSettings', {
	    'baseUrl': 'http://localhost:39548/api'
	});
```
 
