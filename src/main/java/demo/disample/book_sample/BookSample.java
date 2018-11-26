package demo.disample.book_sample;

import demo.disample.beans.Application;

public class BookSample {

    public static void main(String[] args) {
        Application.run();

        BookService service = (BookService) Application.getBean(BookService.class);
        service.testAll();
    }

}
