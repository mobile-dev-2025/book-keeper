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


//adds book data to the db
app.post("/Books", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Books");
      const result = await collection.insertOne({
          //subId: req.user.subId,
          bookName: req.body.bookName.trim(),
          totalPages: req.body.totalPages,
          currentPage: Math.max(0, 
              parseInt(req.body.currentPage || 0)
          ),
          pagesRead: req.body.pagesRead,
          actualEndDate: null, // To be updated when book is completed
          startDate: startDateObj,
          endDate: endDateObj,
          dailyReadHistory: [],            
          complete: isComplete,
          current: isCurrent,
          notes: req.body.notes?.trim() || '',
          progressHistory: [],
          hidden: false,
          meta: {
              createdAt: new Date(),
              updatedAt: new Date()
          }
      });

      res.json(result);
    } catch (error) {
      console.error("Failed to create book record:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });

 //gets book data from the db
 app.get("/Books/:subId", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Books");
      const result = await collection.find({ subId: req.params.subId }).toArray();
      res.json(result);
    } catch (error) {
      console.error("Error fetching books:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });

  
  //adds user data to the db
  app.post("/user", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Checkuser");
      const result = await collection.insertOne({
        subId: req.body.subId,
        createdAt: new Date(),
        updatedAt: new Date(),
      });
      res.json(result);
    } catch (error) {
      console.error("Failed to create user record:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });

   //gets user data from the db
   app.get("/user/:subId", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Checkuser");
      const result = await collection.findOne({ subId: req.params.subId });
      res.json(result);
    } catch (error) {
      console.error("Error checking user:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });
  
  //checks if user exists in the db
  app.get("/checkUser/:subId", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("Checkuser");
      const result = await collection.findOne({ subId: req.params.subId });
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