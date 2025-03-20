//For creating server in node.js using express
const express = require("express");
const app = express();
const port = 8000;
require("dotenv").config();

//MongoDB
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

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

//adds test data to the database
app.get("/checkUsers", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("users");
    const result = await collection.insertOne({
      userId: req.userId,
    });
    res.json(result);
  } catch (error) {
    console.error("Error adding test data:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

//gets test data from db
app.get("/Test2", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("users");
    const result = await collection.find().toArray();
    res.json(result);
  } catch (error) {
    console.error("Error retrieving test data:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

//deletes test data from db
app.get("/Test3", async (req, res) => {
  try {
    const clientConnection = await clientPromise;
    const db = clientConnection.db("book-keeper");
    const collection = db.collection("test");
    const result = await collection.deleteMany({});
    res.json(result);
  } catch (error) {
    console.error("Error deleting test data:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

//Basic Hello world to check if server is running
app.get("/", (req, res) => {
  res.send("Hello World");
});

//hardcoded books for testing endpoints
let books = [
  {
    id: 1,
    bookName: "Book1",
    pagesRead: 10,
    startDate: "2024-03-01",
    endDate: "2024-03-02",
    notes: "This is a note",
  },
  {
    id: 2,
    bookName: "Book2",
    pagesRead: 20,
    startDate: "2024-03-02",
    endDate: "2024-03-03",
    notes: "This is a testing note",
  },
  {
    id: 3,
    bookName: "Book3",
    pagesRead: 30,
    startDate: "2024-03-03",
    endDate: "2024-03-04",
    notes: "This is another testing note",
  },
];

//To view users current book
app.get("/currentBook", (req, res) => {
  try {
    if (!books) {
      return res.status(404).json({ error: "No book found" });
    }
    res.status(200).json(books);
  } catch (error) {
    console.error("Error retrieving books:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

//To view users current book
app.get("/currentBook/:id", (req, res) => {
  const bookId = parseInt(req.params.id);
  const currentBook = books.find((currentBook) => currentBook.id === bookId);
  try {
    if (!currentBook) {
      return res.status(404).json({ error: "No book found" });
    }
    res.status(200).json(currentBook);
  } catch (error) {
    console.error("Error retrieving books:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

// to add a new book
app.post("/addBook", (req, res) => {
  try {
    const { id, bookName, pagesRead, startDate, endDate, notes } = req.body;
    console.log(bookName, pagesRead, startDate, endDate, notes);

    if (!bookName || !pagesRead || !startDate || !endDate) {
      return res.status(400).json({ error: "Missing required fields" });
    }
    const newBook = {
      id: books.length + 1,
      bookName: bookName,
      pagesRead: parseInt(pagesRead),
      startDate: startDate,
      endDate: endDate,
      notes: notes,
    };

    books.push(newBook);
    res.status(201).json({ newBook });
  } catch (error) {
    console.error("Error adding book:", error);
    res.status(500).json({ error: "Internal Server Error" });
  }
});

//To view users book history
app.get("/history", (req, res) => {
  res.send("Book History");
});

//Listening to port
app.listen(port, function (err) {
  if (err) {
    console.log(`Error in running the server: ${err}`);
  }
  console.log(`Server is running on port: ${port}`);
});
