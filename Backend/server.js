//For creating server in node.js using express 
const authRoutes = require('./routes/authRoutes');
const bookRoutes = require('./routes/bookRoutes');
const express = require('express');
const mongoose = require('mongoose');
const app = express();
const port = 8000;

require('dotenv').config();

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

app.get("/TestG", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("checkingbooks");
      const result = await collection.insertOne({
        owner: "subId:34345345",
        bookNo: 1,
        title: "Book Title",
        pageAmount: 255,
        startDate: "25 march",
        endDate: "25 april",
        actualEndDate: "calculated",
        currentPage: 1,
        pagesPerDay: 20,
        actualPagesPerDay: 25,
        dailyReadHistory: "test",
        bookComplete: false,
        currentBook: true,
        hidden: false,
      });
      res.json(result);
    } catch (error) {
      console.error("Error adding test data:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });
  
  //User creation 
  
  app.get("/checkUsers", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("checkusers");
      const result = await collection.insertOne({
        userId: req.userId,
      });
      res.json(result);
    } catch (error) {
      console.error("Error adding test data:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});