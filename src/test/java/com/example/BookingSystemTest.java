package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    @Mock
    TimeProvider timeProvider;
    @Mock
    RoomRepository roomRepository;
    @Mock
    NotificationService notificationService;

    BookingSystem bookingSystem;

    LocalDateTime now;
    LocalDateTime futureStart;
    LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        bookingSystem = new BookingSystem(timeProvider, roomRepository, notificationService);
        now = LocalDateTime.of(2026, 2, 2, 10, 0);
        futureStart = now.plusDays(1);
        futureEnd = futureStart.plusHours(2);
    }

    @Nested
    class BookRoomTests {

        @Test
        void shouldBookRoomWhenRoomIsAvailable() throws NotificationException {
            // ARRANGE
            Room room = mock(Room.class);
            when(timeProvider.getCurrentTime()).thenReturn(now);
            when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
            when(room.isAvailable(futureStart, futureEnd)).thenReturn(true);

            // ACT
            boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

            // ASSERT
            assertThat(result).isTrue();
            verify(room).addBooking(any(Booking.class));
            verify(roomRepository).save(room);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        void shouldReturnFalseWhenRoomIsNotAvailable() throws NotificationException {
            // ARRANGE
            Room room = mock(Room.class);
            when(timeProvider.getCurrentTime()).thenReturn(now);
            when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
            when(room.isAvailable(futureStart, futureEnd)).thenReturn(false);

            // ACT
            boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

            // ASSERT
            assertThat(result).isFalse();
            verify(room, never()).addBooking(any());
            verify(roomRepository, never()).save(any());
            verify(notificationService, never()).sendBookingConfirmation(any());
        }

        @Test
        void shouldBookRoomEvenWhenNotificationFails() throws NotificationException {
            // ARRANGE
            Room room = mock(Room.class);
            when(timeProvider.getCurrentTime()).thenReturn(now);
            when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
            when(room.isAvailable(futureStart, futureEnd)).thenReturn(true);
            doThrow(new NotificationException("Notification failed"))
                    .when(notificationService).sendBookingConfirmation(any(Booking.class));

            // ACT
            boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

            // ASSERT
            assertThat(result).isTrue();
            verify(room).addBooking(any(Booking.class));
            verify(roomRepository).save(room);
        }

        @Nested
        class ValidationTests {

            @ParameterizedTest
            @MethodSource("com.example.BookingSystemTest$BookRoomTests$ValidationTests#provideNullArgumentsForBookRoom")
            void shouldThrowExceptionWhenBookRoomParametersAreNull(String roomId, LocalDateTime start, LocalDateTime end) {
                // ARRANGE & ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
            }

            static Stream<Arguments> provideNullArgumentsForBookRoom() {
                LocalDateTime validTime = LocalDateTime.now().plusDays(1);
                return Stream.of(
                        Arguments.of(null, validTime, validTime.plusHours(1)),
                        Arguments.of("A1", null, validTime.plusHours(1)),
                        Arguments.of("A1", validTime, null)
                );
            }

            @Test
            void shouldThrowExceptionWhenBookingInThePast() {
                // ARRANGE
                LocalDateTime pastTime = now.minusDays(1);
                when(timeProvider.getCurrentTime()).thenReturn(now);

                // ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.bookRoom("A1", pastTime, pastTime.plusHours(1)))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Kan inte boka tid i dåtid");
            }

            @Test
            void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
                // ARRANGE
                when(timeProvider.getCurrentTime()).thenReturn(now);

                // ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.bookRoom("A1", futureEnd, futureStart))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Sluttid måste vara efter starttid");
            }

            @Test
            void shouldThrowExceptionWhenRoomDoesNotExist() {
                // ARRANGE
                when(timeProvider.getCurrentTime()).thenReturn(now);
                when(roomRepository.findById("A1")).thenReturn(Optional.empty());

                // ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.bookRoom("A1", futureStart, futureEnd))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Rummet existerar inte");
            }
        }
    }

    @Nested
    class GetAvailableRoomsTests {

        @Test
        void shouldReturnAvailableRooms() {
            // ARRANGE
            Room room1 = mock(Room.class);
            Room room2 = mock(Room.class);
            Room room3 = mock(Room.class);
            when(room1.isAvailable(futureStart, futureEnd)).thenReturn(true);
            when(room2.isAvailable(futureStart, futureEnd)).thenReturn(false);
            when(room3.isAvailable(futureStart, futureEnd)).thenReturn(true);
            when(roomRepository.findAll()).thenReturn(List.of(room1, room2, room3));

            // ACT
            List<Room> availableRooms = bookingSystem.getAvailableRooms(futureStart, futureEnd);

            // ASSERT
            assertThat(availableRooms).containsExactly(room1, room3);
        }

        @Test
        void shouldReturnEmptyListWhenNoRoomsAreAvailable() {
            // ARRANGE
            Room room = mock(Room.class);
            when(room.isAvailable(futureStart, futureEnd)).thenReturn(false);
            when(roomRepository.findAll()).thenReturn(List.of(room));

            // ACT
            List<Room> availableRooms = bookingSystem.getAvailableRooms(futureStart, futureEnd);

            // ASSERT
            assertThat(availableRooms).isEmpty();
        }

        @Nested
        class ValidationTests {

            @ParameterizedTest
            @MethodSource("com.example.BookingSystemTest$GetAvailableRoomsTests$ValidationTests#provideNullArgumentsForGetAvailableRooms")
            void shouldThrowExceptionWhenGetAvailableRoomsParametersAreNull(LocalDateTime start, LocalDateTime end) {
                // ARRANGE & ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.getAvailableRooms(start, end))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Måste ange både start- och sluttid");
            }

            static Stream<Arguments> provideNullArgumentsForGetAvailableRooms() {
                LocalDateTime validTime = LocalDateTime.now().plusDays(1);
                return Stream.of(
                        Arguments.of(null, validTime),
                        Arguments.of(validTime, null),
                        Arguments.of(null, null)
                );
            }

            @Test
            void shouldThrowExceptionWhenGetAvailableRoomsEndTimeIsBeforeStartTime() {
                // ARRANGE & ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.getAvailableRooms(futureEnd, futureStart))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Sluttid måste vara efter starttid");
            }
        }
    }

    @Nested
    class CancelBookingTests {

        @Test
        void shouldCancelBookingSuccessfully() throws NotificationException {
            // ARRANGE
            Room room = mock(Room.class);
            Booking booking = new Booking("booking", "A1", futureStart, futureEnd);
            when(timeProvider.getCurrentTime()).thenReturn(now);
            when(roomRepository.findAll()).thenReturn(List.of(room));
            when(room.hasBooking("booking")).thenReturn(true);
            when(room.getBooking("booking")).thenReturn(booking);

            // ACT
            boolean result = bookingSystem.cancelBooking("booking");

            // ASSERT
            assertThat(result).isTrue();
            verify(room).removeBooking("booking");
            verify(roomRepository).save(room);
            verify(notificationService).sendCancellationConfirmation(booking);
        }

        @Test
        void shouldReturnFalseWhenBookingNotFound() {
            // ARRANGE
            Room room = mock(Room.class);
            when(roomRepository.findAll()).thenReturn(List.of(room));
            when(room.hasBooking("nonexistent")).thenReturn(false);

            // ACT
            boolean result = bookingSystem.cancelBooking("nonexistent");

            // ASSERT
            assertThat(result).isFalse();
            verify(room, never()).removeBooking(any());
            verify(roomRepository, never()).save(any());
        }

        @Test
        void shouldCancelBookingEvenWhenNotificationFails() throws NotificationException {
            // ARRANGE
            Room room = mock(Room.class);
            Booking booking = new Booking("booking", "A1", futureStart, futureEnd);
            when(timeProvider.getCurrentTime()).thenReturn(now);
            when(roomRepository.findAll()).thenReturn(List.of(room));
            when(room.hasBooking("booking")).thenReturn(true);
            when(room.getBooking("booking")).thenReturn(booking);
            doThrow(new NotificationException("Notification failed"))
                    .when(notificationService).sendCancellationConfirmation(booking);

            // ACT
            boolean result = bookingSystem.cancelBooking("booking");

            // ASSERT
            assertThat(result).isTrue();
            verify(room).removeBooking("booking");
            verify(roomRepository).save(room);
        }

        @Nested
        class ValidationTests {

            @Test
            void shouldThrowExceptionWhenCancelBookingWithNullId() {
                // ARRANGE & ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Boknings-id kan inte vara null");
            }

            @Test
            void shouldThrowExceptionWhenCancellingStartedBooking() {
                // ARRANGE
                Room room = mock(Room.class);
                LocalDateTime pastStart = now.minusHours(1);
                Booking booking = new Booking("booking", "A1", pastStart, now.plusHours(1));
                when(timeProvider.getCurrentTime()).thenReturn(now);
                when(roomRepository.findAll()).thenReturn(List.of(room));
                when(room.hasBooking("booking")).thenReturn(true);
                when(room.getBooking("booking")).thenReturn(booking);

                // ACT & ASSERT
                assertThatThrownBy(() -> bookingSystem.cancelBooking("booking"))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Kan inte avboka påbörjad eller avslutad bokning");
            }
        }
    }
}