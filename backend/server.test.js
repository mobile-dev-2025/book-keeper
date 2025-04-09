const supertest = require('supertest');
const app = require('./server'); // Make sure this exports the Express `app`, not the server

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
