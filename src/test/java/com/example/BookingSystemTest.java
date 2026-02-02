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

    @BeforeEach
    void setUp() {
        bookingSystem = new BookingSystem(timeProvider, roomRepository, notificationService);
    }

    @Test
    void shouldBookRoomWhenRoomIsAvailable() throws NotificationException {
        LocalDateTime now = LocalDateTime.of(2026,2,2,10,0);
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = start.plusHours(2);

        Room room = mock(Room.class);

        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("A1")).thenReturn(Optional.of(room));
        when(room.isAvailable(start,end)).thenReturn(true);

        boolean result = bookingSystem.bookRoom("A1", start, end);

        assertThat(result).isTrue();
        verify(room).addBooking(any());
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any());
        
    }

    @Test
    void shouldReturnFalseWhenRoomIsNotAvailable() {

    }

    @Test
    void shouldReturnTrueIfNotificationFails() {
        
    }

}