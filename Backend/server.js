//For creating server in node.js using express 
const express = require("express");
const app = express();
const port = 8000;

require("dotenv").config();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Connecting to database
const { MongoClient } = require("mongodb");

if (!process.env.MONGODB_URI) {
  throw new Error("MONGODB_URI is not defined");
}

const client = new MongoClient(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  tls: true, // Force TLS connection
  tlsAllowInvalidCertificates: false, // Valid certificates must be used
});

const clientPromise = client.connect();
clientPromise
  .then(() => {
    console.log("Connected to MongoDB");
  })
  .catch((error) => {
    console.error("Error connecting to MongoDB:", error);
  });


app.post("/checkUser", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("users");

    // Extract userId from request body
    const { userId } = req.body;

    if (!userId) {
      return res.status(400).json({ error: "userId is required" });
    }

    // Check if the user already exists
    let user = await collection.findOne({ userId });
    let isNewUser = false;

    if (!user) {
      // Create new user if not found
      const newUser = {
        userId,
        createdAt: new Date(),
        lastLogin: new Date(),
      };

      await collection.insertOne(newUser);
      user = newUser;
      isNewUser = true;
    } else {
      // Update last login timestamp
      await collection.updateOne(
        { userId },
        { $set: { lastLogin: new Date() } }
      );
    }

    res.json({
      isNewUser,
      userId: user.userId,
    });
  } catch (error) {
    console.error("Error processing user:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

app.post("/addBook", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("books");

    // Extract book details from request body
    const {  bookTitle, totalPages, userId, pagesRead, startDate, endDate, notes } =
      req.body;

    // Validate required fields
    if (!bookTitle || !totalPages || !userId) {
      return res
        .status(400)
        .json({ error: "title, totalPages, and userId are required" });
    }

    // Check for duplicate books for the same user (optional)
    const existingBook = await collection.findOne({ bookTitle, userId });
    if (existingBook) {
      return res
        .status(400)
        .json({ error: "Book with this title already exists for this user" });
    }

    // Create new book
    const newBook = {
      bookTitle,
      totalPages,
      userId,
      pagesRead: pagesRead || 0,
      startDate: startDate ? new Date(startDate) : new Date(),
      endDate: endDate ? new Date(endDate) : null,
      notes: notes || ""
    
    };

    await collection.insertOne(newBook);

    res.json({
      message: "Book added successfully",
      book: newBook,
    });
  } catch (error) {
    console.error("Error processing book:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

app.get("/history", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("books");

    // Extract userId from query parameters
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({ error: "userId is required" });
    }

    // Fetch all books read by the user
    const books = await collection.find({ userId }).toArray();

    res.json({
      message: "User reading history retrieved successfully",
      books,
    });
  } catch (error) {
    console.error("Error fetching user history:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});


// Listening to port
app.listen(port, (err) => {
  if (err) {
    console.log(`Error in running the server: ${err}`);
  } else {
    console.log(`Server is running on port: ${port}`);
  }
});
