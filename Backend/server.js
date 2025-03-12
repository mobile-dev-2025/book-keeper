//For creating server in node.js using express 
const express = require('express');
const app = express();
const port = 8000;

app.use(express.json());
app.use(express.urlencoded({extended: true}));

//Basic Hello world
app.get('/', (req, res) =>{
    res.send('Hello World');
});

//To view users current book
app.get('/CurrentBook', (req, res) => {
       res.send('Current Book');
});

let books = [
    {
        bookName: 'Book1',
        pagesRead: 10,
        startDate: '2024-03-01',
        endDate: '2024-03-02',
        notes: 'This is a note'
    },
    {
        bookName: 'Book2',
        pagesRead: 20,
        startDate: '2024-03-02',
        endDate: '2024-03-03',
        notes: 'This is a testing note'
    },
    {
        bookName: 'Book3',
        pagesRead: 30,
        startDate: '2024-03-03',
        endDate: '2024-03-04',
        notes: 'This is another testing note'
    }
];
// to add a new book
app.post('/AddBook', (req, res) => {
    try {
        const { bookName, pagesRead, startDate, endDate, notes } = req.body;
        console.log(bookName, pagesRead, startDate, endDate, notes );

        if (!bookName || !pagesRead || !startDate || !endDate) {
            return res.status(400).json({ error: 'Missing required fields' });    
        }   
        const newBook = {
            bookName    : bookName,
            pagesRead: parseInt(pagesRead),
            startDate  : startDate,
            endDate: endDate, 
            notes: notes 
           
        };

        books.push(newBook);
        res.status(201).json({ newBook });
    } catch (error) {
        console.error('Error adding book:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});



//To view users book history
app.get('/History', (req, res) => {
    res.send('Book History');
});

//Listening to port
app.listen(port, function(err){
    if(err){
        console.log(`Error in running the server: ${err}`);
    }
    console.log(`Server is running on port: ${port}`);
});