const express = require('express');
const router = express.Router();


//hardcoded books for testing endpoints
let books = [
    {
        id: 1,
        bookName: 'Book1',
        pagesRead: 10,
        startDate: '2024-03-01',
        endDate: '2024-03-02',
        notes: 'This is a note'
    },
    {
        id: 2,
        bookName: 'Book2',
        pagesRead: 20,
        startDate: '2024-03-02',
        endDate: '2024-03-03',
        notes: 'This is a testing note'
    }, 
    {
        id: 3,
        bookName: 'Book3',
        pagesRead: 30,
        startDate: '2024-03-03',
        endDate: '2024-03-04',
        notes: 'This is a testing note'
    },
   ];
   
//To view users current book
router.get('/currentBook', (req, res) => {
    try{
        if(!books){
            return res.status(404).json({ error: 'No book found' });
        }
    res.status(200).json(books);
} catch (error) {
  console.error('Error retrieving books:', error);
  res.status(500).json({ error: 'Internal Server Error' });
}
});

//To view users current book
router.get('/currentBook/:id', (req, res) => {
    const bookId = parseInt(req.params.id);
    const currentBook = books.find(currentBook => currentBook.id === bookId);
    try{
        if(!currentBook){
            return res.status(404).json({ error: 'No book found' });
        }
    res.status(200).json(currentBook);
} catch (error) {
  console.error('Error retrieving books:', error);
  res.status(500).json({ error: 'Internal Server Error' });
}
});


// to add a new book
router.post('/addBook', (req, res) => {
    try {
        const { bookName, pagesRead, startDate, endDate, notes } = req.body;
        console.log(bookName, pagesRead, startDate, endDate, notes );

        if (!bookName || !pagesRead || !startDate || !endDate) {
            return res.status(400).json({ error: 'Missing required fields' });    
        } 
        const currentDate = new Date();
        const startDateObj = new Date(startDate);
        const endDateObj = new Date(endDate);
        
        // Date validation
        if (startDateObj >= endDateObj) {
            return res.status(400).json({ error: 'End date must be after start date' });
        }
         // Calculate book status flags
         const isComplete = endDateObj < currentDate;
         const isCurrent = !isComplete && 
                          (currentDate >= startDateObj && currentDate <= endDateObj);
        
        const newBook = {
            id: books.length + 1,
            bookName    : bookName.trim(),
            pagesRead: Math.max(0, parseInt(pagesRead)),
            startDate  : startDateObj,
            endDate: endDateObj,
            notes: notes || '',
            userId:req.user_id,
            complete: isComplete,
            current: isCurrent,
            hidden: false   
        };

        books.push(newBook);
        res.status(201).json({ newBook });
    } catch (error) {
        console.error('Error adding book:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

//To update a book
router.put('/currentBook/:id', (req, res) => {
    try {
        const bookId = parseInt(req.params.id);
        const { bookName, pagesRead, startDate, endDate, notes } = req.body;
        const currentBook = books.find(currentBook => currentBook.id === bookId);
        
        if (!currentBook) {
            return res.status(404).json({ error: 'No book found' });
        }
        currentBook.bookName = bookName;
        currentBook.pagesRead = pagesRead;
        currentBook.startDate = startDate;
        currentBook.endDate = endDate;
        currentBook.notes = notes;

        res.status(200).json(currentBook);
    } catch (error) {
        console.error('Error updating book:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

//To view users book history
router.get('/history', (req, res) => {
    res.send('Book History list');
});

//update book history
router.put('/history/:id', (req, res) => {
//update hidden markers
    res.send('Update Book History');
});


module.exports = router;