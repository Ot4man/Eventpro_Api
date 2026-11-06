package com.eventpro.service;

import com.eventpro.dto.TicketResponse;
import com.eventpro.entity.*;
import com.eventpro.entity.enums.TicketStatus;
import com.eventpro.exception.TicketAlreadyUsedException;
import com.eventpro.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket validTicket;
    private Event event;
    private Client client;
    private TicketCategory ticketCategory;
    private TicketCategoriesPerEvent eventCategory;
    private ReservationLine reservationLine;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        client = Client.builder().id(1L).nom("Karim").email("karim@test.com").build();
        event = Event.builder().id(10L).title("Festival Mawazine").build();
        ticketCategory = TicketCategory.builder().id(5L).name("VIP").build();
        eventCategory = TicketCategoriesPerEvent.builder()
                .id(20L)
                .event(event)
                .category(ticketCategory)
                .unitPrice(new BigDecimal("300.00"))
                .build();

        reservation = Reservation.builder().id(100L).client(client).build();

        reservationLine = ReservationLine.builder()
                .id(50L)
                .reservation(reservation)
                .category(eventCategory)
                .quantity(2)
                .fixedUnitPrice(new BigDecimal("300.00"))
                .build();

        validTicket = Ticket.builder()
                .id(1L)
                .code("TKT-ABC12345")
                .reservationLine(reservationLine)
                .issueDate(Instant.now())
                .status(TicketStatus.VALID)
                .build();
    }

    @Test
    @DisplayName("US9 : Le scan d'un billet valide doit le passer à USED et renseigner la date de contrôle")
    void testScanTicket_Success() {
        when(ticketRepository.findByCode("TKT-ABC12345")).thenReturn(Optional.of(validTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.scanTicket("TKT-ABC12345");

        assertNotNull(response);
        assertEquals(TicketStatus.USED, response.status());
        assertNotNull(response.scanDate());
        verify(ticketRepository, times(1)).save(validTicket);
    }

    @Test
    @DisplayName("US9 : Un second scan d'un billet déjà utilisé doit lever TicketAlreadyUsedException (HTTP 409)")
    void testScanTicket_AlreadyUsed_ShouldThrowConflict() {
        validTicket.setStatus(TicketStatus.USED);
        validTicket.setScanDate(Instant.now().minusSeconds(3600));

        when(ticketRepository.findByCode("TKT-ABC12345")).thenReturn(Optional.of(validTicket));

        assertThrows(TicketAlreadyUsedException.class, () -> ticketService.scanTicket("TKT-ABC12345"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Génération unitaire : doit créer exactement 2 billets pour une ligne de réservation de quantité 2")
    void testGenerateTicketsForReservation() {
        reservation.setLines(List.of(reservationLine));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Ticket> generated = ticketService.generateTicketsForReservation(reservation);

        assertNotNull(generated);
        assertEquals(2, generated.size());
        assertEquals(TicketStatus.VALID, generated.get(0).getStatus());
        assertEquals(TicketStatus.VALID, generated.get(1).getStatus());
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }
}

