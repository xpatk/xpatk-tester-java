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
        double durationInHours = (outTimeMilliseconds - inTimeMilliseconds) / (1000.0 * 60 * 60);

        if (durationInHours <= 0.5) {
            ticket.setPrice(0);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                double price = roundPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                ticket.setPrice(discount ? price * 0.95 : price);
                break;
            }
            case BIKE: {
                double price = roundPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                ticket.setPrice(discount ? price * 0.95 : price);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    private double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }
}