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
    const { bookTitle, totalPages, userId, pagesRead, startDate, endDate, notes } = req.body;

    // Validate required fields
    if (!bookTitle || !totalPages || !userId) {
      return res
        .status(400)
        .json({ error: "title, totalPages, and userId are required" });
    }

    // Check for duplicate books for the same user
    const existingBook = await collection.findOne({ bookTitle, userId });
    if (existingBook) {
      console.log("Book already exists, updating instead:", existingBook);
      
      // Update the existing book instead of creating a new one
      const updatedBook = {
        ...existingBook,
        totalPages: totalPages,
        pagesRead: pagesRead || existingBook.pagesRead || 0,
        currentPage: pagesRead || existingBook.currentPage || 0,
        startDate: startDate ? new Date(startDate) : existingBook.startDate,
        endDate: endDate ? new Date(endDate) : existingBook.endDate,
        notes: notes || existingBook.notes || "",
        lastUpdated: new Date()
      };
      
      await collection.updateOne(
        { _id: existingBook._id },
        { $set: updatedBook }
      );
      
      console.log("Book updated successfully:", updatedBook);
      
      return res.json({
        message: "Book updated successfully",
        book: updatedBook,
      });
    }

    // Create new book
    const newBook = {
      bookTitle,
      totalPages,
      userId,
      pagesRead: pagesRead || 0, // Ensure pagesRead defaults to 0 if not provided
      currentPage: pagesRead || 0, // Set currentPage equal to pagesRead
      startDate: startDate ? new Date(startDate) : new Date(),
      endDate: endDate ? new Date(endDate) : null,
      notes: notes || "",
      lastUpdated: new Date(),
      createdAt: new Date()
    };

    const result = await collection.insertOne(newBook);
    newBook._id = result.insertedId;
    
    console.log("New book created:", newBook);

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

    // Extract userId and bookTitle from query parameters
    const { userId, bookTitle } = req.query;

    if (!userId || !bookTitle) {
      return res.status(400).json({ error: "userId and bookTitle is required" });
    }

    // Find the most recent book being read (with pagesRead < totalPages)
    const currentBook = await collection.findOne(
      {
        userId,
        bookTitle,
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

app.put("/currentBook", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("books");
  
      const { userId, bookTitle, currentPage, notes } = req.body;
  
      if (!userId || !bookTitle || currentPage === undefined) {
        return res.status(400).json({ error: "userId, bookTitle, and currentPage are required" });
      }
  
      // Find the current book being read
      const currentBook = await collection.findOne({ userId, bookTitle });
  
      if (!currentBook) {
        console.log("Book not found for update:", { userId, bookTitle });
        return res.status(404).json({ message: "No book found for this user and title" });
      }
  
      console.log("Found book for update:", currentBook);
  
      // Ensure currentPage is valid
      if (currentPage < 0) {
        return res.status(400).json({ error: "currentPage cannot be negative" });
      }
      if (currentPage > currentBook.totalPages) {
        return res.status(400).json({ error: "currentPage cannot exceed totalPages" });
      }
  
      // Track previous last page read
      const lastPageRead = currentBook.currentPage;
  
      // Calculate pages read today
      const pagesReadToday = currentPage - lastPageRead;
      const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD format
  
      // Initialize or get dailyRead array
      let dailyRead = currentBook.dailyRead || [];
  
      if (pagesReadToday > 0) {
        // Check if today's date already exists in dailyRead
        const existingEntryIndex = dailyRead.findIndex(entry => entry.date === today);
  
        if (existingEntryIndex !== -1) {
          // If today's entry exists, update page count
          dailyRead[existingEntryIndex].page += pagesReadToday;
        } else {
          // If today's entry doesn't exist, add a new one
          dailyRead.push({ date: today, page: pagesReadToday });
        }
      }
  
      // Update book progress
      const updateFields = {
        pagesRead: currentPage, // Track total pages read
        currentPage,
        lastUpdated: new Date().toISOString(),
        lastPageRead,
        dailyRead
      };
  
      if (notes !== undefined) {
        updateFields.notes = notes;
      }
  
      // If book is completed, set endDate
      if (currentPage === currentBook.totalPages) {
        updateFields.endDate = new Date().toISOString();
      }
  
      console.log("Updating book with fields:", updateFields);
  
      // Update the book document
      await collection.updateOne(
        { _id: currentBook._id },
        { $set: updateFields }
      );
  
      // Get the updated book to return in response
      const updatedBook = await collection.findOne({ _id: currentBook._id });
  
      console.log("Book updated successfully:", updatedBook);
  
      // Return the updated book details
      res.json({
        message: currentPage === currentBook.totalPages ? "Book completed" : "Book progress updated successfully",
        updatedBook
      });
    } catch (error) {
      console.error("Error updating current book:", error);
      res.status(500).json({ error: "Internal Server Error" });
    }
});
 
  
    
app.get("/readingPlans", async (req, res) => {
    try {
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("reading-plans");
  
      // Extract userId and optional bookTitle from query parameters
      const { userId, bookTitle } = req.query;
  
      if (!userId) {
        return res.status(400).json({ error: "userId is required" });
      }
  
      // Create query object to filter reading plans
      const query = { userId };
      if (bookTitle) {
        query.bookTitle = bookTitle;
      }
  
      // Fetch reading plans based on userId and optional bookTitle
      const readingPlans = await collection.find(query).toArray();
  
      if (readingPlans.length === 0) {
        return res.json({
          message: bookTitle ? `No reading plans found for book "${bookTitle}"` : "No reading plans found for this user",
          readingPlans: []
        });
      }
  
      res.json({
        message: bookTitle ? `Reading plans for "${bookTitle}" retrieved successfully` : "Reading plans retrieved successfully",
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
    const { userId, bookTitle, pagesPerDay } = req.body;

    // Validate required fields
    if (!userId || !bookTitle || !pagesPerDay) {
      return res.status(400).json({ error: "userId, bookTitle, and pagesPerDay are required" });
    }

    // Find the book in the books collection
    const book = await booksCollection.findOne({ bookTitle, userId });

    if (!book) {
      return res.status(404).json({ error: "Book not found for this user" });
    }

     console.log("Found book for reading plan:", book);

    // Check if a reading plan already exists
    const existingPlan = await readingPlansCollection.findOne({ bookTitle, userId });
    if (existingPlan) {
      console.log("Updating existing reading plan:", existingPlan);
      
      // Calculate the pages remaining
      const pagesRemaining = book.totalPages - book.pagesRead;

      // Dynamically calculate the estimated days to finish the book
      let estimatedDays = Math.ceil(pagesRemaining / pagesPerDay); // rounding up to the nearest whole number

      // Calculate end date based on estimatedDays
      const endDate = new Date();
      endDate.setDate(endDate.getDate() + estimatedDays);
      
      // Update fields
      const updateFields = {
        pagesPerDay,
        pagesRead: book.pagesRead,
        totalPages: book.totalPages,
        estimatedDays,
        endDate,
        updatedAt: new Date()
      };
      
      // Update the reading plan
      await readingPlansCollection.updateOne(
        { _id: existingPlan._id },
        { $set: updateFields }
      );
      
      // Get the updated plan
      const updatedPlan = await readingPlansCollection.findOne({ _id: existingPlan._id });
      
      console.log("Reading plan updated:", updatedPlan);
      
      return res.json({
        message: "Reading plan updated successfully",
        readingPlan: updatedPlan
      });
    }

    // Calculate the pages remaining
    const pagesRemaining = book.totalPages - book.pagesRead;

    // Dynamically calculate the estimated days to finish the book
    let estimatedDays = Math.ceil(pagesRemaining / pagesPerDay); // rounding up to the nearest whole number

    // Calculate end date based on estimatedDays
    const endDate = new Date();
    endDate.setDate(endDate.getDate() + estimatedDays);

    // Create a new reading plan
    const newReadingPlan = {
      bookTitle,
      userId,
      totalPages: book.totalPages,
      pagesRead: book.pagesRead || 0,
      pagesPerDay,
      startDate: new Date(),
      endDate,
      estimatedDays,
      createdAt: new Date(),
    };

    // Insert the new reading plan into the reading-plans collection
    const result = await readingPlansCollection.insertOne(newReadingPlan);
    newReadingPlan._id = result.insertedId;
    
    console.log("New reading plan created:", newReadingPlan);

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

// Fetch the last read book and last read page from the user's reading history
app.get("/lastRead", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("books");

    // Extract userId and bookTitle from query parameters
    const { userId, bookTitle } = req.query;

    if (!userId || !bookTitle) {
      return res.status(400).json({ error: "userId and bookTitle are required" });
    }

    // Fetch the user's reading history sorted by last updated for the same book
    const lastReadBook = await collection.findOne(
      { userId, bookTitle },
      { sort: { lastUpdated: -1 } }
    );

    if (!lastReadBook) {
      return res.status(404).json({ message: "No reading history found for this book and user" });
    }

    res.json({
      message: "Last read book retrieved successfully",
      bookTitle: lastReadBook.bookTitle,
      lastPageRead: lastReadBook.lastPageRead
    });
  } catch (error) {
    console.error("Error fetching last read book:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

app.get("/readingStats", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");

    const { userId, bookTitle } = req.query;

    if (!userId || !bookTitle) {
      return res.status(400).json({ error: "userId and bookTitle are required" });
    }

    const booksCollection = db.collection("books");
    const plansCollection = db.collection("reading-plans");

    const book = await booksCollection.findOne({ userId, bookTitle });
    const plan = await plansCollection.findOne({ userId, bookTitle });

    if (!book || !plan) {
      return res.status(404).json({ error: "Book or reading plan not found" });
    }
    const pagesPerDay = plan.pagesPerDay;
    const dailyRead = book.dailyRead || [];

   // Sort the reading data by date
    dailyRead.sort((a, b) => new Date(a.date) - new Date(b.date));

      
       let cumulativeActual = 0;
       const stats = dailyRead.map((entry, index) => {
         cumulativeActual += entry.page; // total read so far
         const cumulativePlan = pagesPerDay * (index + 1); // expected total so far
         return {
           date: entry.date,
           plan: cumulativePlan,
           actual: cumulativeActual,
           bonus: cumulativeActual - cumulativePlan
         };
       });
   
    res.json({
      message: "Reading stats retrieved successfully",
      stats
    });

  } catch (error) {
    console.error("Error fetching reading stats:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});


//marking the book as completed
app.post('/finishedBook', async (req, res) => {
  try {
      const { bookTitle, userId } = req.body;

      // Get the database connection
      const clientConnection = await clientPromise;
      const db = clientConnection.db("book-keeper");
      const collection = db.collection("books");

      // Check if the book exists in the database
      const book = await collection.findOne({ bookTitle, userId });

      if (!book) {
          return res.status(404).json({ error: "Book not found" });
      }

      // Mark the book as completed
      const updateFields = {
          pagesRead: book.totalPages,
          currentPage: book.totalPages,
          endDate: new Date()
      };

      await collection.updateOne(
          { bookTitle, userId },
          { $set: updateFields }
      );

      res.status(200).json({
          message: "Book marked as completed successfully",
          book: { ...book, ...updateFields } // Return updated book
      });
  } catch (error) {
      console.error("Error marking book as finished:", error);
      res.status(500).json({ error: "An error occurred while marking the book as finished" });
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