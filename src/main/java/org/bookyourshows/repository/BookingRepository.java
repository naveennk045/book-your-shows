package org.bookyourshows.repository;

import org.bookyourshows.config.DatabaseManager;
import org.bookyourshows.dto.booking.*;
import org.bookyourshows.mapper.BookingMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookingRepository {
    private final ShowRepository showRepository;
    private final PaymentRepository paymentRepository;


    public BookingRepository() {

        this.showRepository = new ShowRepository();
        this.paymentRepository = new PaymentRepository();
    }

    public Optional<BookingDetails> getBookingById(int bookingId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {

            BookingDetails details = getBookingSummaryAndShow(connection, bookingId);
            if (details == null) {
                return Optional.empty();
            }

            details.setSeats(getBookingSeats(connection, bookingId));
            details.setPayments(getBookingPayments(connection, bookingId));

            return Optional.of(details);
        }
    }

    public List<BookingSummary> getAllBookings() throws SQLException {
        String query = """
                    SELECT * FROM bookings
                    ORDER BY booked_at DESC
                """;

        List<BookingSummary> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(BookingMapper.mapRowToBookingSummary(rs));
            }
        }

        return list;
    }

    public List<BookingSummary> getAllBookingsByShowId(Integer showId) throws SQLException {
        String query = """
                    SELECT * FROM bookings
                    WHERE show_id = ?
                    ORDER BY booked_at DESC
                """;

        List<BookingSummary> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();) {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(BookingMapper.mapRowToBookingSummary(rs));
            }

        }

        return list;
    }

    public Optional<BookingMovieInfo> findBookingWithMovieAndUser(int bookingId) throws SQLException {
        String sql = """
                SELECT 
                    b.booking_id,
                    b.user_id,
                    s.movie_id
                FROM bookings b
                JOIN shows s ON b.show_id = s.show_id
                WHERE b.booking_id = ?
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new BookingMovieInfo(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("movie_id")
                ));
            }
        }

        return Optional.empty();
    }

    public List<BookingSummary> getBookingsByTheatreId(int theatreId) throws SQLException {

        String query = """
                    SELECT b.*
                    FROM bookings b
                    JOIN shows s ON b.show_id = s.show_id
                    WHERE s.theatre_id = ?
                    ORDER BY b.booked_at DESC
                """;

        List<BookingSummary> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, theatreId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(BookingMapper.mapRowToBookingSummary(rs));
                }
            }
        }

        return list;
    }

    public List<BookingSummary> getBookingsByUserId(int userId) throws SQLException {

        String query = """
                    SELECT * FROM bookings
                    WHERE user_id = ?
                    ORDER BY booked_at DESC
                """;

        List<BookingSummary> list = new ArrayList<>();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(BookingMapper.mapRowToBookingSummary(rs));
                }
            }
        }

        return list;
    }

    public Optional<BookingInfo> findBookingWithTheatreAndUser(int bookingId) throws SQLException {
        String sql = """
                SELECT 
                    b.booking_id,
                    b.user_id,
                    s.theatre_id
                FROM bookings b
                JOIN shows s ON b.show_id = s.show_id
                WHERE b.booking_id = ?
                """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new BookingInfo(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getInt("theatre_id")
                ));
            }
        }

        return Optional.empty();
    }

    private BookingDetails getBookingSummaryAndShow(Connection connection, int bookingId) throws SQLException {

        String query = """
                    SELECT
                        b.booking_id,
                        b.user_id,
                        b.show_id,
                        b.total_amount,
                        b.booking_status,
                        b.payment_status,
                        b.booked_at,
                
                        s.show_date,
                        s.start_time,
                        s.end_time,
                        s.base_price,
                        s.theatre_id,
                        s.screen_id,
                        s.movie_id,
                
                        t.theatre_name,
                        ta.city,
                        sc.screen_name,
                        st.name AS screen_type_name,
                        m.title AS movie_title,
                        m.language,
                        m.censor_rating
                    FROM bookings b
                    JOIN shows s ON s.show_id = b.show_id
                    JOIN theatres t ON t.theatre_id = s.theatre_id
                    JOIN theatre_addresses ta ON ta.theatre_id = t.theatre_id
                    JOIN screens sc ON sc.screen_id = s.screen_id
                    JOIN screen_types st ON st.screen_type_id = sc.screen_type_id
                    JOIN movies m ON m.movie_id = s.movie_id
                    WHERE b.booking_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                BookingDetails details = new BookingDetails();
                details.setBooking(BookingMapper.mapRowToBookingSummary(rs));
                details.setShow(BookingMapper.mapRowToBookingShowInfo(rs));

                return details;
            }
        }
    }

    private List<BookingSeatInfo> getBookingSeats(Connection connection, int bookingId) throws SQLException {

        String query = """
                    SELECT
                        bsd.booking_seat_id,
                        bsd.show_seat_id,
                        bsd.price_paid,
                        shs.show_id,
                        se.row_no,
                        se.seat_number,
                        sca.name AS seat_category_name
                    FROM booking_seat_details bsd
                    JOIN show_seating shs ON shs.show_seat_id = bsd.show_seat_id
                    JOIN seats se ON se.seat_id = shs.seat_id
                    JOIN seat_categories sca ON sca.seat_category_id = se.seat_category_id
                    WHERE bsd.booking_id = ?
                """;

        List<BookingSeatInfo> seats = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(BookingMapper.mapRowToBookingSeatInfo(rs));
                }
            }
        }

        return seats;
    }

    private List<BookingPaymentInfo> getBookingPayments(Connection connection, int bookingId) throws SQLException {

        String query = """
                    SELECT
                        p.transaction_id,
                        p.booking_id,
                        p.amount,
                        p.payment_gateway,
                        p.gateway_transaction_id,
                        p.status,
                        p.created_at,
                        p.updated_at
                    FROM payments p
                    WHERE p.booking_id = ?
                    AND p.status = 'SUCCESS'
                    ORDER BY p.created_at DESC
                """;

        List<BookingPaymentInfo> payments = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(BookingMapper.mapRowToBookingPaymentInfo(rs));
                }
            }
        }

        return payments;
    }

    public int createBookingWithSeats(int userId, BookingCreateRequest request, double totalAmount) throws SQLException {

        String insertBookingSql = """
                    INSERT INTO bookings (user_id, show_id, total_amount, booking_status, payment_status)
                    VALUES (?, ?, ?, 'PENDING', 'PENDING')
                """;

        String insertSeatSql = """
                    INSERT INTO booking_seat_details (booking_id, show_seat_id, price_paid)
                    SELECT ?, shs.show_seat_id,
                           (sh.base_price * scrt.price_multiplier * sc.price_multiplier)
                    FROM show_seating shs
                    JOIN shows sh ON shs.show_id = sh.show_id
                    JOIN seats s ON shs.seat_id = s.seat_id
                    JOIN seat_categories sc ON s.seat_category_id = sc.seat_category_id
                    JOIN screens scr ON s.screen_id = scr.screen_id
                    JOIN screen_types scrt on scr.screen_type_id = scrt.screen_type_id
                    WHERE shs.show_id = ? AND shs.show_seat_id = ?
                """;
        String updateShowSeatSql = """
                    UPDATE show_seating
                    SET status = 'LOCKED',
                        locked_by = ?,
                        locked_at = NOW(),
                        lock_expiry = NOW() + INTERVAL 5 MINUTE
                    WHERE show_seat_id = ? AND status = 'AVAILABLE'
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            int bookingId;

            //  Create booking
            try (PreparedStatement ps = connection.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, request.getShowId());
                ps.setDouble(3, totalAmount);

                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    throw new RuntimeException("Failed to create booking");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        connection.rollback();
                        throw new RuntimeException("Failed to get booking id");
                    }
                    bookingId = keys.getInt(1);
                }
            }

            try (
                    PreparedStatement seatInsert = connection.prepareStatement(insertSeatSql);
                    PreparedStatement seatUpdate = connection.prepareStatement(updateShowSeatSql)
            ) {

                for (Integer showSeatId : request.getShowSeatIds()) {

                    //  Lock seat
                    seatUpdate.setInt(1, userId);
                    seatUpdate.setInt(2, showSeatId);

                    int updated = seatUpdate.executeUpdate();
                    if (updated == 0) {
                        connection.rollback();
                        throw new RuntimeException("Seat already booked/locked: " + showSeatId);
                    }

                    // Insert seat booking
                    seatInsert.setInt(1, bookingId);
                    seatInsert.setInt(2, request.getShowId());
                    seatInsert.setInt(3, showSeatId);

                    seatInsert.executeUpdate();
                }
            }

            connection.commit();
            return bookingId;
        }
    }


    public void updateBookingStatus(Integer bookingId, String bookingStatus, String paymentStatus, Integer transactionId) throws SQLException {


        String query = """
                        UPDATE bookings
                        SET booking_status = ?,
                        payment_status = ?
                        WHERE booking_id = ?
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, bookingStatus);
            preparedStatement.setString(2, paymentStatus);
            preparedStatement.setInt(3, bookingId);

            int updated = preparedStatement.executeUpdate();
            if (updated == 0) {
                connection.rollback();
                throw new RuntimeException("Booking status not updated");
            }


            boolean shouldUpdatePayment = !Objects.equals(paymentStatus, "PENDING");
            if (Objects.equals(paymentStatus, "FAILED")) {
                updateShowSeatingStatus(connection, bookingId, "AVAILABLE");
            } else if (Objects.equals(bookingStatus, "CONFIRMED")) {
                updateShowSeatingStatus(connection, bookingId, "BOOKED");
            } else if (Objects.equals(paymentStatus, "REFUNDED")) {
                updateShowSeatingStatus(connection, bookingId, "AVAILABLE");
            }

            if (shouldUpdatePayment) {
                paymentRepository.updatePaymentStatus(connection, transactionId, paymentStatus);
            }
            connection.commit();
        }
    }

    private void updateShowSeatingStatus(Connection connection, Integer bookingId, String status) throws SQLException {

        String query = """
                       UPDATE show_seating ss
                       JOIN booking_seat_details bsd\s
                         ON bsd.show_seat_id = ss.show_seat_id
                       SET ss.status = ?
                       WHERE bsd.booking_id = ?
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, bookingId);

            int updated = preparedStatement.executeUpdate();
            if (updated == 0) {
                connection.rollback();
                throw new RuntimeException("Seating status not updated");
            }
        }
    }
}


