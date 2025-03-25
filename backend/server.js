//For creating server in node.js using express 
const express = require("express");
const app = express();
const port = 8000;

// Load environment variables from .env file
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
      notes: notes || "",
       
    
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

// Fetching history of books read by the user from database
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

// Fetching the current book being read by the user
app.get("/currentBook", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("books");

    // Extract userId from query parameters
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({ error: "userId is required" });
    }

    // Find the most recent book being read (with pagesRead < totalPages)
    const currentBook = await collection.findOne(
      {
        userId,
        $expr: { $lt: ["$pagesRead", "$totalPages"] }, // To check if pagesRead < totalPages
      },
      { sort: { startDate: -1 } }
    );

    if (!currentBook) {
      return res.json({ message: "No current book found for this user" });
    }

    res.json({
      message: "Current book retrieved successfully",
      currentBook,
    });
    } catch (error) {
      console.error("Error fetching current book:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
  });

// Updating the book details
app.put("/currentBook", async (req, res) => { 
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("books");

    const { userId, pagesRead, notes } = req.body;
    
    if (!userId || pagesRead === undefined) {
      return res.status(400).json({ error: "userId and pagesRead are required" });
    }

    // Find the most recent book being read
    const currentBook = await collection.findOne(
      {
        userId,
        $expr: { $lt: ["$pagesRead", "$totalPages"] },
      },
      { sort: { startDate: -1 } }
    );

    if (!currentBook) {
      return res.status(404).json({ message: "No current book found for this user" });
    }

    const updateFields = { pagesRead };
    if (notes) updateFields.notes = notes;

    let responseMessage = "Current book updated successfully";

    // If pagesRead reaches totalPages, mark the book as completed
    if (pagesRead >= currentBook.totalPages) {
      updateFields.pagesRead = currentBook.totalPages; // Ensure it doesn't exceed totalPages
      updateFields.endDate = new Date();
      responseMessage = "Book completed";
    }

    await collection.updateOne(
      { _id: currentBook._id },
      { $set: updateFields }
    );

    res.json({ message: responseMessage, updatedBook: { ...currentBook, ...updateFields } });
  } catch (error) {
    console.error("Error updating current book:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});
// Fetching all reading plans from the database
app.get("/readingPlans", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("reading-plans");

    // Fetch all reading plans from the database
    const readingPlans = await collection.find({}).toArray();

    res.json({
      message: "Reading plans retrieved successfully",
      readingPlans,
    });
  } catch (error) {
    console.error("Error fetching reading plans:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

// Creating a new reading plan
app.post("/readingPlans", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");

    const booksCollection = db.collection("books");
    const readingPlansCollection = db.collection("reading-plans");

    // Extract the reading plan details from request body
    const { userId, bookTitle, estimatedDays } = req.body;

    // Validate required fields
    if (!userId || !bookTitle || !estimatedDays) {
      return res.status(400).json({ error: "userId, bookTitle, and estimatedDays are required" });
    }

    // Find the book in the books collection
    const book = await booksCollection.findOne({ bookTitle, userId });

    if (!book) {
      return res.status(404).json({ error: "Book not found for this user" });
    }

    // Calculate pages per day based on estimatedDays
    const pagesPerDay = Math.max(Math.floor(book.totalPages / estimatedDays), 1);

    // Calculate end date if not provided
    const endDate = new Date(book.startDate.getTime() + estimatedDays * 24 * 60 * 60 * 1000);

    // Create a new reading plan
    const newReadingPlan = {
      bookTitle,
      userId,
      totalPages: book.totalPages,
      pagesRead: book.pagesRead || 0,
      pagesPerDay,
      startDate: book.startDate,
      endDate,
      estimatedDays,
      createdAt: new Date(),
    };

    // Insert the new reading plan into the reading-plans collection
    await readingPlansCollection.insertOne(newReadingPlan);

    // Return success response
    res.json({
      message: "Reading plan created successfully",
      readingPlan: newReadingPlan,
    });
  } catch (error) {
    console.error("Error creating reading plan:", error);
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