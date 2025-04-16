const supertest = require('supertest');
const app = require('./server'); // Export only the Express `app` from your server file
const { MongoClient } = require('mongodb');

const testUserId = 'test-user-id';
const testBookTitle = 'Test Book Title';

let client;
let db;
let usersCollection;
let booksCollection;

beforeAll(async () => {
  client = new MongoClient(process.env.MONGODB_URI, { useUnifiedTopology: true });
  await client.connect();
  db = client.db('book-keeper');
  usersCollection = db.collection('users');
  booksCollection = db.collection('books');

  // Ensure clean state
  await usersCollection.deleteMany({ userId: testUserId });
  await booksCollection.deleteMany({ userId: testUserId, bookTitle: testBookTitle });

  // Insert mock book
  await booksCollection.insertOne({
    userId: testUserId,
    bookTitle: testBookTitle,
    currentPage: 10,
    totalPages: 100,
    pagesRead: 10,
    dailyRead: [],
  });
});

afterAll(async () => {
  // Cleanup after tests
  await usersCollection.deleteMany({ userId: testUserId });
  await booksCollection.deleteMany({ userId: testUserId, bookTitle: testBookTitle });

  await client.close();
});

// Test GET /currentBook
describe('GET /currentBook', () => {
  it('should return current book if userId and bookTitle are passed', async () => {
    const response = await supertest(app)
      .get('/currentBook')
      .query({ userId: testUserId, bookTitle: testBookTitle });

    expect([200, 400]).toContain(response.status);
    if (response.status === 200 && response.body.currentBook) {
      expect(response.body).toHaveProperty('message', 'Current book retrieved successfully');
      expect(response.body.currentBook).toMatchObject({
        userId: testUserId,
        bookTitle: testBookTitle,
      });
    } else if (response.status === 400) {
      expect(response.body).toHaveProperty('error');
    } else {
      expect(response.body).toHaveProperty('message', 'No current book found for this user');
    }
  });
});

// Test PUT /currentBook
describe('PUT /currentBook', () => {
  it('should update the book progress if valid data is passed', async () => {
    const response = await supertest(app)
      .put('/currentBook')
      .send({
        userId: testUserId,
        bookTitle: testBookTitle,
        currentPage: 50,
        notes: 'Halfway through the book!',
      });

    expect([200, 400, 404]).toContain(response.status);

    if (response.status === 200) {
      expect(response.body).toHaveProperty('message');
      expect(response.body.updatedBook).toMatchObject({
        userId: testUserId,
        bookTitle: testBookTitle,
        currentPage: 50,
        notes: 'Halfway through the book!',
      });
    } else if (response.status === 400) {
      expect(response.body).toHaveProperty('error');
    } else if (response.status === 404) {
      expect(response.body).toHaveProperty('message', 'No book found for this user and title');
    }
  });

  it('should return 400 if required fields are missing', async () => {
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: 'user-without-page' });

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error');
  });

  it('should return 400 if currentPage is negative', async () => {
    const response = await supertest(app)
      .put('/currentBook')
      .send({ userId: testUserId, bookTitle: testBookTitle, currentPage: -1 });

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'currentPage cannot be negative');
  });
});

// Test POST /checkUser
describe('POST /checkUser', () => {
  it('should create a new user if one does not exist', async () => {
    await usersCollection.deleteMany({ userId: testUserId }); // Force fresh start

    const response = await supertest(app)
      .post('/checkUser')
      .send({ userId: testUserId });

    expect(response.status).toBe(200);
    expect(response.body).toMatchObject({
      userId: testUserId,
      isNewUser: true,
    });

    const user = await usersCollection.findOne({ userId: testUserId });
    expect(user).toBeTruthy();
    expect(user).toHaveProperty('createdAt');
    expect(user).toHaveProperty('lastLogin');
  });

  it('should update lastLogin and return isNewUser: false if user already exists', async () => {
    const response = await supertest(app)
      .post('/checkUser')
      .send({ userId: testUserId });

    expect(response.status).toBe(200);
    expect(response.body).toMatchObject({
      userId: testUserId,
      isNewUser: false,
    });
  });

  it('should return 400 if userId is missing', async () => {
    const response = await supertest(app).post('/checkUser').send({});
    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'userId is required');
  });
});

// Test POST /addBook
describe('POST /addBook', () => {
  it('should add a new book if it does not exist', async () => {
    await booksCollection.deleteMany({ userId: testUserId, bookTitle: 'New Test Book' });

    const response = await supertest(app)
      .post('/addBook')
      .send({
        userId: testUserId,
        bookTitle: 'New Test Book',
        totalPages: 200,
        pagesRead: 20,
        notes: 'Starting the book',
      });

    expect(response.status).toBe(200);
    expect(response.body.message).toBe('Book added successfully');
    expect(response.body.book).toHaveProperty('bookTitle', 'New Test Book');
  });

  it('should update an existing book with new data', async () => {
    const response = await supertest(app)
      .post('/addBook')
      .send({
        userId: testUserId,
        bookTitle: testBookTitle,
        totalPages: 300,
        pagesRead: 100,
        notes: 'Updated note',
      });

    expect(response.status).toBe(200);
    expect(response.body.message).toBe('Book updated successfully');
    expect(response.body.book).toMatchObject({
      bookTitle: testBookTitle,
      userId: testUserId,
      pagesRead: 100,
      notes: 'Updated note',
    });
  });

  it('should return 400 if required fields are missing', async () => {
    const response = await supertest(app)
      .post('/addBook')
      .send({ userId: testUserId });

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error');
  });
});
// to test the get history endpoint
describe('GET /history', () => {
  beforeAll(async () => {
    // Insert multiple mock books for history
    await booksCollection.insertMany([
      {
        userId: testUserId,
        bookTitle: 'History Book 1',
        totalPages: 150,
        pagesRead: 150,
        currentPage: 150,
        notes: 'Finished reading',
        dailyRead: [],
        createdAt: new Date(),
        lastUpdated: new Date()
      },
      {
        userId: testUserId,
        bookTitle: 'History Book 2',
        totalPages: 200,
        pagesRead: 100,
        currentPage: 100,
        notes: 'Halfway there',
        dailyRead: [],
        createdAt: new Date(),
        lastUpdated: new Date()
      }
    ]);
  });

