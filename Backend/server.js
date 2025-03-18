//For creating server in node.js using express 
const authRoutes = require('./routes/authRoutes');
const bookRoutes = require('./routes/bookRoutes');
const express = require('express');
const mongoose = require('mongoose');
const app = express();
const port = 8000;

app.use(express.json());
app.use(express.urlencoded({extended: true}));

//connecting to database
mongoose.connect(process.env.MONGODB_URI)
  .then(() => console.log('MongoDB connected'))
  .catch(err => console.error('DB connection error:', err));

  //Routes
// Route mounting
app.use('/auth', authRoutes);
app.use('/books', bookRoutes);

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