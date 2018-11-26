package demo.disample.book_sample;

import demo.disample.annotations.Autowired;
import demo.disample.annotations.Component;

import java.util.Date;

@Component
public class BookService {

    private final static Date TEST_DATE_1 = new Date(1543190400000L);
    private final static Date TEST_DATE_2 = new Date(1514764800000L);

    private BookRepository bookRepository;

    @Autowired
    public void setBookRepository(BookRepository bookRepository){
        this.bookRepository = bookRepository;
    }

    public void testAll() {
        bookRepository.findByTitle("test title");
        bookRepository.findByAuthorName("some author");
        bookRepository.findByNumberOfPages(123);
        bookRepository.findByPublicationDate(TEST_DATE_1);
        bookRepository.findByTitleAndAuthorName("new title", "other author");
        bookRepository.findByNumberOfPagesOrPublicationDate(321, TEST_DATE_2);
    }

}
