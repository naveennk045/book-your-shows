# Book Your Shows API

RESTful APIs for users, theatres, movies, shows, bookings, payments, feedback, and admin operations.

***

## Users Management

### Get User Profile

**GET** `/users/{user_id}`

Returns the profile of a specific user.

#### Path Parameters

- `user_id` (integer) – User identifier.


#### Response

```json
{
  "user_id": 1,
  "email": "ram@gmail.com",
  "mobile_number": "9843912345",
  "first_name": "Ram",
  "last_name": "Kumar",
  "profile_picture": null,
  "address": {
    "address_id": 1,
    "address_line1": "12 Gandhi Street",
    "city": "Chennai",
    "pincode": "600001"
  }
}
```


***

### Update User Profile

**PUT** `/users/{user_id}`

Updates a user’s profile details.

#### Path Parameters

- `user_id` (integer)


#### Request Body

```json
{
  "user_id": 1,
  "email": "ram@gmail.com",
  "mobile_number": "9843560011",
  "first_name": "Ram",
  "last_name": "Kumar",
  "profile_picture": null,
  "address": {
    "address_id": 1,
    "address_line1": "12 Gandhi Street",
    "city": "Chennai",
    "pincode": "600001"
  }
}
```


#### Response

```json
{
  "message": "User profile updated successfully"
}
```


***

### Delete User

**DELETE** `/users/{user_id}`

Deletes a user and associated details (implementation-specific).

#### Path Parameters

- `user_id` (integer)


#### Response

```json
{
  "message": "User deleted successfully"
}
```


***

### Get User Bookings

**GET** `/users/{user_id}/bookings`

Returns all bookings for a particular user.

#### Path Parameters

- `user_id` (integer)


#### Success Response

```json
[
  {
    "booking_id": 1,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "total_amount": 570,
    "booked_at": "2026-03-12T08:00:00",
    "show_id": 1,
    "movie_id": 1,
    "theatre_id": 1,
    "title": "Thaai Zahavi",
    "theatre_name": "SVR cinemas"
  },
  {
    "booking_id": 2,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "total_amount": 570,
    "booked_at": "2026-03-12T08:00:00",
    "show_id": 1,
    "movie_id": 1,
    "theatre_id": 1,
    "title": "Youth",
    "theatre_name": "SVR cinemas"
  }
]
```


#### Empty Response

```json
{
  "message": "No bookings found"
}
```


***

## Theatre Management

### Add Theatre

**POST** `/theatres`

Creates a new theatre.

#### Request Body

```json
{
  "theatre_name": "PVR Cinemas",
  "email": "support@pvr.com",
  "contact_number": "9655584334",
  "total_screens": 1,
  "license_document": "https://bookyourtickets.com/lsfjgnd3vu4rie4589r9",
  "address_line1": "Some Street",
  "address_line2": "Near Main Road",
  "city": "Tiruppur",
  "state": "Tamil Nadu",
  "country": "India",
  "pincode": "641601",
  "latitude": 11.1085,
  "longitude": 77.3411
}
```


#### Response

```json
{
  "theatre_id": 6,
  "message": "Theatre created successfully"
}
```


***

### Get Theatre Details

**GET** `/theatres/{theatre_id}`

Fetch details of a specific theatre.

#### Path Parameters

- `theatre_id` (integer)


#### Response

```json
{
  "theatre_id": 1,
  "theatre_name": "SVR Cinemas",
  "email": "support@svr.com",
  "contact_number": "9655584334",
  "total_screens": 3,
  "address": {
    "address_line1": "Near Bus Stand",
    "city": "Madurai",
    "state": "Tamil Nadu",
    "pincode": "625001",
    "latitude": 9.9252,
    "longitude": 78.1198
  }
}
```


***

### List Theatres

**GET** `/theatres`
**GET** `/theatres?limit={limit}&offset={offset}`

Returns paginated list of theatres.

#### Query Parameters (optional)

- `limit` (integer, default implementation-specific)
- `offset` (integer, default implementation-specific)


#### Example Response

```json
[
  {
    "theatre_id": 1,
    "theatre_name": "SVR Cinemas",
    "city": "Madurai",
    "total_screens": 3
  },
  {
    "theatre_id": 2,
    "theatre_name": "MD Screens",
    "city": "Chennai",
    "total_screens": 2
  }
]
```


***

### Search and Filter Theatres

**GET** `/theatres?name={theatre_name}&location={theatre_location}&movie_id={movie_id}`
**GET** `/theatres?status=PENDING&registration_year={year}`

Supports filtering theatres by name, location, movie, status, and registration year.

***

### Update Theatre

**PUT** `/theatres/{theatre_id}`

Updates theatre details.

#### Path Parameters

- `theatre_id` (integer)


#### Request Body

```json
{
  "theatre_id": 1,
  "theatre_name": "SVR Cinemas",
  "email": "support@svr.com",
  "contact_number": "9655584334",
  "total_screens": 3,
  "address": {
    "address_line1": "Near Bus Stand",
    "city": "Madurai",
    "state": "Tamil Nadu",
    "pincode": "625001",
    "latitude": 9.9252,
    "longitude": 78.1198
  }
}
```


#### Response

```json
{
  "message": "Theatre updated successfully",
  "theatre_id": 1
}
```


***

### Delete Theatre

**DELETE** `/theatres/{theatre_id}`

Deletes a theatre.

#### Path Parameters

- `theatre_id` (integer)


#### Response

```json
{
  "message": "Theatre deleted successfully"
}
```


***

## Screen Configuration

### Get Screen Types

**GET** `/screen-types`

Returns all available screen types.

#### Response

```json
[
  {
    "screen_type_id": 1,
    "name": "IMAX"
  },
  {
    "screen_type_id": 2,
    "name": "Dolby Cinema"
  }
]
```


***

### Get Screen Type Details

**GET** `/screen-types/{screen_type_id}`

#### Path Parameters

- `screen_type_id` (integer)


#### Response

```json
{
  "screen_type_id": 1,
  "name": "IMAX",
  "price_multiplier": 1.5,
  "description": "Large format screen"
}
```


***

### Create Screen

**POST** `/theatres/{theatre_id}/screens`

Creates a screen under a theatre.

#### Path Parameters

- `theatre_id` (integer)


#### Request Body

```json
{
  "screen_name": "Screen 1",
  "screen_type_id": 1,
  "total_rows": 10,
  "no_of_seats": 120
}
```


#### Response

```json
{
  "message": "Screen created successfully",
  "screen_id": 5
}
```


***

### List Screens in a Theatre

**GET** `/theatres/{theatre_id}/screens`

#### Path Parameters

- `theatre_id` (integer)


#### Response

```json
[
  {
    "screen_id": 1,
    "screen_name": "Screen 1",
    "screen_type_id": 1,
    "total_rows": 10,
    "no_of_seats": 120
  },
  {
    "screen_id": 2,
    "screen_name": "Screen 2",
    "screen_type_id": 2,
    "total_rows": 8,
    "no_of_seats": 100
  }
]
```


***

### Get Screen Details

**GET** `/theatres/{theatre_id}/screens/{screen_id}`

#### Path Parameters

- `theatre_id` (integer)
- `screen_id` (integer)


#### Response

```json
{
  "screen_name": "Screen 2",
  "screen_type": {
    "id": 2,
    "name": "Dolby Cinema",
    "price_multiplier": 1.4,
    "description": "Motion seats experience"
  },
  "total_rows": 8,
  "no_of_seats": 100
}
```


***

### Update Screen

**PUT** `/theatres/{theatre_id}/screens/{screen_id}`

#### Request Body

```json
{
  "screen_name": "Screen 1 Updated",
  "screen_type_id": 2,
  "total_rows": 12,
  "no_of_seats": 140
}
```


#### Response

```json
{
  "message": "Screen updated successfully"
}
```


***

### Delete Screen

**DELETE** `/theatres/{theatre_id}/screens/{screen_id}`

#### Response

```json
{
  "message": "Screen deleted successfully"
}
```


***

## Seat Configuration

### Get Seat Categories

**GET** `/seat-categories`

#### Response

```json
[
  {
    "seat_category_id": 1,
    "name": "Standard"
  },
  {
    "seat_category_id": 2,
    "name": "Gold"
  }
]
```


***

### Get Seat Category Details

**GET** `/seat-categories/{seat_category_id}`

#### Path Parameters

- `seat_category_id` (integer)


#### Response

```json
{
  "seat_category_id": 1,
  "name": "Standard",
  "price_multiplier": 1.2,
  "description": "Normal seats with medium comfort and very near to screens"
}
```


***

### Create Seats for a Screen

**POST** `/theatres/{theatre_id}/screens/{screen_id}/seats`

#### Request Body

```json
{
  "rows": [
    {
      "row_label": "A",
      "row_no": 1,
      "segments": [
        {
          "seat_category_id": 1,
          "seat_count": 3
        },
        {
          "seat_category_id": 2,
          "seat_count": 2
        },
        {
          "seat_category_id": 3,
          "seat_count": 3
        }
      ]
    },
    {
      "row_label": "B",
      "row_no": 2,
      "segments": [
        {
          "seat_category_id": 1,
          "seat_count": 5
        },
        {
          "seat_category_id": 2,
          "seat_count": 5
        }
      ]
    }
  ]
}
```


#### Response

```json
{
  "message": "Seats created successfully"
}
```


***

### List Seats for a Screen

**GET** `/theatres/{theatre_id}/screens/{screen_id}/seats`

#### Response

```json
[
  {
    "seatId": 1,
    "seatNumber": "A1",
    "rowNo": 1,
    "seat_category_id": 2
  },
  {
    "seatId": 2,
    "seatNumber": "A2",
    "rowNo": 1,
    "seat_category_id": 1
  }
]
```


***

### Get Seat Details

**GET** `/theatres/{theatre_id}/screens/{screen_id}/seats/{seat_id}`

#### Response

```json
{
  "status": "success",
  "message": "Seat details fetched successfully",
  "data": {
    "seatId": 1,
    "seatNumber": "A1",
    "rowNo": 1,
    "seatCategory": {
      "id": 1,
      "name": "Standard",
      "priceMultiplier": 1.2
    }
  }
}
```


***

### Update Seat

**PUT** `/theatres/{theatre_id}/screens/{screen_id}/seats/{seat_id}`

#### Request Body

```json
{
  "seatCategoryId": 2
}
```


#### Response

```json
{
  "status": "success",
  "message": "Seat updated successfully"
}
```


***

### Delete Seat

**DELETE** `/theatres/{theatre_id}/screens/{screen_id}/seats/{seat_id}`

#### Response

```json
{
  "message": "Seat details fetched successfully"
}
```


***

## Theatre Feedback and Bookings (Owner View)

### View Customer Feedback for Theatre

**GET** `/theatres/{theatre_id}/feedbacks`

#### Response

```json
[
  {
    "rating_id": 1,
    "rating": 4,
    "comment": "great experience good sound quality",
    "created_at": "",
    "user_id": 1,
    "name": "Ram Kumar"
  },
  {
    "rating_id": 2,
    "rating": 5,
    "comment": "Excellent theatre!",
    "created_at": "",
    "user_id": 3,
    "name": "Karthik"
  }
]
```

*(Alternate response structure with nested `user` is documented under Feedback Management.)*

***

### View All Bookings for a Theatre

**GET** `/theatres/{theatre_id}/bookings`

#### Response

```json
[
  {
    "booking_id": 1,
    "total_amount": 570,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "booked_at": "2026-03-12T08:00:00",
    "user_id": 1,
    "name": "Ram Kumar",
    "show_id": 1,
    "movie_id": 1,
    "title": "Thaai Kizhavi"
  },
  {
    "booking_id": 2,
    "total_amount": 670,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "booked_at": "2026-03-12T08:00:00",
    "user_id": 1,
    "name": "naveen Kumar",
    "show_id": 1,
    "movie_id": 1,
    "title": "Youth"
  }
]
```


***

## Movie Management

### Add Movie (Admin)

**POST** `/admin/movies`

#### Request Body

```json
{
  "title": "Youth",
  "language": "Tamil",
  "genre": "RomCom",
  "duration": 150,
  "release_date": "2026-04-10",
  "poster_url": "https://example.com/poster.jpg",
  "trailer_url": "https://youtube.com/trailer",
  "description": "Action packed movie",
  "censor_rating": "U/A"
}
```


#### Response

```json
{
  "message": "Movie created successfully",
  "movie_id": 10
}
```


***

### Update Movie (Admin)

**PUT** `/admin/movies/{movie_id}`

#### Request Body

```json
{
  "title": "Youth",
  "language": "Tamil",
  "genre": "RomCom",
  "duration": 150,
  "release_date": "2026-04-10",
  "poster_url": "https://example.com/poster.jpg",
  "trailer_url": "https://youtube.com/trailer",
  "description": "Action packed movie",
  "censor_rating": "U/A"
}
```


#### Response

```json
{
  "message": "Movie updated successfully"
}
```


***

### Delete Movie (Admin)

**DELETE** `/admin/movies/{movie_id}`

#### Response

```json
{
  "message": "Movie deleted successfully"
}
```


***

### Get Movie Details

**GET** `/movies/{movie_id}`

#### Response

```json
{
  "title": "Youth",
  "language": "Tamil",
  "genre": "RomCom",
  "duration": 150,
  "release_date": "2026-04-10",
  "poster_url": "https://example.com/poster.jpg",
  "trailer_url": "https://youtube.com/trailer",
  "description": "Action packed movie",
  "censor_rating": "U/A"
}
```


***

### List Movies

**GET** `/movies`
**GET** `/movies?limit={limit}&offset={offset}`
**GET** `/movies?name={movie_name}`
**GET** `/movies?language={movie_language}&release_year={year}&genre={genre}&sort=release_date`

#### Example Response

```json
[
  {
    "movie_id": 1,
    "title": "Leo",
    "language": "Tamil",
    "genre": "Action"
  },
  {
    "movie_id": 2,
    "title": "Vadam",
    "language": "Tamil",
    "genre": "Drama"
  }
]
```


***

## Show Management

### Add Show

**POST** `/shows`

#### Request Body

```json
{
  "theatre_id": 1,
  "screen_id": 1,
  "movie_id": 1,
  "show_date": "2026-04-10",
  "start_time": "18:00:00",
  "end_time": "21:00:00",
  "base_price": 120
}
```


#### Response

```json
{
  "message": "Show created successfully",
  "show_id": 10,
  "status": "SCHEDULED"
}
```


***

### View Show

**GET** `/shows/{show_id}`

#### Response

```json
{
  "status": "success",
  "data": {
    "show_id": 1,
    "show_date": "2026-04-10",
    "start_time": "18:00:00",
    "end_time": "21:00:00",
    "base_price": 120,
    "status": "SCHEDULED",
    "movie": {
      "movie_id": 1,
      "title": "Leo",
      "duration": 150
    },
    "theatre": {
      "theatre_id": 1,
      "theatre_name": "SVR Cinemas",
      "city": "Madurai"
    },
    "screen": {
      "screen_id": 1,
      "screen_name": "Screen 1",
      "screen_type": {
        "name": "IMAX",
        "price_multiplier": 1.5
      }
    }
  }
}
```


***

### List Shows (By Theatre)

**GET** `/shows?theatreId={theatre_id}`

#### Example Response

```json
[
  {
    "show_id": 1,
    "show_date": "2026-04-10",
    "start_time": "18:00:00",
    "end_time": "21:00:00",
    "base_price": 120,
    "movie": {
      "movie_id": 1,
      "title": "Leo"
    },
    "screen": {
      "screen_id": 1,
      "screen_name": "Screen 1"
    }
  },
  {
    "show_id": 3,
    "show_date": "2026-04-10",
    "start_time": "18:00:00",
    "end_time": "21:00:00",
    "base_price": 120,
    "movie": {
      "movie_id": 1,
      "title": "Youth"
    },
    "screen": {
      "screen_id": 1,
      "screen_name": "Screen 1"
    }
  }
]
```


***

### Update Show

**PUT** `/shows/{show_id}`

#### Request Body

```json
{
  "start_time": "19:00:00",
  "end_time": "22:00:00",
  "base_price": 150
}
```


#### Response

```json
{
  "message": "Show updated successfully"
}
```


***

### Delete Show

**DELETE** `/shows/{show_id}`

#### Response

```json
{
  "message": "Show deleted successfully"
}
```


***

## Seat Availability

### Get Seat Layout for a Show

**GET** `/shows/{show_id}/seats`

#### Response

```json
{
  "show_id": 1,
  "screen": {
    "screen_name": "Screen 1",
    "screen_type": {
      "name": "IMAX",
      "price_multiplier": 1.5
    }
  },
  "layout": [
    {
      "row_label": "A",
      "seats": [
        {
          "seat_id": 1,
          "seat_number": "A1",
          "seat_category": {
            "name": "Standard",
            "price_multiplier": 1.2
          },
          "status": "AVAILABLE",
          "price": 180
        },
        {
          "seat_id": 2,
          "seat_number": "A2",
          "seat_category": {
            "name": "Gold",
            "price_multiplier": 1.3
          },
          "status": "BOOKED",
          "price": 195
        }
      ]
    }
  ]
}
```


***

## Booking Management

### Book Tickets

**POST** `/bookings`

#### Request Body

```json
{
  "showId": 1,
  "seatIds": [1, 2, 3]
}
```


#### Response

```json
{
  "bookingId": 10,
  "totalAmount": 570,
  "bookingStatus": "PENDING",
  "paymentStatus": "PENDING"
}
```


***

### View Booking Details

**GET** `/bookings/{booking_id}`

#### Response

```json
{
  "booking_id": 1,
  "booking_status": "CONFIRMED",
  "payment_status": "SUCCESS",
  "total_amount": 570,
  "booked_at": "2026-03-20T10:00:00",
  "show": {
    "show_id": 1,
    "show_date": "2026-04-10",
    "start_time": "18:00:00"
  },
  "movie": {
    "movie_id": 1,
    "title": "Leo"
  },
  "theatre": {
    "theatre_name": "SVR Cinemas"
  },
  "screen": {
    "screen_name": "Screen 1"
  },
  "seats": [
    {
      "seat_number": "A1",
      "seat_category": "Gold",
      "price_paid": 180
    },
    {
      "seat_number": "A2",
      "seat_category": "Gold",
      "price_paid": 180
    }
  ]
}
```


***

### Cancel Booking

**PUT** `/bookings/{booking_id}/cancel`

#### Response

```json
{
  "message": "Booking cancelled successfully"
}
```


***

## Payments and Refunds

### Initiate Payment for Booking

**POST** `/bookings/{booking_id}/payments`

#### Request Body

```json
{
  "amount": 570
}
```


#### Response

```json
{
  "status": "success",
  "message": "Payment initiated",
  "data": {
    "payment_id": 101,
    "status": "INITIATED",
    "redirect_url": "https://payment-gateway.com/session"
  }
}
```


***

### Refund Payment

**POST** `/payments/{payment_id}/refund`

#### Request Body

```json
{
  "amount": 570,
  "reason": "Booking cancelled"
}
```

*(Response format can be defined based on gateway integration.)*

***

## Feedback Management

### Get Feedback for a Theatre

**GET** `/theatres/{theatre_id}/feedbacks`

#### Response

```json
[
  {
    "rating_id": 1,
    "rating": 4,
    "comment": "great experience good sound quality",
    "created_at": "",
    "user": {
      "user_id": 1,
      "name": "Ram Kumar"
    }
  },
  {
    "rating_id": 2,
    "rating": 5,
    "comment": "Excellent theatre!",
    "created_at": "",
    "user": {
      "user_id": 3,
      "name": "Karthik"
    }
  }
]
```


***

### Get Feedback for a Movie

**GET** `/movies/{movie_id}/ratings`

#### Response

```json
[
  {
    "rating_id": 1,
    "rating": 4,
    "comment": "Great movie!",
    "created_at": "",
    "user": {
      "user_id": 1,
      "name": "Ram Kumar"
    }
  },
  {
    "rating_id": 2,
    "rating": 5,
    "comment": "Excellent visuals!",
    "created_at": "",
    "user": {
      "user_id": 2,
      "name": "Selva"
    }
  }
]
```


***

### Submit Theatre Feedback

**POST** `/theatres/{theatre_id}/feedbacks`

#### Request Body

```json
{
  "booking_id": 1,
  "rating": 4,
  "comment": "Clean theatre and good sound"
}
```


#### Response

```json
{
  "message": "Feedback submitted successfully",
  "ratingId": 10
}
```


***

### Submit Movie Rating

**POST** `/movies/{movie_id}/ratings`

#### Request Body

```json
{
  "booking_id": 1,
  "rating": 4,
  "comment": "Great movie with stunning actions"
}
```


#### Response

```json
{
  "message": "Feedback submitted successfully",
  "rating_id": 16
}
```


***

## Admin Management

### List All Users

**GET** `/users`

(Admin-only context.)

#### Response

```json
[
  {
    "user_id": 1,
    "email": "ram@gmail.com",
    "first_name": "Ram",
    "last_name": "Kumar",
    "role": "CUSTOMER",
    "account_status": "ACTIVE"
  },
  {
    "user_id": 2,
    "email": "selva@gmail.com",
    "first_name": "Selva",
    "last_name": "Kumar",
    "role": "THEATRE_OWNER",
    "account_status": "ACTIVE"
  }
]
```


***

### Approve Theatre Registration

**PUT** `/admin/theatres/{theatre_id}/approve`

#### Response

```json
{
  "message": "Theatre status updated successfully"
}
```


***

### Reject Theatre Registration

**PUT** `/admin/theatres/{theatre_id}/reject`

#### Request Body

```json
{
  "reason": "Reason for rejection"
}
```


#### Response

```json
{
  "message": "Theatre status updated successfully"
}
```


***

### Manage Screen Types (Admin)

#### Create Screen Type

**POST** `/admin/screen-types`

```json
{
  "name": "IMAX",
  "price_multiplier": 1.5,
  "description": "Large format screen"
}
```

Response:

```json
{
  "message": "Screen type created successfully",
  "screenTypeId": 10
}
```


#### Update Screen Type

**PUT** `/admin/screen-types/{id}`

```json
{
  "name": "IMAX Updated",
  "price_multiplier": 1.6,
  "description": "Updated description"
}
```

Response:

```json
{
  "message": "Screen type updated successfully"
}
```


#### Delete Screen Type

**DELETE** `/admin/screen-types/{id}`

```json
{
  "message": "Screen type deleted successfully"
}
```


***

### Manage Seat Categories (Admin)

#### Create Seat Category

**POST** `/admin/seat-categories`

```json
{
  "name": "Gold",
  "price_multiplier": 1.3,
  "description": "Middle row seating"
}
```

Response:

```json
{
  "message": "Seat category created successfully",
  "seat_category_id": 10
}
```


#### Update Seat Category

**PUT** `/admin/seat-categories/{id}`

```json
{
  "name": "Gold",
  "price_multiplier": 1.3,
  "description": "Middle row seating"
}
```

Response:

```json
{
  "message": "Seat category updated successfully"
}
```


#### Delete Seat Category

**DELETE** `/admin/seat-categories/{id}`

```json
{
  "message": "Seat category deleted successfully"
}
```


***

## Admin Analytics and Dashboard

### View All Bookings (Admin)

**GET** `/admin/bookings`

#### Response

```json
[
  {
    "booking_id": 1,
    "total_amount": 570,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "booked_at": "",
    "user": {
      "user_id": 1,
      "name": "Ram Kumar"
    },
    "movie": {
      "movie_id": 1,
      "title": "Leo"
    },
    "theatre": {
      "theatre_id": 1,
      "theatre_name": "SVR Cinemas"
    }
  },
  {
    "booking_id": 1,
    "total_amount": 670,
    "booking_status": "CONFIRMED",
    "payment_status": "SUCCESS",
    "booked_at": "",
    "user": {
      "user_id": 2,
      "name": "Selva Kumar"
    },
    "movie": {
      "movie_id": 5,
      "title": "youth"
    },
    "theatre": {
      "theatre_id": 2,
      "theatre_name": "PVR Cinemas"
    }
  }
]
```


***

### View All Payments (Admin)

**GET** `/admin/payments`

#### Response

```json
[
  {
    "payment_id": 101,
    "booking_id": 1,
    "amount": 570,
    "payment_gateway": "Stripe",
    "status": "SUCCESS",
    "created_at": ""
  },
  {
    "payment_id": 102,
    "booking_id": 2,
    "amount": 730,
    "payment_gateway": "Stripe",
    "status": "FAILED",
    "created_at": ""
  }
]
```


***

### Admin Analytics Dashboard

**GET** `/admin/analytics/dashboard`

High-level booking, revenue, and usage metrics (exact schema can be designed as per analytics requirements).

***

If you want, I can next turn this into an OpenAPI (Swagger) YAML/JSON spec from this Markdown so you can plug it directly into Swagger UI or Postman.

