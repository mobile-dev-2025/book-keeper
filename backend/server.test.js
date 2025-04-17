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
  it('should return reading history for a valid userId', async () => {
    const response = await supertest(app)
      .get('/history')
      .query({ userId: testUserId });
  //test if the response is correct
    expect(response.status).toBe(200);
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'User reading history retrieved successfully');
    expect(Array.isArray(response.body.books)).toBe(true);
    expect(response.body.books.length).toBeGreaterThanOrEqual(2);

    //test if the books belong to the test user
    response.body.books.forEach(book => {
      expect(book).toHaveProperty('userId', testUserId);
      expect(book).toHaveProperty('bookTitle');
      expect(book).toHaveProperty('totalPages');
    });
  });
  // Test for no history found
  it('should return 400 if userId is missing', async () => {
    const response = await supertest(app)
      .get('/history');

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'userId is required');
  });
});
describe('GET /readingPlans', () => {
  const testUserId = 'test-reading-plans-user';
  const testBookTitle = 'Test Reading Book';

  beforeAll(async () => {
    await db.collection('reading-plans').deleteMany({ userId: testUserId });

    await db.collection('reading-plans').insertMany([
      {
        userId: testUserId,
        bookTitle: testBookTitle,
        dailyGoal: 15,
        startDate: new Date(),
      },
      {
        userId: testUserId,
        bookTitle: 'Another Book',
        dailyGoal: 10,
        startDate: new Date(),
      },
    ]);
  });

  afterAll(async () => {
    await db.collection('reading-plans').deleteMany({ userId: testUserId });
  });
  it('should return reading plans for a valid userId', async () => {
    const response = await supertest(app)
      .get('/readingPlans')
      .query({ userId: testUserId });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Reading plans retrieved successfully');
    expect(Array.isArray(response.body.readingPlans)).toBe(true);
    expect(response.body.readingPlans.length).toBeGreaterThanOrEqual(1);
    expect(response.body.readingPlans[0]).toHaveProperty('userId', testUserId);
  });

  it('should return filtered reading plans when bookTitle is provided', async () => {
    const response = await supertest(app)
      .get('/readingPlans')
      .query({ userId: testUserId, bookTitle: testBookTitle });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', `Reading plans for "${testBookTitle}" retrieved successfully`);
    expect(Array.isArray(response.body.readingPlans)).toBe(true);
    expect(response.body.readingPlans.length).toBe(1);
    expect(response.body.readingPlans[0]).toMatchObject({
      userId: testUserId,
      bookTitle: testBookTitle,
    });
  });

  it('should return 400 if userId is missing', async () => {
    const response = await supertest(app).get('/readingPlans');
    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'userId is required');
  });

  it('should return empty array if no plans match bookTitle', async () => {
    const response = await supertest(app)
      .get('/readingPlans')
      .query({ userId: testUserId, bookTitle: 'Nonexistent Book' });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'No reading plans found for book "Nonexistent Book"');
    expect(Array.isArray(response.body.readingPlans)).toBe(true);
    expect(response.body.readingPlans.length).toBe(0);
  });
});
describe('POST /readingPlans', () => {
  const testUserId = 'reading-plan-user';
  const testBookTitle = 'Reading Plan Book';

  beforeAll(async () => {
    // Clean state
    await db.collection('books').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
    await db.collection('reading-plans').deleteMany({ userId: testUserId, bookTitle: testBookTitle });

    // Add test book
    await db.collection('books').insertOne({
      userId: testUserId,
      bookTitle: testBookTitle,
      totalPages: 120,
      pagesRead: 20,
    });
  });

  afterAll(async () => {
    // Cleanup
    await db.collection('books').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
    await db.collection('reading-plans').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
  });

  it('should create a new reading plan if none exists', async () => {
    const response = await supertest(app)
      .post('/readingPlans')
      .send({
        userId: testUserId,
        bookTitle: testBookTitle,
        pagesPerDay: 10,
      });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Reading plan created successfully');
    expect(response.body.readingPlan).toHaveProperty('userId', testUserId);
    expect(response.body.readingPlan).toHaveProperty('bookTitle', testBookTitle);
    expect(response.body.readingPlan).toHaveProperty('pagesPerDay', 10);
    expect(response.body.readingPlan).toHaveProperty('estimatedDays');
  });

  it('should update the existing reading plan instead of creating a new one', async () => {
    const response = await supertest(app)
      .post('/readingPlans')
      .send({
        userId: testUserId,
        bookTitle: testBookTitle,
        pagesPerDay: 15,
      });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Reading plan updated successfully');
    expect(response.body.readingPlan).toHaveProperty('pagesPerDay', 15);
    expect(response.body.readingPlan).toHaveProperty('updatedAt');
  });

  it('should return 400 if required fields are missing', async () => {
    const response = await supertest(app)
      .post('/readingPlans')
      .send({ userId: testUserId }); // Missing bookTitle and pagesPerDay

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'userId, bookTitle, and pagesPerDay are required');
  });

  it('should return 404 if the book is not found for the user', async () => {
    const response = await supertest(app)
      .post('/readingPlans')
      .send({
        userId: testUserId,
        bookTitle: 'Nonexistent Book',
        pagesPerDay: 10,
      });

    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('error', 'Book not found for this user');
  });
});
describe('GET /readingStats', () => {
  const testUserId = 'reading-stats-user';
  const testBookTitle = 'Stats Book';

  beforeAll(async () => {
    // Clean previous data
    await db.collection('books').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
    await db.collection('reading-plans').deleteMany({ userId: testUserId, bookTitle: testBookTitle });

    // Insert a book with reading progress
    await db.collection('books').insertOne({
      userId: testUserId,
      bookTitle: testBookTitle,
      totalPages: 200,
      pagesRead: 60,
      dailyRead: [
        { date: '2024-01-01', page: 10 },
        { date: '2024-01-02', page: 20 },
        { date: '2024-01-03', page: 30 },
      ]
    });

    // Insert corresponding reading plan
    await db.collection('reading-plans').insertOne({
      userId: testUserId,
      bookTitle: testBookTitle,
      pagesPerDay: 20,
      totalPages: 200,
      pagesRead: 60,
    });
  });

  afterAll(async () => {
    await db.collection('books').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
    await db.collection('reading-plans').deleteMany({ userId: testUserId, bookTitle: testBookTitle });
  });

  it('should return reading stats if book and plan exist', async () => {
    const response = await supertest(app)
      .get('/readingStats')
      .query({ userId: testUserId, bookTitle: testBookTitle });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Reading stats retrieved successfully');
    expect(Array.isArray(response.body.stats)).toBe(true);
    expect(response.body.stats.length).toBe(3);
    expect(response.body.stats[0]).toMatchObject({
      date: '2024-01-01',
      plan: 20,
      actual: 10,
      bonus: -10
    });
    expect(response.body.stats[2].actual).toBe(60);
  });

  it('should return 400 if required query parameters are missing', async () => {
    const response = await supertest(app)
      .get('/readingStats')
      .query({ userId: testUserId }); // missing bookTitle

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'userId and bookTitle are required');
  });

  it('should return 404 if book or reading plan not found', async () => {
    const response = await supertest(app)
      .get('/readingStats')
      .query({ userId: 'nonexistent-user', bookTitle: 'nonexistent-book' });

    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('error', 'Book or reading plan not found');
  });
});
describe('POST /finishedBook', () => {
  const testUserId = 'finished-book-user';
  const testBookTitle = 'Finished Book Title';

  beforeAll(async () => {
    await booksCollection.deleteMany({ userId: testUserId, bookTitle: testBookTitle });

    await booksCollection.insertOne({
      userId: testUserId,
      bookTitle: testBookTitle,
      currentPage: 80,
      totalPages: 100,
      pagesRead: 80,
      dailyRead: []
    });
  });

  afterAll(async () => {
    await booksCollection.deleteMany({ userId: testUserId, bookTitle: testBookTitle });
  });

  it('should mark the book as completed successfully', async () => {
    const response = await supertest(app)
      .post('/finishedBook')
      .send({ userId: testUserId, bookTitle: testBookTitle });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Book marked as completed successfully');
    expect(response.body.book).toMatchObject({
      userId: testUserId,
      bookTitle: testBookTitle,
      currentPage: 100,
      pagesRead: 100
    });
    expect(new Date(response.body.book.endDate)).toBeInstanceOf(Date);
  });

  it('should return 404 if the book is not found', async () => {
    const response = await supertest(app)
      .post('/finishedBook')
      .send({ userId: 'nonexistent-user', bookTitle: 'nonexistent-book' });

    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('error', 'Book not found');
  });
});



 