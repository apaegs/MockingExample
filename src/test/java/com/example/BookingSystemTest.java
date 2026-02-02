package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

}