package demo.disample.book_sample;

import demo.disample.beans.CrudRepository;

import java.util.Date;
import java.util.List;

public interface BookRepository extends CrudRepository<Integer, Book> {

    List<Book> findByTitle(String title);

    List<Book> findByAuthorName(String authorName);

    List<Book> findByNumberOfPages(Integer numberOfPages);

    List<Book> findByPublicationDate(Date publicationDate);

    List<Book> findByTitleAndAuthorName(String title, String authorName);

    List<Book> findByNumberOfPagesOrPublicationDate(Integer numberOfPages, Date publicationDate);
}
