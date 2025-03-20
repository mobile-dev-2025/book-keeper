//For creating server in node.js using express 
const authRoutes = require('./routes/authRoutes');
const bookRoutes = require('./routes/bookRoutes');
const express = require('express');
const app = express();
const port = 8000;

require('dotenv').config();

app.use(express.json());
app.use(express.urlencoded({extended: true}));

//connecting to database
const { MongoClient } = require("mongodb");

if (!process.env.MONGODB_URI) {
  throw new Error("MONGODB_URI is not defined");
}

const client = new MongoClient(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  ssl: true, // Make sure SSL is enabled
  tls: true, // Force TLS connection
  tlsAllowInvalidCertificates: false, // Valid certificates must be used
  useUnifiedTopology: true,
});

const clientPromise = client.connect();
clientPromise.then(() => {
  console.log("Connected to MongoDB");
}).catch((error) => {
  console.error("Error connecting to MongoDB:", error);
});

// Route mounting
app.use('/auth', authRoutes);
app.use('/books', bookRoutes); 

//Basic Hello world to check if server is running
app.get('/', (req, res) =>{
    res.send('Hello World');
});



app.get("/Books", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Books");
      const result = await collection.insertOne({
        subId,
        bookNo: 1,
        bookTitle: req.body.bookTitle,
        totalPages: req.body.totalPages,
        startDate: new Date(req.body.startDate),
        endDate: req.body.endDate ? new Date(req.body.endDate) : null,
        actualEndDate: req.body.endDate ? new Date(req.body.endDate) : null,
        currentPage: req.body.currentPage || 1,
        pagesPerDay: req.body.pagesPerDay || 0,
        dailyReadHistory: [],
        bookComplete: false,
        currentBook: true,
        createdAt: new Date(),
        updatedAt: new Date(),
        hidden: false
      });
      res.json(result);
    } catch (error) {
      console.error("Failed to create book record:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });
  
  //User creation 
  
  app.get("/checkUser", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Checkuser");
      const result = await collection.insertOne({
        subId,
      });
      res.json(result);
    } catch (error) {
      console.error("Error checking user:", error);
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