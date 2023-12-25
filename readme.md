## Auction Project
> Developed by Paras Aggarwal

This project is built to support auction where: 
- A seller can register a product for auction and specify minimum bid.
- A buyer can bid on any product (not owned by them) and any number of times.
- A seller can end the auction and see the winner and winning bid.

Notes:
- Users information is only stored in Users Service and only user token is used to represent user while auction and the information for specific user token can be fetched from user service if required (thus following GDPR).
- Support to change the minimum bid of a product is not available in this.
- This project doesn't support multiple currencies or currency conversion. Prices are just considered as a price regardless of any currency (think of it as all prices are in Euro).
- User authentication is skipped for now. 
- We are reading user token from header but in real world user token should be fetched from auth token.

### Services
- Users Service (To store user information)
- Biding Service (To store products and associated bids)

**Basic architecture:**
![basic_architecture.png](docs%2Fbasic_architecture.png)

### Database design
**Users Service**

Table #1: 

`user_record`
- Long id (Primary key)
- String token (unique) -- [UUID format is used]
- String first_name (not nullable)
- String last_name (not nullable)
- String phone_number (unique, not nullable)
- String email (unique, not nullable)
- String street
- String house_number
- String city
- Long pincode
- String country
- String additional_address_info

**Biding Service**

Table #1:

`product_detail`

- Long id (Primary key)
- String name (not nullable)
- String description 
- Double start_price (not nullable)
- Boolean sold (not nullable)
- Double sold_price
- Boolean active
- String city
- String author

Table #2: [foreign key with `product_detail.id`]

`biding_list`

- Long id (Primary key)
- Long product_detail (Foreign key to `product_detail.id`)
- Date timestamp (not nullable, not updatable)
- Double bid_price (not nullable)
- String bider (not nullable, not updatable)

![database.png](docs%2Fdatabase.png)

### APIs and curls

**User service**

- `POST: /user {}`: **To create a new user**
```
curl --location 'localhost:8081/user' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "vorname",
    "lastName": "nachname",
    "phoneNumber": "+49000000001",
    "email": "qa-bot-1@test.de",
    "street": "straße",
    "houseNumber": "1",
    "city": "Berlin",
    "pincode": 10178,
    "country": "Germany",
    "additionalAddressInfo": "c/o nachname"
}'
```

- `POST: /user/search {}`: **To search a user by email or phone number**
```
curl --location 'localhost:8081/user/search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "qa-bot-1@test.de"
}'
```
or 
```
curl --location 'localhost:8081/user/search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "phoneNumber": "+49000000001"
}'
```

- `GET: /user/{userToken}`: **To get user information based on provided user token**
```
curl --location 'localhost:8081/user/{USER_TOKEN}' \
--header 'Content-Type: application/json'
```

**Biding service**

- `POST: /product {}`: **To add a new product for auction**. (`USER_TOKEN` is unique token of user and can be fetched from `addUser` or `searchUser` response)
> Requires a header `X-User-Token: {USER_TOKEN}`. This user will be considered author and should be a valid user. 
```
curl --location 'localhost:8080/product' \
--header 'Content-Type: application/json' \
--header 'X-User-Token: {USER_TOKEN}' \
--data '{
    "name": "Mercedes-Benz",
    "description": "Vintage car from 1930",
    "startPrice": 3000,
    "active": true
}'
```

- `GET: /products`: **To get list of products**
> Support a query parameter `allow_inactive` support boolean and default value is true. Used to support inactive products in products list. e.g. `GET: /products?allow_inactive=false`
```
curl --location 'localhost:8080/products?allow_inactive=false' \
--header 'Content-Type: application/json'
```

- `PATCH: /product/{PRODUCT_IDENTIFIER} {}`: **To toggle product status ACTIVE/INACTIVE.** (`PRODUCT_IDENTIFIER` is `id` of product and available in `addProduct` and `getProducts` response).
> Requires a header `X-User-Token: {USER_TOKEN}` and only product author can do this action. Status of product cannot be changed once it is sold out.
```
curl --location --request PATCH 'localhost:8080/product/{PRODUCT_IDENTIFIER}' \
--header 'Content-Type: application/json' \
--header 'X-User-Token: {USER_TOKEN}' \
--data '{
    "active": false
}'
```
or 
```
curl --location --request PATCH 'localhost:8080/product/{PRODUCT_IDENTIFIER}' \
--header 'Content-Type: application/json' \
--header 'X-User-Token: {USER_TOKEN}' \
--data '{
    "active": true
}'
```

- `POST: /product/{PRODUCT_IDENTIFIER}/bid {}`: **To place a bid**
> Requires a header `X-User-Token: {USER_TOKEN}`. This will be considered as user who is biding. Author cannot hit this API.
```
curl --location 'localhost:8080/product/{PRODUCT_IDENTIFIER}/bid' \
--header 'Content-Type: application/json' \
--header 'X-User-Token: {USER_TOKEN}' \
--data '{
    "price": 4500.00
}'
```

- `POST: /product/{PRODUCT_IDENTIFIER}/sold`: **To end the auction for a product**
> Requires a header `X-User-Token: {USER_TOKEN}`. It should be a valid token and only author can hit this API (so `USER_TOKEN` should represent product author.
```
curl --location --request POST 'localhost:8080/product/{PRODUCT_IDENTIFIER}}/sold' \
--header 'Content-Type: application/json' \
--header 'X-User-Token: {USER_TOKEN}'
```

### How to install
**Prerequisites:** 
- Java 17
- cURL in terminal or any API platform
- IntelliJ IDEA or any IDE (Developed on IntelliJ so works well with it)
- Enable annotation processing in your IDE (IDE preferences/settings > editor > code style > java > annotation processing)
- Install lombok plugin in your IDE

**Steps**
- Open this project in your IDE (Project should open as a Gradle project)
- Once build and indexing is done
- Open `UsersApplication.java` inside `users/src/main/java/org/deutschebank/auction/users/` and run it.
- Open `BidingApplication.java` inside `biding/src/main/java/org/deutschebank/auction/biding/` and run it.
- Use the curl commands mentioned above.
- You should be able to see this running :).
