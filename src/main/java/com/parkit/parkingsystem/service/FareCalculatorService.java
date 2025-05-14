package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMilliseconds = ticket.getInTime().getTime();
        long outTimeMilliseconds = ticket.getOutTime().getTime();
        double durationInMinutes = (outTimeMilliseconds - inTimeMilliseconds) / (1000.0 * 60);

        if (durationInMinutes <= 30) {
            ticket.setPrice(0);
            return;
        }

        double durationInHours = durationInMinutes / 60;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                double price = durationInHours * Fare.CAR_RATE_PER_HOUR;
                ticket.setPrice(discount ? price * 0.95 : price);
                break;
            }
            case BIKE: {
                double price = durationInHours * Fare.BIKE_RATE_PER_HOUR;
                ticket.setPrice(discount ? price * 0.95 : price);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}