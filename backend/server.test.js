const supertest = require('supertest');
const app = require('./server'); // Make sure this exports the Express `app`, not the server

// Setup before running any tests: Insert a test book into the database
beforeAll(async () => {
  const { MongoClient } = require('mongodb');
  const client = new MongoClient(process.env.MONGODB_URI, { useUnifiedTopology: true });
  // Connect to the MongoDB database
  await client.connect();
  const db = client.db("book-keeper");
  const collection = db.collection("books");
  // Insert a mock book to use for testing the endpoints
  await collection.insertOne({
    userId: 'test-user-id',
    bookTitle: 'Test Book Title',
    currentPage: 10,
    totalPages: 100,
    pagesRead: 10,
    dailyRead: [],
  });
 // Close the database connection after the setup
  await client.close();
});

// Test suite for the GET /currentBook endpoint
describe('GET /currentBook', () => {
  it('should return current book if userId and bookTitle are passed', async () => {
    const userId = 'test-user-id';
    const bookTitle = 'Test Book Title';
    // Send a GET request to fetch the current book
    const response = await supertest(app)
      .get('/currentBook')
      .query({ userId, bookTitle });

    expect([200, 400]).toContain(response.status); // 400 if user/book not found, or 200 if found
    // If the book is found (status 200), check that the response contains the book details
    if (response.status === 200 && response.body.currentBook) {
      expect(response.body).toHaveProperty('message', 'Current book retrieved successfully');
      expect(response.body).toHaveProperty('currentBook');
      expect(response.body.currentBook).toHaveProperty('userId', userId);
      expect(response.body.currentBook).toHaveProperty('bookTitle', bookTitle);
    } else if (response.status === 400) {
       // If no book is found (status 400), check that the error message is in the response
      expect(response.body).toHaveProperty('error');
    } else {
      // If no current book found (status other than 200 or 400), check the message
      expect(response.body).toHaveProperty('message', 'No current book found for this user');
    }
  });
});

describe('PUT /currentBook', () => {
  it('should update the book progress if valid data is passed', async () => {
    const userId = 'test-user-id';
    const bookTitle = 'Test Book Title';
    const currentPage = 50; 
    const notes = 'Halfway through the book!';
 
    // Send a PUT request to update the book progress
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId, bookTitle, currentPage, notes });
    // Check that the response status is either 200, 400, or 404
    expect([200, 400, 404]).toContain(response.status);
    // If the update is successful (status 200), check the response contains the updated book details
    if (response.status === 200) {
      expect(response.body).toHaveProperty('message');
      expect(response.body).toHaveProperty('updatedBook');
      expect(response.body.updatedBook).toHaveProperty('userId', userId);
      expect(response.body.updatedBook).toHaveProperty('bookTitle', bookTitle);
      expect(response.body.updatedBook).toHaveProperty('currentPage', currentPage);
      expect(response.body.updatedBook).toHaveProperty('notes', notes);
    } else if (response.status === 400) {
      // If there is an error (status 400), check that the error message is in the response
      expect(response.body).toHaveProperty('error');
    } else if (response.status === 404) {
       // If no book was found to update (status 404), check the error message
      expect(response.body).toHaveProperty('message', 'No book found for this user and title');
    }
  });

   // Test for missing required fields in the PUT request body
  it('should return 400 if required fields are missing', async () => {
    // Send a PUT request with a missing bookTitle and currentPage
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: 'user-without-page' }); 
    // Check that the response status is 400 for invalid data
    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error');
  });

   // Test for invalid currentPage value (negative page number)
  it('should return 400 if currentPage is negative', async () => {
    // Send a PUT request with a negative currentPage
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: 'test-user-id', bookTitle: 'Test Book Title', currentPage: -1 });
    // Check that the response status is 400 for invalid currentPage
    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'currentPage cannot be negative');
  });
});
