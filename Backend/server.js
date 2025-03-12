//For creating server in node.js using express 
const express = require('express');
const app = express();
const port = 8000;

app.use(express.json());
app.use(express.urlencoded({extended: true}));

//Basic Hello world
app.get('/', (req, res) =>{
    res.send('Hello World');
});

//To view users current book
app.get('/CurrentBook', (req, res) => {
    res.send('Current Book');
});

// to add a new book
app.post('/AddBook', (req, res) => {
    res.send('Book Added');
})

//To view users book history
app.get('/History', (req, res) => {
    res.send('Book History');
});

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});