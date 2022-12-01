USE habede_soap;

CREATE TABLE logging (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(256) NOT NULL,
    IP VARCHAR(16) NOT NULL,
    endpoint VARCHAR(256) NOT NULL,
    requested_at TIMESTAMP NOT NULL
);

CREATE TABLE subscription (
    creator_id INT NOT NULL,
    subscriber_id INT NOT NULL,
    status ENUM ('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    is_polled BOOLEAN NOT NULL DEFAULT 0,
    PRIMARY KEY (creator_id, subscriber_id)
);