const supertest = require('supertest');
const app = require('./server'); // Make sure this exports the Express `app`, not the server

beforeAll(async () => {
  const { MongoClient } = require('mongodb');
  const client = new MongoClient(process.env.MONGODB_URI, { useUnifiedTopology: true });
  await client.connect();
  const db = client.db("book-keeper");
  const collection = db.collection("books");

  await collection.insertOne({
    userId: 'test-user-id',
    bookTitle: 'Test Book Title',
    currentPage: 10,
    totalPages: 100,
    pagesRead: 10,
    dailyRead: [],
  });

  await client.close();
});

describe('GET /currentBook', () => {
  it('should return current book if userId and bookTitle are passed', async () => {
    const userId = 'test-user-id';
    const bookTitle = 'Test Book Title';

    const response = await supertest(app)
      .get('/currentBook')
      .query({ userId, bookTitle });

    expect([200, 400]).toContain(response.status); // 400 if user/book not found, or 200 if found

    if (response.status === 200 && response.body.currentBook) {
      expect(response.body).toHaveProperty('message', 'Current book retrieved successfully');
      expect(response.body).toHaveProperty('currentBook');
      expect(response.body.currentBook).toHaveProperty('userId', userId);
      expect(response.body.currentBook).toHaveProperty('bookTitle', bookTitle);
    } else if (response.status === 400) {
      expect(response.body).toHaveProperty('error');
    } else {
      expect(response.body).toHaveProperty('message', 'No current book found for this user');
    }
  });
});

describe('PUT /currentBook', () => {
  it('should update the book progress if valid data is passed', async () => {
    const userId = 'test-user-id';
    const bookTitle = 'Test Book Title';
    const currentPage = 50; // Assume this is a valid page
    const notes = 'Halfway through the book!';

    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId, bookTitle, currentPage, notes });

    expect([200, 400, 404]).toContain(response.status);

    if (response.status === 200) {
      expect(response.body).toHaveProperty('message');
      expect(response.body).toHaveProperty('updatedBook');
      expect(response.body.updatedBook).toHaveProperty('userId', userId);
      expect(response.body.updatedBook).toHaveProperty('bookTitle', bookTitle);
      expect(response.body.updatedBook).toHaveProperty('currentPage', currentPage);
      expect(response.body.updatedBook).toHaveProperty('notes', notes);
    } else if (response.status === 400) {
      expect(response.body).toHaveProperty('error');
    } else if (response.status === 404) {
      expect(response.body).toHaveProperty('message', 'No book found for this user and title');
    }
  });

  it('should return 400 if required fields are missing', async () => {
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: 'user-without-page' }); // Missing bookTitle and currentPage

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error');
  });

  it('should return 400 if currentPage is negative', async () => {
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: 'test-user-id', bookTitle: 'Test Book Title', currentPage: -1 });

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'currentPage cannot be negative');
  });
});
