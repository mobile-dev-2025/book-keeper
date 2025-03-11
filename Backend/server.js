//For creating server in node.js using express 
const express = require('express');
const app = express();
const port = 8000;

//for Hello world
app.get('/', function(req, res){
    res.send('Hello World');
});

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});