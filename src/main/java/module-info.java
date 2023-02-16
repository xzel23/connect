/**
 * Module containing the Connection interface.
 */
module com.dua3.connect {
    exports com.dua3.connect;

    requires static com.dua3.cabe.annotations;

    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.client5.httpclient5.win;
    requires org.slf4j;
    requires org.apache.httpcomponents.core5.httpcore5;
}
