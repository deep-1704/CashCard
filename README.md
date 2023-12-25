# CashCard API

- This is a Spring Boot project I developed while learning Spring Framework.
- This project features a CashCard service API which makes handling allowances a lot easier.
- Currently it works on an embedded-database(H2) for test implementation.

## Endpoints

| URI             | Method | Body            | Http Response Status                  | Response Header                 | Response Body                 |
| --------------- | ------ | --------------- | ------------------------------------- | ------------------------------- | ----------------------------- |
| `/cashcards/{id}` | `GET`    | -               | 1.`200 OK`<br />2.`404 NOT_FOUND`         | -                               | 1. CashCard Object<br />2. null |
| `/cashcards`      | `GET`    | -               | `200 OK`                              | -                               | List of owned CashCards       |
| `/cashcards`      | `POST`   | CashCard object | `201 CREATED`                         | `{location: path/to/newCashCard}` | null                          |
| `/cashcards/{id}` | `PUT`    | CashCard object | 1.`204 NO_CONTENT`<br />2.`404 NOT_FOUND` | -                               | null                          |
| `/cashcards/{id}` | `DELETE` | -               | 1.`204 N0_CONTENT`<br />2.`404 NOT_FOUND` | -                               | null                          |
