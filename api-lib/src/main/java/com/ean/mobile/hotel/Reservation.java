/*
 * Copyright 2013 EAN.com, L.P. All rights reserved.
 */

package com.ean.mobile.hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ean.mobile.Address;

/**
 * Holds the response from a booking response. Together with an itinerary, represents all information knowable
 * about a particular reservation.
 * Built from the <a href="http://developer.ean.com/docs/read/hotels/version_3/book_reservation/Response_Format">
 *     HotelRoomReservationResponse</a> documentation, as well as experimental results with test bookings.
 */
public final class Reservation {

    /**
     * The formatter used for paring DateTime objects from returned api date fields.
     */
    private static final DateTimeFormatter API_DATE_PARSER = DateTimeFormat.forPattern("MM/dd/YYYY");

    /**
     * EAN's unique ID for the reservation. Used along with the booking confirmation number for any communication
     * that EAN or your own customer service department makes with the customer.
     * Ensure both values are clearly provided to the customer.
     */
    public final long itineraryId;

    /**
     * Confirmation number(s) for the booking, one per room booked.
     * Generated by the Expedia Collect reservation database or from the specific Hotel
     * Collect system used to make the booking.
     * Used along with the itinerary ID for any communication that EAN or your own customer service
     * department makes with the customer. Ensure both values are clearly provided to the customer.
     */
    public final List<Long> confirmationNumbers;

    /**
     * Indicates if the hotel itself confirmed the reservation as it was processed.
     * Always returns as true for Expedia Collect.
     * <br />
     * For Hotel Collect , when returned as false, indicates the property has not yet
     * returned a confirmation number for the reservation.
     * The reservation will likely return with a PS status.
     * <br />
     * In these cases, an EAN agent will monitor the booking until it is fully confirmed.
     * The confirmation email will advise the customer that a confirmation number will be
     * forwarded by an agent as soon as the property provides it.
     */
    public final boolean processedWithConfirmation;

    /**
     * Any error text that may have been generated during the booking process
     * in addition to the contents of the EanWSError common element.
     *
     * Will be null in the case that an error did not occur.
     */
    public final String errorText;

    /**
     * Any information received from the hotel at the time of booking. May be null.
     */
    public final String hotelReplyText;

    /**
     * The supplier used to actually make the booking.
     */
    public final SupplierType supplierType;

    /**
     * Indicates the status of the reservation in the supplier system at the time of booking.
     * Anticipate appropriate customer messaging for all non-confirmed values.
     */
    public final ConfirmationStatus reservationStatusCode;

    /**
     * Indicator for a prevented duplicate booking,
     * used in conjunction with the affiliateConfirmationId request parameter.
     * Returns as true along with the existing successful itinerary
     * if the same confirmation value is sent more than once.
     */
    public final boolean existingItinerary;

    /**
     * Check-in instructions for the hotel.
     */
    public final String checkInInstructions;

    /**
     * The date of check-in.
     */
    public final LocalDate arrivalDate;

    /**
     * The date of check-out.
     */
    public final LocalDate departureDate;

    /**
     * The name of the hotel that this reservation is for.
     */
    public final String hotelName;

    /**
     * The address of the hotel.
     */
    public final Address hotelAddress;

    /**
     * A short description of the room booked.
     */
    public final String roomDescription;

    /**
     * Whether or not this booking is refundable. If the booking is successful and this is set to true,
     * then the amount charged is final and cannot be refunded. Applies only to Expedia Collect.
     */
    public final boolean nonRefundable;

    /**
     * Confirms how many guests are guaranteed for the room booked.
     *
     * If original guest count is lower than this number for a Hotel Collect property,
     * make discrepancy very clear to the customer as extra person charges are likely
     * to be charged by the property at this point.
     */
    public final int rateOccupancyPerRoom;

    /**
     * Cancellation policy for the property. Display required.
     */
    public final CancellationPolicy cancellationPolicy;

    /**
     * The list of rate informations associated with this reservation.
     */
    public final List<Rate> rateInformations;

    /**
     * Constructs a reservation object from an appropriately structured JSONObject.
     * @param object The appropriately structured JSONObject.
     */
    public Reservation(final JSONObject object) {
        final List<Long> localConfirmationNumbers;
        if (object.optJSONArray("confirmationNumbers") != null) {
            final JSONArray confirmationNumbersJson = object.optJSONArray("confirmationNumbers");
            localConfirmationNumbers = new ArrayList<Long>(confirmationNumbersJson.length());
            for (int i = 0; i < confirmationNumbersJson.length(); i++) {
                localConfirmationNumbers.add(confirmationNumbersJson.optLong(i));
            }
        } else {
            localConfirmationNumbers = Collections.singletonList(object.optLong("confirmationNumbers"));
        }

        this.itineraryId = object.optLong("itineraryId");
        this.confirmationNumbers = Collections.unmodifiableList(localConfirmationNumbers);
        this.processedWithConfirmation = object.optBoolean("processedWithConfirmation");
        this.errorText = object.optString("errorText");
        this.hotelReplyText = object.optString("hotelReplyText");
        this.supplierType = SupplierType.getByCode(object.optString("supplierType"));
        this.reservationStatusCode = ConfirmationStatus.fromString(object.optString("reservationStatusCode"));
        this.existingItinerary = object.optBoolean("existingItinerary");
        this.checkInInstructions = object.optString("checkInInstructions");
        this.arrivalDate = API_DATE_PARSER.parseLocalDate(object.optString("arrivalDate"));
        this.departureDate = API_DATE_PARSER.parseLocalDate(object.optString("departureDate"));
        this.hotelName = object.optString("hotelName");
        final String addressLine1 = object.optString("hotelAddress");
        final String city = object.optString("hotelCity");
        final String stateProvinceCode = object.optString("hotelStateProvinceCode");
        final String countryCode = object.optString("hotelCountryCode");
        final String postalCode = object.optString("hotelPostalCode");
        this.hotelAddress = new Address(addressLine1, city, stateProvinceCode, countryCode, postalCode);
        this.roomDescription = object.optString("roomDescription");
        this.nonRefundable = object.optBoolean("nonRefundable");
        this.rateOccupancyPerRoom = object.optInt("rateOccupancyPerRoom");
        this.cancellationPolicy = new CancellationPolicy(object, this.arrivalDate);
        this.rateInformations = Collections.unmodifiableList(Rate.parseFromRateInformations(object));
    }
}