import org.jspecify.annotations.NullMarked;

/**
 * Module containing the Connection interface.
 */
@NullMarked
module com.dua3.connect {
    exports com.dua3.connect;

    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.client5.httpclient5.win;
    requires org.slf4j;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.jspecify;
}
