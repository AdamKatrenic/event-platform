package com.adam.event_platform.exception;
/** 
* Custom exception specifically used when a resource conflict occurs,
* such as an event reaching capacity or a booking attempting to change status illegally.
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}