package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Service class responsible for calculating the parking fare for a ticket.
 * It computes the fare based on the duration between the vehicle's in-time and out-time,
 * applies free parking for durations less than or equal to 30 minutes,
 * and includes a 5% discount for recurring users if applicable.
 * Supports different rates for CAR and BIKE parking types.
 */
public class FareCalculatorService {

    /**
     * Calculates and sets the parking fare for the given ticket.
     * The fare is based on the time difference between the ticket's in-time and out-time.
     * If the parking duration is 30 minutes or less, the fare is set to zero.
     * A 5% discount is applied if the {@code discount} parameter is true.
     *
     * @param ticket   the ticket containing parking details (in-time, out-time, parking type)
     * @param discount {@code true} if a recurring user discount should be applied; {@code false} otherwise
     * @throws IllegalArgumentException if the out-time is null or earlier than the in-time,
     *                                  or if the parking type is unknown
     */
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
    /**
     * Rounds the calculated price to two decimal places.
     *
     * @param price the raw calculated price
     * @return the price rounded to two decimal places
     */
    private double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }
}