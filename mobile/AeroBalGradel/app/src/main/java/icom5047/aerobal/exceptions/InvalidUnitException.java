package icom5047.aerobal.exceptions;

public class InvalidUnitException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidUnitException() {
        super();
    }

    public InvalidUnitException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidUnitException(Throwable throwable) {
        super(throwable);
    }

    public InvalidUnitException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
