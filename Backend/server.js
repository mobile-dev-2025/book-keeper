//For creating server in node.js using express 
const express = require('express');
const app = express();
const port = 8000;

app.use(express.json());
app.use(express.urlencoded({extended: true}));

//Routes
app.use('/auth', require('./routes/authRoutes'));
app.use('/books', require('./routes/bookRoutes'));

//Basic Hello world to check if server is running
app.get('/', (req, res) =>{
    res.send('Hello World');
});

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});