package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);
        boolean spotAvailable = ticket.getParkingSpot().isAvailable();
        Assertions.assertFalse(spotAvailable);
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        Ticket ticketBefore = ticketDAO.getTicket("ABCDEF");
        ticketBefore.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateInTime(ticketBefore);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);
        Date outTime = ticket.getOutTime();
        Assertions.assertNotNull(outTime);
        double ticketFare = ticket.getPrice();
        Assertions.assertEquals(1 * Fare.CAR_RATE_PER_HOUR, ticketFare);
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        // Create a previous ticket
        Ticket firstTicket = new Ticket();
        firstTicket.setParkingSpot(parkingSpot);
        firstTicket.setVehicleRegNumber("ABCDEF");
        firstTicket.setPrice(0);
        firstTicket.setInTime(new Date());
        ticketDAO.saveTicket(firstTicket);
        // Mock user parking
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        // Update Second Ticket In TIme
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        secondTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateInTime(secondTicket);
        parkingService.processExitingVehicle();
        // Evaluate and Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        double ticketFare = ticket.getPrice();
        Assertions.assertEquals(0.95 * Fare.CAR_RATE_PER_HOUR, ticketFare);
    }
}
