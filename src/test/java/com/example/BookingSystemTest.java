package com.example;

import org.junit.jupiter.api.BeforeEach;
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

    // bookRoom() tester

    @Test
    void shouldBookRoomWhenRoomIsAvailable() throws NotificationException {
        Room room = mock(Room.class);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
        when(room.isAvailable(futureStart, futureEnd)).thenReturn(true);

        boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

        assertThat(result).isTrue();
        verify(room).addBooking(any(Booking.class));
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    void shouldReturnFalseWhenRoomIsNotAvailable() throws NotificationException {
        Room room = mock(Room.class);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
        when(room.isAvailable(futureStart, futureEnd)).thenReturn(false);

        boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

        assertThat(result).isFalse();
        verify(room, never()).addBooking(any());
        verify(roomRepository, never()).save(any());
        verify(notificationService, never()).sendBookingConfirmation(any());
    }

    @ParameterizedTest
    @MethodSource("provideNullArgumentsForBookRoom")
    void shouldThrowExceptionWhenBookRoomParametersAreNull(String roomId, LocalDateTime start, LocalDateTime end) {
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
        LocalDateTime pastTime = now.minusDays(1);

        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() -> bookingSystem.bookRoom("A1", pastTime, pastTime.plusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() -> bookingSystem.bookRoom("A1", futureEnd, futureStart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    void shouldThrowExceptionWhenRoomDoesNotExist() {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("A1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom("A1", futureStart, futureEnd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
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

        boolean result = bookingSystem.bookRoom("A1", futureStart, futureEnd);

        assertThat(result).isTrue();

        verify(room).addBooking(any(Booking.class));
        verify(roomRepository).save(room);
    }

    // getAvailableRooms() tester

    @Test
    void shouldReturnAvailableRooms() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        Room room3 = mock(Room.class);

        when(room1.isAvailable(futureStart, futureEnd)).thenReturn(true);
        when(room2.isAvailable(futureStart, futureEnd)).thenReturn(false);
        when(room3.isAvailable(futureStart, futureEnd)).thenReturn(true);

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2, room3));

        List<Room> availableRooms = bookingSystem.getAvailableRooms(futureStart, futureEnd);

        assertThat(availableRooms).containsExactly(room1, room3);
    }

    @ParameterizedTest
    @MethodSource("provideNullArgumentsForGetAvailableRooms")
    void shouldThrowExceptionWhenGetAvailableRoomsParametersAreNull(LocalDateTime start, LocalDateTime end) {
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
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(futureEnd, futureStart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    void shouldReturnEmptyListWhenNoRoomsIsAvailable() {
        Room room = mock(Room.class);

        when(room.isAvailable(futureStart, futureEnd)).thenReturn(false);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> availableRooms = bookingSystem.getAvailableRooms(futureStart, futureEnd);

        assertThat(availableRooms).isEmpty();
    }

    // cancelBooking() tests

    @Test
    void shouldCancelBookingSuccessfully() throws NotificationException {
        Room room = mock(Room.class);
        Booking booking = new Booking("booking", "A1", futureStart, futureEnd);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(room.hasBooking("booking")).thenReturn(true);
        when(room.getBooking("booking")).thenReturn(booking);

        boolean result = bookingSystem.cancelBooking("booking");

        assertThat(result).isTrue();
        verify(room).removeBooking("booking");
        verify(roomRepository).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }

    @Test
    void shouldThrowExceptionWhenCancelBookingWithNullId() {
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boknings-id kan inte vara null");
    }

    @Test
    void shouldReturnFalseWhenBookingNotFound() {
        Room room = mock(Room.class);

        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(room.hasBooking("nonexistent")).thenReturn(false);

        boolean result = bookingSystem.cancelBooking("nonexistent");

        assertThat(result).isFalse();
        verify(room, never()).removeBooking(any());
        verify(roomRepository, never()).save(any());
    }

}



