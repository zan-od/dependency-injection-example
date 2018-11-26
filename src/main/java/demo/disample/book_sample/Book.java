package demo.disample.book_sample;

import demo.disample.annotations.Column;
import demo.disample.annotations.Id;
import demo.disample.annotations.Table;

import java.util.Date;

@Table(name = "book")
public class Book {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @Column(name = "publication_date")
    private Date publicationDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }
}
