package wagemaker.co.uk.db;

public class ImportException extends Exception {

    private static final long serialVersionUID = 1L;

    public ImportException(String message) {
        super(message);
    }

    public ImportException(Exception e) {
        super(e);
    }

    public ImportException(String message, Exception e) {
        super(message, e);
    }

}
