//For creating server in node.js using express 
const express = require('express');
const app = express();
const port = 8000;

//Basic Hello world
app.get('/', function(req, res){
    res.send('Hello World');
});
app.get('/backend', function(req, res){
    res.send('Mobile_dev_backend');
});

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});