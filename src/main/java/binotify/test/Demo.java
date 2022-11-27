package binotify.test;

import jakarta.jws.WebService;

import java.sql.Connection;

@WebService(endpointInterface = "binotify.test.IDemo")
public class Demo implements IDemo {
    private Connection db_conn;

    public Demo(Connection db_conn) {
        this.db_conn = db_conn;
    }

    @Override
    public String helloWorld() {
        return "Hello, World!";
    }
}
